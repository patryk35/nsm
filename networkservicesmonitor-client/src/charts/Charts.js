import React, {Component} from 'react';
import './Charts.css';
import {notification, Row} from 'antd/lib/index';
import {AutoComplete, Button, Col, DatePicker, Form, Input, Select} from 'antd';
import {getMonitoredParameterValues} from "../utils/APIRequestsUtils";
import LoadingSpin from '../common/spin/LoadingSpin';
import moment from 'moment';
import ChartsModule from "./ChartsModule";


const {Option, OptGroup} = AutoComplete;
const FormItem = Form.Item;
const dataSource = [
    {
        title: 'Przykłady',
        children: [
            'agent="AppServer"',
            'agent="AppServer" service="application-1"',
            'agent="AppServer" service="application-1" parameter="AgentCPUUsage"'
        ],
    },
    {
        title: 'Wszystkie wartości dla danego agenta',
        children: [
            'agent=""', 'agentId=""'
        ],
    },
    {
        title: 'Wszystkie wartości dla danego serwisu',
        children: [
            'agent="" service=""', 'agentId="" service=""', 'agent="" serviceId=""', 'agentId="" serviceId=""'
        ],
    },
    {
        title: 'Wszystkie wartości parametru',
        children: [
            'agent="" parameter=""', 'agentId="" parameter=""',
        ],
    },
    {
        title: 'Wszystkie wartości dla określonego serwisu i parametru',
        children: [
            'agent="" parameter=""', 'agentId="" parameter=""',
            'agent="" service="" parameter=""', 'agentId="" service="" parameter=""', 'agent="" serviceId="" parameter=""', 'agentId="" serviceId="" parameter=""'
        ],
    },
];

class Charts extends Component {
    constructor(props) {
        super(props);
        this.state = {
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
            chartType: "LineChart",
            data: [],
            charts: [],
            apiValidation: {
                message: "",
                status: ""
            }
        };
        this.loadCharts = this.loadCharts.bind(this);
    }

    checkForDataBreaks(intervals, i) {
        if (intervals.length < 4) {
            return true;
        }

        if (i === intervals.length - 1) {
            return Math.abs(1 - intervals[i] / intervals[i - 1]) > 5
        } else if (i === 0) {
            return Math.abs(1 - intervals[i] / intervals[i + 1]) > 5
        }
        return Math.abs(1 - intervals[i] / intervals[i - 1]) > 5 && Math.abs(1 - intervals[i] / intervals[i + 1]) > 5 &&
            Math.abs(1 - intervals[i] / intervals[i - 2]) > 5 && Math.abs(1 - intervals[i] / intervals[i + 2]) > 5;

    }

    loadCharts() {
        const state = this.state;

        let monitoredParameterValuesRequest = {
            query: state.query.value || "",
            datetimeFrom: (state.momentFrom !== "" && state.momentFrom !== null) ? state.momentFrom.format("YYYY-MM-DD HH:mm:ss") : null,
            datetimeTo: (state.momentTo.value !== "" && state.momentTo.value !== null) ? state.momentTo.value.format("YYYY-MM-DD HH:mm:ss") : null,
        };
        this.setState({
            isLoading: true
        });

        let promise = getMonitoredParameterValues(monitoredParameterValuesRequest);

        if (!promise) {
            return;
        }

        promise
            .then(response => {
                this.setState({
                    data: this.generateChartsData(response),
                    isLoading: false,
                    apiValidation: {
                        status: "",
                        message: ""
                    }
                });

            }).catch((error) => {
            this.setState({
                isLoading: false,
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
            case "parameter with provided name not found":
                return "Nie znaleziono parametru o podanej nazwie!";
            default:
                return ""
        }
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                data: [],
                isLoading: false,
            });
        }
    }

    convertDate = (date, offset) => {
        let regexp = /(\w+)-(\w+)-(\w+)T(\w+):(\w+):(\w+).(\w+)\+0000/g;
        let match = regexp.exec(date);
        if (match !== null) {
            return new Date(parseInt(match[1]), parseInt(match[2]) - 1, parseInt(match[3]), parseInt(match[4]), parseInt(match[5]), parseInt(match[6]), parseInt(match[7]) + offset)
        }

    };

    generateChartsData(monitoredParametersValues) {
        const data = [];
        if (monitoredParametersValues !== null && monitoredParametersValues.length > 0) {
            monitoredParametersValues.forEach((monitoredParameter, j) => {
                let parameterData = [];
                const title = monitoredParameter.name;
                const multipler = parseFloat(monitoredParameter.multiplier);
                parameterData.push(
                    [
                        {type: 'datetime', id: 'Data'},
                        {type: 'number', id: title}
                    ]
                );
                let lastDate = null;
                let interval;
                let intervals = [];
                monitoredParameter.content.forEach((val) => {
                    if (lastDate !== null) {
                        interval = this.convertDate(val.timestamp, 0) - lastDate;
                        intervals.push(interval);
                    }
                    lastDate = this.convertDate(val.timestamp, 0);
                });

                monitoredParameter.content.forEach((val, i) => {
                    let date = this.convertDate(val.timestamp, 0);
                    if (i > 0 && this.checkForDataBreaks(intervals, i - 1)) {
                        parameterData.push([
                            this.convertDate(val.timestamp, -1),
                            null
                        ]);
                    }
                    parameterData.push([
                        date,
                        parseFloat(val.value) * multipler
                    ]);
                });
                if (parameterData.length > 1) {
                    data.push({
                        key: j,
                        title: title,
                        unit: monitoredParameter.unit,
                        additionalMessage: (monitoredParameter.dataLimit < monitoredParameter.foundDataCount) ?
                            "Ze względu na zbyt dużą ilość znalezionych rekordów powyższy wykres może nie odpowiadać w 100% " +
                            "rzeczywistości [znalzeiono " + monitoredParameter.foundDataCount + " rekordów, które " +
                            "przekształcone zostały na " + monitoredParameter.dataLimit + " rekordów]. Aby uzyskać " +
                            "dokładniejsze dane zmniejsz przdział czasowy." : null,
                        data: parameterData
                    })
                }

            })
        }
        return data;
    }


    render() {
        const state = this.state;
        return (
            <div>
                <div className="charts-container">
                    <Row gutter={16}>
                        <Col span={24}>
                            <FormItem
                                validateStatus={this.state.query.status} help={this.state.query.message}>
                                <AutoComplete
                                    size={"default"}
                                    style={{width: '100%'}}
                                    dataSource={this.state.dataSource.map(this.renderOption)}
                                    placeholder="Wyszukaj"
                                    optionLabelProp="value"
                                    onSearch={this.handleSearch}>
                                    <Input onBlur={(e) => {
                                        this.validateQuery(e);
                                    }}/>
                                </AutoComplete>
                            </FormItem>
                        </Col>
                        <div>
                            <Select size={"small"} className="charts-form-select"
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
                                                className={"charts-date-picker"}
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
                                    <DatePicker showTime placeholder="Do" className={"charts-date-picker-right"}
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
                                <Button type="primary" htmlType="submit" size="small" className="charts-form-button"
                                        disabled={!this.validate()}
                                        onClick={() => {
                                            this.loadCharts()
                                        }}>
                                    Szukaj
                                </Button>
                            </FormItem>
                        </div>
                    </Row>
                </div>

                {state.data.length !== 0 && <div className="charts-container">
                    <div>
                        <Select className="charts-form-chart-selector" defaultValue={"Wykres Liniowy"}
                                onChange={(event) => this.handleChangeChartType(event)}>
                            <Option key="LineChart" title="LineChart">Wykres Liniowy</Option>
                            <Option key="AreaChart" title="AreaChart">Wykres Warstwowy</Option>
                            <Option key="ScatterChart" title="ScatterChart">Wykres Punktowy</Option>
                        </Select>
                    </div>
                </div>}

                <div className="charts-container">

                    {state.isLoading ? (
                        <LoadingSpin/>
                    ) : (
                        <div>
                            {state.data.length === 0 ? (
                                <p className={"charts-info"}>Tu pojawią się wykresy dla wybranych danych</p>
                            ) : (
                                <ChartsModule data={this.state.data} chartType={this.state.chartType}> </ChartsModule>
                            )}
                        </div>
                    )}
                </div>
            </div>

        );
    }

    validate() {
        return this.state.momentTo !== null && this.state.momentFrom !== null && this.state.query.status !== "error"
            && this.state.query.value !== "";
    }

    handleChangeChartType(event) {
        this.setState({
            chartType: event
        });
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
            if (ds.title !== "Przykłądy") {
                ds.children.forEach(str => {
                    let regexString = str.replace(new RegExp('""', 'g'), "\"(.+)\"");
                    regexString = "^" + regexString + "$";
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
        if (typeof item === "string") {
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


export default Charts;
