import React, {Component} from 'react';
import './AgentErrorsList.css';
import {Icon, notification, Table} from 'antd';
import {ALERTS_LIST_SIZE} from "../../../configuration";
import {getAgentErrors, getUserAlertsList} from "../../../utils/APIRequestsUtils";
import {convertDate} from "../../../utils/SharedUtils";
import {genIcon} from "../shared/SharedFunctions";


class AgentErrorsList extends Component {
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
        let configurationPromise = getAgentErrors(page, size);
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
            {title: 'Czas', dataIndex: 'timestamp', key: 'timestamp'},
            {title: 'Agent', dataIndex: 'agent', key: 'agent'},
            {title: 'Błąd', dataIndex: 'message', key: 'message'}
        ];

        const data = [];
        this.state.alerts.forEach((alert) => {
            data.push({
                key: alert.id,
                timestamp: convertDate(alert.timestamp),
                message: alert.message,
                agent: alert.agent.name
            });

        });

        return (
            <Table
                scroll={{x: true}}
                loading={this.state.isLoading}
                locale={{
                    emptyText: "Brak alertów"
                }}
                columns={columns}
                dataSource={data}
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


export default AgentErrorsList;