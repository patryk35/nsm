import React, {Component} from 'react';
import './AgentServicesList.css';
import {Button, Icon, notification, Table} from 'antd';
import {AGENT_SERVICES_LIST_SIZE} from "../../../configuration";
import {getAgentServicesList} from "../../../utils/APIRequestsUtils";
import {handleAgentServiceDeleteClick} from "../shared/ServiceShared";
import {Link} from "react-router-dom";
import {getCurrentUser} from "../../../utils/SharedUtils";


class AgentServicesList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            services: [],
            page: 0,
            size: AGENT_SERVICES_LIST_SIZE,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
        this.loadServicesList = this.loadServicesList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }


    loadServicesList(page = 0, size = AGENT_SERVICES_LIST_SIZE) {
        let promise = getAgentServicesList(this.props.agentId, page, size);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.state.services.slice();
                this.setState({
                    services: response.content,
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
        this.loadServicesList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                services: [],
                page: 0,
                size: AGENT_SERVICES_LIST_SIZE,
                totalElements: 0,
                totalPages: 0,
                last: true,
                isLoading: false
            });
            this.loadServicesList();
        }
    }

    handleLoadMore() {
        this.loadServicesList(this.state.page + 1);
    }


    refresh = () => {
        this.loadServicesList(this.state.page);
    };

    render() {
        const state = this.state;
        const columns = [
            {title: 'Identyfikator', dataIndex: 'id', key: 'id'},
            {title: 'Nazwa serwisu', dataIndex: 'name', key: 'name'},
            {title: 'Opis', dataIndex: 'description', key: 'description'},
            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="service-operation">
                        <Link
                            to={"agents/" + this.props.agentId + "/" + this.props.agentName + "/service/details/" + record.id}>
                            <Icon className="agent-services-list-menu-item"
                                  title="Szczegóły"
                                  type="unordered-list"/></Link>
                        {(getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") || getCurrentUser().roles.includes("ROLE_OPERATOR")) &&
                        <Link
                            to={"agents/" + this.props.agentId + "/" + this.props.agentName + "/service/edit/" + record.id}>
                            <Icon className={"agent-services-list-menu-item"}
                                  title={"Edytuj"}
                                  type="edit"/></Link>
                        }
                        {(getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") || getCurrentUser().roles.includes("ROLE_OPERATOR")) && record.systemService === false &&
                        <a onClick={() => handleAgentServiceDeleteClick(this.refresh, record.id, record.name)}>
                            <Icon
                                className="agent-services-list-menu-item"
                                title="Usuń"
                                type="delete"/></a>
                        }
                    </span>
            }
        ];

        const data = [];
        this.state.services.forEach((service, index) => {
            data.push({
                key: index,
                id: service.serviceId,
                name: service.name,
                description: service.description,
                systemService: service.systemService
            });

        });
        return (
            (this.state.isLoading || data.length !== 0) ? (
                <div>
                    <h3>Lista serwisów agenta</h3>
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
                            onShowSizeChange: ((current, size) => this.loadServicesList(current - 1, size)),
                            onChange: ((current, size) => this.loadServicesList(current - 1, size))
                        }}/>
                    {getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") &&
                    <Button type="primary" className={"services-list-button-above-list"}>
                        <Link to={"agents/" + this.props.agentId + "/" + this.props.agentName + "/service/create"}>Dodaj
                            nowy serwis</Link>
                    </Button>
                    }
                </div>
            ) : (
                <div>
                    <h3>Brak serwisów dla wybranego agenta</h3>
                    {getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") &&
                    <Button type="primary">
                        <Link to={"agents/" + this.props.agentId + "/" + this.props.agentName + "/service/create"}>Dodaj
                            pierwszy serwis</Link>
                    </Button>
                    }

                </div>
            ));
    }


}


export default AgentServicesList;