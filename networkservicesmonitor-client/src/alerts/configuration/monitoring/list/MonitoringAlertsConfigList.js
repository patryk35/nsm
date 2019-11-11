import React, {Component} from 'react';
import './MonitoringAlertsConfigList.css';
import {Icon, Table} from 'antd';
import {ALERTS_CONFIGS_LIST_SIZE} from "../../../../configuration";
import {getMonitoringAlertConfigList} from "../../../../utils/APIRequestsUtils";
import {convertLevelToName, handleConfigurationDeleteClick} from "../../shared/AlertsConfigurationShared";


class MonitoringAlertsConfigList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            configs: [],
            page: 0,
            size: ALERTS_CONFIGS_LIST_SIZE,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
        this.loadConfigsList = this.loadConfigsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }


    loadConfigsList(page = 0, size = ALERTS_CONFIGS_LIST_SIZE) {
        let configurationPromise = getMonitoringAlertConfigList(page, size);

        configurationPromise
            .then(response => {
                this.state.configs.slice();
                this.setState({
                    configs: response.content,
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
        this.loadConfigsList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                configs: [],
                page: 0,
                size: ALERTS_CONFIGS_LIST_SIZE,
                totalElements: 0,
                totalPages: 0,
                last: true,
                isLoading: false
            });
            this.loadAlertsList();
        }
    }

    handleLoadMore() {
        this.loadConfigsList(this.state.page + 1);
    }

    refresh = () => {
        this.loadConfigsList(this.state.page);
    };

    render() {
        const state = this.state;
        const columns = [
            {title: 'Agent', dataIndex: 'agentName', key: 'agentName'},
            {title: 'Serwis', dataIndex: 'serviceName', key: 'serviceName'},
            {title: 'Wiadomość', dataIndex: 'message', key: 'message'},
            {title: 'Poziom', dataIndex: 'level', key: 'level'},
            {title: 'Parametr', dataIndex: 'monitoredParameterTypeName', key: 'monitoredParameterTypeName'},
            {title: 'Warunek', dataIndex: 'condition', key: 'condition'},
            {title: 'Wartość', dataIndex: 'value', key: 'value'},
            {title: 'Włączony', dataIndex: 'enabled', key: 'enabled'},

            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="service-operation">
                        <a href={"/alert/monitoring/edit/" + record.key}><Icon
                            type="edit"/></a>
                        <a title="Usuń"
                           onClick={() => handleConfigurationDeleteClick(this.refresh, record.key, "monitoring")}><Icon
                            type="delete"/></a>
                    </span>
            }
        ];

        const data = [];
        this.state.configs.forEach((config) => {
            data.push({
                key: config.id,
                agentName: config.agentName,
                serviceName: config.serviceName,
                message: config.message,
                monitoredParameterTypeName: config.monitoredParameterTypeName,
                condition: config.condition,
                value: config.value,
                enabled: config.enabled ? "Tak" : "Nie",
                level: convertLevelToName(config.alertLevel)
            });

        });

        return (
            <article className="monitoring-alert-list-container">
                <Table
                    columns={columns}
                    dataSource={data}
                    loading={this.state.isLoading}
                    locale={{
                        emptyText: "Brak danych"
                    }}
                    pagination={{
                        current: state.page + 1,
                        defaultPageSize: state.size,
                        hideOnSinglePage: true,
                        total: state.totalElements,
                        onShowSizeChange: ((current, size) => this.loadAlertsList(current - 1, size)),
                        onChange: ((current, size) => this.loadAlertsList(current - 1, size))
                    }}/>
            </article>

        )
    }
}


export default MonitoringAlertsConfigList;