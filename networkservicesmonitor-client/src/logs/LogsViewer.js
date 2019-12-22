import React, {Component} from 'react';
import './LogsViewer.css';
import {notification, Row} from 'antd/lib/index';
import {AutoComplete, Button, Col, DatePicker, Form, Input, Select, Table, TimePicker} from 'antd';
import {LOGS_LIST_SIZE} from "../configuration";
import {getLogs} from "../utils/APIRequestsUtils";
import moment from 'moment';
import {convertDate} from "../utils/SharedUtils";
import LogsModule from "./LogsModule";

const FormItem = Form.Item;

const { Option, OptGroup } = AutoComplete;

const dataSource = [
    {
        title: 'Przykłady',
        children: [
            'agent="AppServer"',
            'agent="AppServer" service="application-1"',
            'agent="AppServer" service="application-1" path="/var/log/application-1/"',
        ],
    },
    {
        title: 'Aby wyszukać logi zawierające specyficzną frazę, dodaj na końcu formuły treść frazy. Przykład dla frazu \"ERROR\" poniżej',
        children: [
            'agent="test" ERROR'
        ]

    },
    {
        title: 'Wszystkie logi dla danego agenta',
        children: [
            'agent=""', 'agentId=""'
        ],
    },
    {
        title: 'Wszystkie logi dla danego serwisu',
        children: [
            'agent="" service=""', 'agentId="" service=""', 'agent="" serviceId=""', 'agentId="" serviceId=""'
        ],
    },
    {
        title: 'Wszystkie logi dla danej ścieżki',
        children: [
            'agent="" path=""', 'agentId="" path=""'
        ],
    },
    {
        title: 'Wszystkie logi dla danego serwisu i ścieżki',
        children: [
            'agent="" service="" path=""', 'agentId="" service="" path=""', 'agent="" serviceId="" path=""', 'agentId="" serviceId="" path=""'
        ],
    },

];

class LogsViewer extends Component {
    constructor(props) {
        super(props);
        this.state = {
            logs: [],
            page: 0,
            size: LOGS_LIST_SIZE,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false,
            query: {
                value: "",
                message: "",
                status: ""
            },
            momentFrom: "", //moment().add(-5, "minutes"),
            dataSource: dataSource,
            momentTo: {
                value: "", //moment(),
                message: "",
                status: ""
            },
            apiValidation: {
                message: "",
                status: ""
            }
        };
        this.loadLogsList = this.loadLogsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }


    loadLogsList(page = 0, size = LOGS_LIST_SIZE) {
        const state = this.state;
        if (state.query === "") {
            return;
        }
        let logsRequest = {
            page: page || 0,
            size: size || LOGS_LIST_SIZE,
            query: state.query.value || "",
            datetimeFrom: (state.momentFrom !== "" && state.momentFrom !== null) ? state.momentFrom.format("YYYY-MM-DD HH:mm:ss") : null,
            datetimeTo: (state.momentTo.value !== "" && state.momentTo.value !== null) ? state.momentTo.value.format("YYYY-MM-DD HH:mm:ss") : null,
        };

        this.setState({
            isLoading: true
        });

        let promise = getLogs(logsRequest);

        if (!promise) {
            return;
        }


        promise
            .then(response => {
                this.state.logs.slice();
                this.setState({
                    logs: response.content,
                    page: response.page,
                    size: response.size,
                    totalElements: response.totalElements,
                    totalPages: response.totalPages,
                    last: response.last,
                    isLoading: false,
                    apiValidation: {
                        status: "",
                        message: ""
                    }
                })
            }).catch((error) => {
                this.setState({
                    isLoading: false,
                    logs: [],
                    apiValidation: {
                        status: "error",
                        message: this.mapErrorToMessage(error.queryError)
                    }
                });
                if (this.state.apiValidation.message === "") {
                    notification.error({
                        message: 'Problem podczas pobierania danych!',
                        description: ' Spróbuj ponownie później!',
                        duration: 5
                    });
                }
            });
    };

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                logs: [],
                page: 0,
                size: LOGS_LIST_SIZE,
                totalElements: 0,
                totalPages: 0,
                last: true,
                isLoading: false
            });
            this.loadLogsList();
        }
    }

    handleLoadMore() {
        this.loadLogsList(this.state.page + 1);
    }

    mapErrorToMessage(queryError) {
        switch (queryError) {
            case "use only one from set agent or agentId":
                return "Podana formuła wyszukiwania zawiera klucze agent i agentId. Wybierz tylko jeden z kluczy!";
            case "agent not found with provided name":
                return "Agent o podanej nazwie nie został znaleziony";
            case "agent not found with provided id":
                return "Agent o podanym id nie został znaleziony";
            case "agent and agentId missing in query":
                return "Podana formuła wyszukiwania musi zawierać klucz agent lub agentId";

            case "use only one from set service or serviceId":
                return "Podana formuła wyszukiwania zawiera klucze service i serviceId. Wybierz tylko jeden z kluczy!";
            case "service not found with provided name":
                return "Serwis o podanej nazwie nie został znaleziony";
            case "service not found with provided id":
                return "Serwis o podanym id nie został znaleziony";
            default:
                return ""
        }
    }

    render() {
        return (
            <article>
                <div className="logs-viewer-container">
                    <Row gutter={16}>
                        <Col span={24}>
                            <FormItem
                                validateStatus={this.state.query.status} help={this.state.query.message}>
                                <AutoComplete className="logs-viewer-form-select"
                                    size={"default"}
                                    style={{width: '100%'}}
                                    dataSource={this.state.dataSource.map(this.renderOption)}
                                    placeholder="Wyszukaj"
                                    optionLabelProp="value"
                                    onSearch={this.handleSearch}
                                >
                                    <Input onBlur={(e) => {
                                        this.validateQuery(e);
                                    }}/>
                                </AutoComplete>
                            </FormItem>
                        </Col>

                        <div>
                            <Select size={"small"} className="logs-viewer-form-select"
                                    placeholder={"Szybkie ustawianie zakresu czasu"}
                                    onChange={(event) => this.handleChangeTimeSelect(event)}>
                                <Option key="5m" title="5m">Ostatnie 5 minut</Option>
                                <Option key="10m" title="10m">Ostatnie 10 minut</Option>
                                <Option key="15m" title="15m">Ostatnie 15 minut</Option>
                                <Option key="30m" title="30m">Ostatnie 30 minut</Option>
                                <Option key="45m" title="45m">Ostatnie 45 minut</Option>
                                <Option key="1h" title="1h">Ostatnia godzina</Option>
                                <Option key="2h" title="2h">Ostatnie 2 godziny</Option>
                                <Option key="5h" title="5h">Ostatnie 5 godzin</Option>
                                <Option key="12h" title="12h">Ostatnie 12 godzin</Option>
                                <Option key="24h" title="24h">Ostatnie 24 godziny</Option>
                                <Option key="today" title="today">Dziś</Option>
                                <Option key="yesterday" title="yesterday">Wczoraj</Option>
                            </Select>
                        </div>
                        <Row>
                            <Col span={12}>
                                <FormItem
                                    >
                                    <DatePicker showTime value={this.state.momentFrom} placeholder="Od"
                                                className={"logs-viewer-date-picker"}
                                                onChange={(date) => {
                                                    this.setState({
                                                        momentFrom: date
                                                    });
                                                }}/>
                                </FormItem>
                            </Col>
                            <Col span={12}>
                                <FormItem
                                    validateStatus={this.state.momentTo.status}
                                    help={this.state.momentTo.message}>
                                    <DatePicker showTime placeholder="Do" className={"logs-viewer-date-picker-right"}
                                                value={this.state.momentTo.value}
                                                onChange={(date) => {
                                                    this.setState({
                                                        momentTo: {
                                                            value: date,
                                                            status: (this.state.momentTo - this.state.momentFrom) <= 0 ? "error" : "",
                                                            message: (this.state.momentTo - this.state.momentFrom) <= 0 ? "Data \"Do\" nie może być przed datą \"Od\"" : ""
                                                        }
                                                    });
                                                }}/>
                                </FormItem>
                            </Col>
                        </Row>
                        <div>
                            <FormItem
                                validateStatus={this.state.apiValidation.status}
                                help={this.state.apiValidation.message}>
                                <Button type="primary" htmlType="submit" size="small" className="logs-viewer-form-button"
                                        disabled={!this.validate()}
                                        onClick={() => {
                                            this.loadLogsList()
                                        }}>
                                    Szukaj
                                </Button>
                            </FormItem>
                        </div>
                    </Row>
                </div>
                <LogsModule logs={this.state.logs} isLoading={this.state.isLoading} page={this.state.page} size={this.state.size}
                totalElements={this.state.totalElements} loadLogsList={this.loadLogsList}></LogsModule>
            </article>
        );
    }

    validate() {
        return this.state.momentTo !== null && this.state.momentFrom !== null && this.state.query.status !== "error"
            && this.state.query.value !== "";
    }

    handleChangeTimeSelect(event) {
        let regexp = /(\w+)m/g;
        let match = regexp.exec(event);
        if (match !== null) {
            this.setState({
                momentFrom: moment().add(-match[1], "minutes"),
                momentTo: {
                    value: moment()
                }
            });
            return;
        }

        regexp = /(\w+)h/g;
        match = regexp.exec(event);
        if (match !== null) {
            this.setState({
                momentFrom: moment().add(-match[1], "hours"),
                momentTo: {
                    value: moment()
                }
            });
            return;
        }

        if (event === "today") {
            this.setState({
                momentFrom: moment('00:00:00', 'HH:mm:ss'),
                momentTo: {
                    value: moment()
                }
            });
            return;
        }

        if (event === "yesterday") {
            this.setState({
                momentFrom: moment('00:00:00', 'HH:mm:ss').add(-24, "hours"),
                momentTo: {
                    value: moment('23:59:59', 'HH:mm:ss').add(-24, "hours")
                }
            });
        }
    }

    validateQuery(event) {
        let status = "error";
        let regexp, match;
        dataSource.forEach(ds => {
            if(ds.title !== "Przykłądy"){
                ds.children.forEach(str => {
                    let regexString = str.replace(new RegExp('""', 'g'), "\"(.+)\"");
                    regexString = "^(" + regexString + "|" + regexString + "\\s.*)$";
                    regexp = new RegExp(regexString, "g");
                    match = regexp.exec(event.target.value.trimEnd());
                    if (match !== null) {
                        status = "";
                        return false; // to break loop
                    }
                })
            }

        });
        this.setState({
            query: {
                value: event.target.value,
                message: status === "error" ? "Podana formuła wyszukiwania nie jest prawidłowa. Skorzystaj z podpowiedzi, aby stworzyć poprawną formułę." : "",
                status: status
            }
        });
    }

    handleSearch = value => {
        let newDataSource = [];
        newDataSource.push(value);
        dataSource.forEach(ds => {
            newDataSource.push(ds)
        });
        this.setState({
            dataSource: value ? newDataSource : dataSource,
        });
    };

    renderOption(item) {
        if(typeof item === "string"){
            return (
                <Option key={item} value={item}>
                    {item}
                </Option>
            )
        } else {
            return (
                <OptGroup key={item.title} label={item.title}>
                    {item.children.map(opt => (
                        <Option key={opt} value={opt}>
                            {opt}
                        </Option>
                    ))}
                </OptGroup>
            );
        }
    };

}


export default LogsViewer;
