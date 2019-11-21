import React, {Component} from 'react';
import './MonitoringAlertsList.css';
import {Icon, notification, Table} from 'antd';
import {ALERTS_LIST_SIZE} from "../../../configuration";
import {getMonitoringAlertsList} from "../../../utils/APIRequestsUtils";
import {convertDate} from "../../../utils/SharedUtils";
import {genIcon} from "../shared/SharedFunctions";


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
        this.setState({
            isLoading: true
        });
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
            });
            notification.error({
                message: 'Problem podczas pobierania danych!',
                description: ' Spróbuj ponownie później!',
                duration: 5
            });
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
            {title: 'Poziom', key: 'level', render: (text, record) => genIcon(record.level)},
            {title: 'Czas', dataIndex: 'timestamp', key: 'timestamp'},
            {title: 'Wiadomość', dataIndex: 'message', key: 'message'},
            {title: 'Agent', dataIndex: 'agentName', key: 'agentName'},
            {title: 'Serwis', dataIndex: 'serviceName', key: 'serviceName'},
            {title: 'Parametr', dataIndex: 'parameterTypeName', key: 'parameterTypeName'},
            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="service-operation">
                        <Icon type="unordered-list" onClick={() => this.props.showDrawer(record.key, "monitoring")}/>
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
                timestamp: convertDate(alert.timestamp),
                message: alert.message,
                level: alert.alertLevel
            });

        });

        return (
            <Table
                scroll={{x: true}}
                columns={columns}
                dataSource={data}
                loading={this.state.isLoading}
                locale={{
                    emptyText: "Brak alertów"
                }}
                size={"small"}
                pagination={{
                    current: state.page + 1,
                    defaultPageSize: state.size,
                    hideOnSinglePage: true,
                    total: state.totalElements,
                    onShowSizeChange: ((current, size) => this.loadAlertsList(current - 1, size)),
                    onChange: ((current, size) => this.loadAlertsList(current - 1, size))
                }}/>
        )
    }
}


export default MonitoringAlertsList;