import React, {Component} from 'react';
import './AgentsList.css';
import {Row} from 'antd/lib/index';
import {Button, Icon, Table} from 'antd';
import {AGENT_LIST_SIZE} from "../../configuration";
import {getAgentsList} from "../../utils/APIRequestsUtils";
import LoadingSpin from '../../common/LoadingSpin';


class AgentsList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            agents: [],
            page: 0,
            size: 10,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
        this.loadAgentsList = this.loadAgentsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
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
                size: 10,
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

    handleAgentEditClick(event, index) {

    }

    handleAgentDeleteClick(event, index) {

    }

    handlePaginationChange = e => {
        const {value} = e.target;
        this.setState({
            pagination: value === 'none' ? false : {position: value},
        });
    };

    resolveStatus(isRegistered, isActive, isConnected) {
        // TODO: implement 2 other parameters
        if (isRegistered === true) {
            return 'Zarejestrowany'
        } else {
            return 'Nowy'
        }
    }

    render() {
        const state = this.state;
        const expandedRowRender = () => {
            const columns = [
                {title: 'Nazwa serwisu', dataIndex: 'serviceName', key: 'serviceName'},
                {title: 'Opis', dataIndex: 'description', key: 'description'},
                {
                    title: 'Akcje', key: 'operation', render: () => <span className="agent-operation">
            <a href="javascript:;"><Icon type="edit"/></a>    <a href="javascript:"><Icon type="delete"/></a>
          </span>
                }
            ];

            const data = [];
            for (let i = 0; i < 1; ++i) {
                data.push({
                    key: i,
                    serviceName: 'Payments Service',
                    description: 'Monitorowanie serwisu odpowiedzialnego za płatności',
                });
                data.push({
                    key: i + 1,
                    serviceName: 'Serwer Service',
                    description: 'Monitorowanie paramtrów serwera',
                });
            }
            return (
                <div>
                    <h3>Lista serwisów agenta</h3>
                    <Table columns={columns} dataSource={data} pagination={false}/>
                </div>);

        };

        const columns = [
            {title: 'Nazwa agenta', dataIndex: 'name', key: 'name'},
            {title: 'Identyfikator', dataIndex: 'key', key: 'key'},
            {title: 'Status', dataIndex: 'status', key: 'status'},
            {title: 'Utworzył', dataIndex: 'creator', key: 'creator'},
            {
                title: 'Akcje', key: 'operation', render: () => <span className="agent-operation">
            <a href="javascript:"><Icon type="edit"/></a>    <a href="javascript:;"><Icon type="delete"/></a>
          </span>
            }
        ];

        const data = [];
        this.state.agents.forEach((agent, index) => {
            data.push({
                key: agent.agentId,
                name: agent.name,
                status: this.resolveStatus(agent.registered),
                creator: 'Patryk Milewski',
                //handleAgentEditClick={(event) => this.handleAgentEditClick(event, pollIndex)}
                //handleAgentDeleteClick={(event) => this.andleAgentDeleteClick(event, pollIndex)}
            });

        });
        return (
            <div className="welcome-container">
                <div className="welcome-content">
                    <Row gutter={16} className="welcome-top-content">
                        <div style={{marginBottom: 16, marginRight: 16}}>
                            <Button type="primary" href={"/agents/create"}
                                //    onClick={}
                                /*loading={loading}*/ >
                                Dodaj nowego agneta
                            </Button>
                        </div>

                        {state.isLoading && <div>Trwa wczytywanie danych <LoadingSpin/></div>}

                        <Table
                            className="components-table-demo-nested"
                            columns={columns}
                            expandedRowRender={expandedRowRender}
                            dataSource={data}
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
            </div>
        );
    }
}


export default AgentsList;