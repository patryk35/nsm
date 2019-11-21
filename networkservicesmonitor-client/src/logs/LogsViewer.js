import React, {Component} from 'react';
import './LogsViewer.css';
import {notification, Row} from 'antd/lib/index';
import {AutoComplete, Button, DatePicker, Input, Table, TimePicker} from 'antd';
import {LOGS_LIST_SIZE} from "../configuration";
import {getLogs} from "../utils/APIRequestsUtils";
import moment from 'moment';
import {convertDate} from "../utils/SharedUtils";


const Option = AutoComplete.Option;
const OptGroup = AutoComplete.OptGroup;

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
            query: "",
            dateFrom: null,
            timeFrom: moment('00:00:00', 'HH:mm:ss'),
            dateTo: null,
            timeTo: null
        };
        this.loadLogsList = this.loadLogsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }


    loadLogsList(page = 0, size = LOGS_LIST_SIZE) {
        // TODO: loading is not working
        // TODO: content out of boredr sometimes
        const state = this.state;
        if (state.query === "") {
            return;
        }
        //todo split time and date
        let logsRequest = {
            query: state.query || "",
            page: page || 0,
            size: size || LOGS_LIST_SIZE,
            datetimeFrom: (state.dateFrom !== null && state.timeFrom !== null) && (state.dateFrom !== "" && state.timeFrom !== "") ? state.dateFrom + " " + state.timeFrom : null,
            datetimeTo: (state.dateTo !== null && state.timeTo !== null) && (state.dateTo !== "" && state.timeTo !== "") ? state.dateTo + " " + state.timeTo : null,
        };
        console.log(logsRequest);
        this.setState({
            isLoading: true
        });

        let promise = getLogs(logsRequest);

        if (!promise) {
            return;
        }


        promise
            .then(response => {
                const logs = this.state.logs.slice();
                this.setState({
                    logs: response.content,
                    page: response.page,
                    size: response.size,
                    totalElements: response.totalElements,
                    totalPages: response.totalPages,
                    last: response.last,
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                logs: [],
                isLoading: false
            });
            notification.error({
                message: 'Problem podczas pobierania danych!',
                description: ' Spróbuj ponownie później!',
                duration: 5
            });
        });
    }

    componentDidMount() {
        this.loadLogsList();
    }

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

    render() {
        const state = this.state;


        const columns = [
            {title: 'Czas', dataIndex: 'time', key: 'time'},
            {title: 'Serwis', dataIndex: 'service', key: 'service'},
            {title: 'Ścieżka', dataIndex: 'path', key: 'path'},
            {title: 'Log', dataIndex: 'log', key: 'log', width: "50%"},
        ];

        const data = [];
        let i = 0;
        this.state.logs.forEach((log, index) => {
            data.push({
                key: i,
                time: convertDate(log.timestamp),
                service: log.serviceName, //TODO
                path: log.path,
                log: log.log,
            });
            i = i + 1;
        });


        const dataSource = [
            {
                title: 'Przykładowe wyszukiwania',
                children: [
                    {
                        title: 'agent="Name" DEBUG',
                    },
                    {
                        title: 'agentId="ID" service="monitorservice"',
                    },
                ],
            },

        ];

        function renderTitle(title) {
            return (
                <span>
                    {title}
                </span>
            );
        }

        function onChangeDate(date, dateString) {
            console.log(date, dateString);
        }

        function onChangeTime(date, dateString) {
            console.log(date, dateString);
        }

        const options = dataSource
            .map(group => (
                <OptGroup key={group.title} label={renderTitle(group.title)}>
                    {group.children.map(opt => (
                        <Option key={opt.title} value={opt.title}>
                            {opt.title}
                        </Option>
                    ))}
                </OptGroup>
            ));

        //TODO: checking time from and time to relation (time to cannot be earlier than time from)
        return (
            <article>
                <div className="logs-viewer-container">
                    <Row gutter={16}>
                        <div>
                            <AutoComplete
                                className="logs-viewer-auto-complete"
                                dropdownClassName="certain-category-search-dropdown"
                                dropdownMatchSelectWidth={false}
                                dropdownStyle={{width: 300}}
                                size="large"
                                style={{width: '100%'}}
                                //dataSource={options}
                                placeholder="Wyszukaj (Dostępne wyszukiwania: agent, agentId, service, serviceId, path)"
                                optionLabelProp="value"
                            >
                                <Input onPressEnter={(e) => {
                                    this.setState({
                                        query: e.target.value
                                    });
                                }} onBlur={(e) => {
                                    this.setState({
                                        query: e.target.value
                                    });
                                }}/>
                            </AutoComplete>
                        </div>

                        <div>
                            <DatePicker placeholder="Od dnia" className="logs-viewer-date-picker"
                                        onChange={(date, dateString) => {
                                            this.setState({
                                                dateFrom: dateString
                                            });
                                        }}/>
                            <TimePicker placeholder="Od godziny" defaultOpenValue={moment('00:00:00', 'HH:mm:ss')}
                                        className="logs-viewer-time-picker" onChange={(moment, timeString) => {
                                this.setState({
                                    timeFrom: timeString
                                });
                            }}/>
                        </div>
                        <div>
                            <DatePicker placeholder="Do dnia" className="logs-viewer-date-picker"
                                        onChange={(date, dateString) => {
                                            this.setState({
                                                dateTo: dateString
                                            });
                                        }}/>
                            <TimePicker placeholder="Do godziny" defaultOpenValue={moment('00:00:00', 'HH:mm:ss')}
                                        className="logs-viewer-time-picker" onChange={(moment, timeString) => {
                                this.setState({
                                    timeTo: timeString
                                });
                            }}/>
                        </div>
                        <div>
                            <Button type="primary" htmlType="submit" size="small" className="logs-viewer-form-button"
                                    onClick={(e) => {
                                        this.loadLogsList()
                                    }}>
                                Szukaj
                            </Button>
                        </div>
                    </Row>
                </div>
                <div className="logs-viewer-container">
                    <Table
                        columns={columns}
                        dataSource={data}
                        size={"small"}
                        loading={this.state.isLoading}
                        locale={{
                            emptyText: "Brak danych"
                        }}
                        scroll={{x: true}}
                        pagination={{
                            current: state.page + 1,
                            defaultPageSize: state.size,
                            hideOnSinglePage: true,
                            total: state.totalElements,
                            onShowSizeChange: ((current, size) => this.loadLogsList(current - 1, size)),
                            onChange: ((current, size) => this.loadLogsList(current - 1, size)),
                            loading: state.isLoading
                        }}
                    />
                </div>
            </article>
        );
    }
}


export default LogsViewer;
