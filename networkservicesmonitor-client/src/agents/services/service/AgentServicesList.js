import React, {Component} from 'react';
import './AgentServicesList.css';
import {Button, Icon, Table} from 'antd';
import {AGENT_SERVICES_LIST_SIZE} from "../../../configuration";
import {getAgentServicesList} from "../../../utils/APIRequestsUtils";


class AgentServicesList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            services: [],
            page: 0,
            size: 10,
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
                const services = this.state.services.slice();
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
            })
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
                size: 10,
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


    render() {
        const state = this.state;
        const columns = [
            {title: 'Id', dataIndex: 'id', key: 'id'},
            {title: 'Nazwa serwisu', dataIndex: 'name', key: 'name'},
            {title: 'Opis', dataIndex: 'description', key: 'description'},
            {
                title: 'Akcje', key: 'operation', render: () => <span className="service-operation">
            <a href="javascript:;"><Icon type="edit"/></a>    <a href="javascript:"><Icon type="delete"/></a>
          </span>
            }
        ];

        const data = [];
        this.state.services.forEach((service, index) => {
            data.push({
                key: index,
                id: service.serviceId,
                name: service.name,
                description: service.description
                //handleAgentEditClick={(event) => this.handleAgentEditClick(event, pollIndex)}
                //handleAgentDeleteClick={(event) => this.andleAgentDeleteClick(event, pollIndex)}
            });

        });
        return (
            data.length !== 0 ? (
                <div>
                    <h3>Lista serwisów agenta</h3>
                    <Table
                        columns={columns}
                        dataSource={data}
                        pagination={{
                            current: state.page + 1,
                            defaultPageSize: state.size,
                            hideOnSinglePage: true,
                            total: state.totalElements,
                            onShowSizeChange: ((current, size) => this.loadServicesList(current - 1, size)),
                            onChange: ((current, size) => this.loadServicesList(current - 1, size))
                        }}/>
                    <Button type="primary" href={"/agents/create"}
                        //    onClick={}
                        /*loading={loading}*/ >
                        Dodaj nowy serwis
                    </Button>
                </div>
            ) : (
                <div>
                    <h3>Brak serwisów dla wybranego agenta</h3>
                    <Button type="primary" href={"/agents/create"}
                        //    onClick={}
                        /*loading={loading}*/ >
                        Dodaj pierwszy serwis
                    </Button>
                </div>
            ));
    }


}


export default AgentServicesList;