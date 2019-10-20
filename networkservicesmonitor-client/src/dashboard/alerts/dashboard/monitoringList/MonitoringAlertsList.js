import React, {Component} from 'react';
import './MonitoringAlertsList.css';
import {Icon, Table} from 'antd';
import {ALERTS_LIST_SIZE} from "../../../../configuration";
import {getMonitoringAlertsList} from "../../../../utils/APIRequestsUtils";
import LoadingSpin from "../../../../common/LoadingSpin";


class MonitoringAlertsList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            alerts: [],
            page: 0,
            size: ALERTS_LIST_SIZE,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
        this.loadAlertsList = this.loadAlertsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }


    loadAlertsList(page = 0, size = ALERTS_LIST_SIZE) {
        let configurationPromise = getMonitoringAlertsList(page, size);

        configurationPromise
            .then(response => {
                this.state.alerts.slice();
                this.setState({
                    alerts: response.content,
                    page: response.page,
                    size: response.size,
                    totalElements: response.totalElements,
                    totalPages: response.totalPages,
                    last: response.last,
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                isLoading: false
            })
        });
    }

    componentDidMount() {
        this.loadAlertsList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                alerts: [],
                page: 0,
                size: ALERTS_LIST_SIZE,
                totalElements: 0,
                totalPages: 0,
                last: true,
                isLoading: false
            });
            this.loadAlertsList();
        }
    }

    handleLoadMore() {
        this.loadAlertsList(this.state.page + 1);
    }

    render() {
        const state = this.state;
        const columns = [
            {title: 'Agent', dataIndex: 'agentName', key: 'agentName'},
            {title: 'Serwis', dataIndex: 'serviceName', key: 'serviceName'},
            {title: 'Parametr', dataIndex: 'parameterTypeName', key: 'parameterTypeName'},
            {title: 'Czas', dataIndex: 'timestamp', key: 'timestamp'},
            {title: 'Wiadomość', dataIndex: 'message', key: 'message'},

            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="service-operation">
                        /*<a href={"agents/details/" + record.key} className="agent-list-menu-item"
                           title="Szczegóły"><Icon type="unordered-list"/></a>*/
                    </span>
            }
        ];

        const data = [];
        this.state.alerts.forEach((alert) => {
            data.push({
                key: alert.id,
                agentName: alert.agentName,
                serviceName: alert.serviceName,
                parameterTypeName: alert.parameterTypeName,
                timestamp: alert.timestamp,
                message: alert.message
            });

        });

        return (
            this.state.isLoading ? (<div>Trwa wczytywanie danych <LoadingSpin/></div>) : (
                data.length !== 0 ? (
                    <div>
                        <Table
                            columns={columns}
                            dataSource={data}
                            pagination={{
                                current: state.page + 1,
                                defaultPageSize: state.size,
                                hideOnSinglePage: true,
                                total: state.totalElements,
                                onShowSizeChange: ((current, size) => this.loadAlertsList(current - 1, size)),
                                onChange: ((current, size) => this.loadAlertsList(current - 1, size))
                            }}/>
                    </div>
                ) : (
                    <div>
                        <h3>Brak alertów</h3>
                    </div>
                )
            )
        )
    }
}


export default MonitoringAlertsList;