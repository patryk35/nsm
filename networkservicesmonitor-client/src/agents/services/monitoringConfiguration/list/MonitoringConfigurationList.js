import React, {Component} from 'react';
import './MonitoringConfigurationList.css';
import {Button, Icon, Table} from 'antd';
import {AGENT_SERVICES_CONFIGURATION_LIST_SIZE} from "../../../../configuration";
import {
    getAgentServicesMonitoringConfigurationsList,
    loadNewAvailableMonitoringParameters
} from "../../../../utils/APIRequestsUtils";
import {handleConfigurationDeleteClick} from "../../shared/ConfigurationShared";
import LoadingSpin from "../../../../common/LoadingSpin";


class MonitoringConfigurationList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            configurations: [],
            availableNewParameters: [],
            page: 0,
            size: AGENT_SERVICES_CONFIGURATION_LIST_SIZE,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
        this.loadConfigurationsList = this.loadConfigurationsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }


    loadConfigurationsList(page = 0, size = AGENT_SERVICES_CONFIGURATION_LIST_SIZE) {
        let availableNewParametersPromise = loadNewAvailableMonitoringParameters(this.props.serviceId);
        let configurationPromise = getAgentServicesMonitoringConfigurationsList(this.props.serviceId, page, size);

        if (!availableNewParametersPromise || !configurationPromise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        availableNewParametersPromise
            .then(response => {
                this.state.availableNewParameters.slice();
                this.setState({
                    availableNewParameters: response,
                })
            }).catch(error => {
        });

        configurationPromise
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
                availableNewParameters: [],
                page: 0,
                size: AGENT_SERVICES_CONFIGURATION_LIST_SIZE,
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
            {title: 'Nazwa parametru', dataIndex: 'parameterName', key: 'parameterName'},
            {title: 'Opis', dataIndex: 'description', key: 'description'},
            {title: 'Odstęp czasowy monitorowania[ms]', dataIndex: 'monitoringInterval', key: 'monitoringInterval'},

            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="service-operation">
                        <a href={"/agents/service/monitoring/edit/" + record.key}><Icon
                            type="edit"/></a>
                        <a title="Usuń"
                           onClick={() => handleConfigurationDeleteClick(this.refresh, record.key, "monitoring")}><Icon
                            type="delete"/></a>
                    </span>
            }
        ];

        const data = [];
        this.state.configurations.forEach((configuration, index) => {
            data.push({
                key: configuration.id,
                parameterName: configuration.parameterName,
                description: configuration.description,
                monitoringInterval: configuration.monitoringInterval
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
                                onShowSizeChange: ((current, size) => this.loadConfigurationsList(current - 1, size)),
                                onChange: ((current, size) => this.loadConfigurationsList(current - 1, size))
                            }}/>
                        {(this.props.editAccess && this.state.availableNewParameters.length != 0) && (
                            <Button type="primary"
                                    href={"/agents/service/" + this.props.serviceId + "/monitoring/create"}>
                                Dodaj nową konfigurację
                            </Button>
                        )}
                    </div>
                ) : (
                    <div>
                        <h3>Brak konfiguracji dla wybranego agenta</h3>
                        {(this.props.editAccess && this.state.availableNewParameters.length != 0) && (
                            <Button type="primary"
                                    href={"/agents/service/" + this.props.serviceId + "/monitoring/create"}>
                                Dodaj pierwszą konfigurację
                            </Button>
                        )}
                    </div>
                )
            )
        )
    }
}


export default MonitoringConfigurationList;