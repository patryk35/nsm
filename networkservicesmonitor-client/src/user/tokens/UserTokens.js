import React, {Component} from 'react';
import './UserTokens.css';
import {notification, Row} from 'antd/lib/index';
import {Button, Dropdown, Icon, Menu, Table} from 'antd';
import {USER_LIST_SIZE} from "../../configuration";
import {
    activateUser,
    addAdminAccess, addOperatorAccess, deleteLogsConfiguration, deleteMonitoringConfiguration, deleteUserToken,
    disableUser,
    enableUser,
    getUsersList, getUserTokens,
    removeAdminAccess, removeOperatorAccess
} from "../../utils/APIRequestsUtils";
import {getCurrentUser} from "../../utils/SharedUtils";
import {Link} from "react-router-dom";
import {
    handleConfigurationDeleteClick,
    openNotificationWithIcon
} from "../../agents/services/shared/ConfigurationShared";


class UserTokens extends Component {
    constructor(props) {
        super(props);
        this.state = {
            tokens: [],
            last: true,
            isLoading: false
        };
        this.loadTokensList = this.loadTokensList.bind(this);
        notification.config({
            placement: 'topLeft',
            top: 70,
            duration: 5,
        });
    }


    loadTokensList() {
        let promise = getUserTokens();

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                const tokens = this.state.tokens.slice();
                this.setState({
                    tokens: response,
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
        this.loadTokensList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                tokens: [],
                isLoading: false
            });
            this.loadTokensList();
        }
    }

    executeDelete = (id) => {
        let promise;
        promise = deleteUserToken(id);

        if (!promise) {
            return;
        }

        promise
            .then(() => {
                openNotificationWithIcon('success', 'Pomyślnie usunięto', 'Token został usunięty');
                this.loadTokensList();
            }).catch(error => {
                openNotificationWithIcon('error', 'Nie udało się usunąć tokenu!', 'Spróbuj ponownie później')
            }
        );
    };


    handleTokenDeleteClick = (id, name) => {
        const key = `open${Date.now()}`;
        const btn = (
            <Button type="primary" size="large" className="agent-list-delete-button"
                    onClick={() => {
                        notification.close(key);
                        this.executeDelete(id);
                    }}>
                Potwierdź
            </Button>
        );
        notification.open({
            message: 'Usuń token',
            description:
                'Token ' + name + " zostanie usunięty",
            btn,
            key
        });
    };

    render() {
        const state = this.state;
        const columns = [
            {title: 'Nazwa', dataIndex: 'name', key: 'name'},
            {title: 'Czas wygaśnięcia', dataIndex: 'expirationTime', key: 'expirationTime'},
            {title: 'Dozwolone metody', dataIndex: 'methods', key: 'methods'},
            {title: 'Dozwolone endpointy', dataIndex: 'endpoints', key: 'endpoints'},
            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="service-operation">
                        <a onClick={() => this.handleTokenDeleteClick(record.id, record.name)}>
                            <Icon
                                title="Usuń"
                                type="delete"/></a>

                    </span>
            }

        ];

        const data = [];
        this.state.tokens.forEach((token, index) => {
            data.push({
                name: token.name,
                expirationTime: token.expirationTime,
                methods: token.allowedMethods,
                endpoints: token.allowedEndpoints,
                id: token.id
            });

        });
        return (
            <article className="token-list-container">
                <div style={{marginBottom: 16, marginRight: 16}}>
                    <Button type="primary">
                        <Link to={"/users/tokens/add"}>Dodaj nowy token</Link>
                    </Button>

                </div>
                        <Row gutter={16}>
                            <Table
                                columns={columns}
                                dataSource={data}
                                loading={this.state.isLoading}
                                locale={{
                                    emptyText: "Brak danych"
                                }}
                                pagination={false}
                            />
                        </Row>
            </article>
        );
    }
}


export default UserTokens;