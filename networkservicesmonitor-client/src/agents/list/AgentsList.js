import React, {Component} from 'react';
import './AgentsList.css';
import {Row} from 'antd/lib/index';
import {Button, Icon, Table} from 'antd';
import {AGENT_LIST_SIZE} from "../../configuration";
import {getAgentsList} from "../../utils/APIRequestsUtils";
import AgentServicesList from "../services/list/AgentServicesList";
import {handleAgentDeleteClick} from "../shared/AgentShared";
import {Link} from "react-router-dom";
import {getCurrentUser} from "../../utils/SharedUtils";


class AgentsList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            agents: [],
            page: 0,
            size: AGENT_LIST_SIZE,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
        this.loadAgentsList = this.loadAgentsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }

    resolveStatus(isRegistered, isActive, isConnected) {
        if (isRegistered === true) {
            return 'Zarejestrowany'
        } else {
            return 'Nowy'
        }
    }

    loadAgentsList(page = 0, size = AGENT_LIST_SIZE) {
        let promise = getAgentsList(page, size);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                const agents = this.state.agents.slice();
                this.setState({
                    agents: response.content,
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
        this.loadAgentsList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                agents: [],
                page: 0,
                size: AGENT_LIST_SIZE,
                totalElements: 0,
                totalPages: 0,
                last: true,
                isLoading: false
            });
            this.loadAgentsList();
        }
    }

    handleLoadMore() {
        this.loadAgentsList(this.state.page + 1);
    }

    refresh = () => {
        this.loadAgentsList(this.state.page);
    };

    render() {
        const state = this.state;
        const expandedRowRender = (record) => {
            return (
                <AgentServicesList agentId={record.key} agentName={record.name}></AgentServicesList>
            )
        };

        const columns = [
            {title: 'Nazwa agenta', dataIndex: 'name', key: 'name'},
            {title: 'Identyfikator', dataIndex: 'key', key: 'key'},
            {title: 'Status', dataIndex: 'status', key: 'status'},
            {title: 'Agent Proxy', dataIndex: 'proxy', key: 'proxy'},
            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="agent-operation">
                        <Link to={"agents/details/" + record.key}>
                            <Icon className={"agent-list-menu-item"}
                                  title={"Szczegóły"}
                                  type="unordered-list"/></Link>
                        {getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") &&
                        <Link to={"agents/edit/" + record.key}>
                            <Icon className={"agent-list-menu-item"}
                                  title={"Edytuj"}
                                  type="edit"/></Link>
                        }
                        {getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") &&
                        <a
                            onClick={() => handleAgentDeleteClick(this.refresh, record.key, record.name)}
                            className={"agent-list-menu-item"} title={"Usuń"}><Icon
                            type="delete"/></a>
                        }
                    </span>
            }
        ];

        const data = [];
        this.state.agents.forEach((agent, index) => {
            data.push({
                key: agent.agentId,
                name: agent.name,
                status: this.resolveStatus(agent.registered),
                proxy: agent.proxyAgent ? "Tak" : "Nie",
                services: null,
            });

        });
        return (
            <div className="users-list-container">
                <Row gutter={16}>
                    <div style={{marginBottom: 16, marginRight: 16}}>
                        {getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") &&
                        <Button type="primary">
                            <Link to={"/agents/create"}>Dodaj nowego agneta</Link>
                        </Button>
                        }
                    </div>

                    <Table
                        columns={columns}
                        expandedRowRender={record => expandedRowRender(record)}
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
                            onShowSizeChange: ((current, size) => this.loadAgentsList(current - 1, size)),
                            onChange: ((current, size) => this.loadAgentsList(current - 1, size))
                        }}
                    />
                </Row>
            </div>
        );
    }
}


export default AgentsList;