import React, {Component} from 'react';
import './LogsConfigurationList.css';
import {Button, Icon, Table} from 'antd';
import {AGENT_SERVICES_CONFIGURATION_LIST_SIZE} from "../../../../configuration";
import {getAgentServicesLogsConfigurationsList} from "../../../../utils/APIRequestsUtils";
import {handleMonitoringConfigurationDeleteClick} from "../../shared/ConfigurationShared";


class LogsConfigurationList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            configurations: [],
            page: 0,
            size: 10,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
        this.loadConfigurationsList = this.loadConfigurationsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }


    loadConfigurationsList(page = 0, size = AGENT_SERVICES_CONFIGURATION_LIST_SIZE) {
        let promise = getAgentServicesLogsConfigurationsList(this.props.serviceId, page, size);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.state.configurations.slice();
                this.setState({
                    configurations: response.content,
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
        this.loadConfigurationsList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                configurations: [],
                page: 0,
                size: 10,
                totalElements: 0,
                totalPages: 0,
                last: true,
                isLoading: false
            });
            this.loadConfigurationsList();
        }
    }

    handleLoadMore() {
        this.loadConfigurationsList(this.state.page + 1);
    }


    refresh = () => {
        this.loadConfigurationsList(this.state.page);
    };

    render() {
        const state = this.state;
        const columns = [
            {title: 'Id', dataIndex: 'key', key: 'key'},
            {title: 'Ścieżka do logów', dataIndex: 'path', key: 'path'},
            {title: 'Maska monitorowanych plików', dataIndex: 'monitoredFilesMask', key: 'monitoredFilesMask'},
            {title: 'Maska zbieranych lini logów', dataIndex: 'logLineRegex', key: 'logLineRegex'},

            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="service-operation">
                        <a href={"/agents/service/logs/edit/" + record.key}><Icon
                            type="edit"/></a>
                        <a title="Usuń"
                           onClick={() => handleMonitoringConfigurationDeleteClick(this.refresh, record.key, "logs")}><Icon
                            type="delete"/></a>
                    </span>
            }
        ];

        const data = [];
        this.state.configurations.forEach((configuration, index) => {
            data.push({
                key: configuration.id,
                path: configuration.path,
                monitoredFilesMask: configuration.monitoredFilesMask,
                logLineRegex: configuration.logLineRegex
            });

        });
        return (
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
                            onShowSizeChange: ((current, size) => this.loadConfigurationsList(current - 1, size)),
                            onChange: ((current, size) => this.loadConfigurationsList(current - 1, size))
                        }}/>
                    {this.props.editAccess && (
                        <Button type="primary"
                                href={"/agents/service/" + this.props.serviceId + "/logs/create"}>
                            Dodaj nową konfigurację
                        </Button>
                    )}

                </div>
            ) : (
                <div>
                    <h3>Brak konfiguracji dla wybranego agenta</h3>
                    {this.props.editAccess && (
                        <Button type="primary"
                                href={"/agents/service/" + this.props.serviceId + "/logs/create"}>
                            Dodaj pierwszą konfigurację
                        </Button>
                    )}

                </div>
            ));
    }


}


export default LogsConfigurationList;