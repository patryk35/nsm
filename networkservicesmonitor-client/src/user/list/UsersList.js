import React, {Component} from 'react';
import './UsersList.css';
import {notification, Row} from 'antd/lib/index';
import {Button, Dropdown, Icon, Menu, Table} from 'antd';
import {USER_LIST_SIZE} from "../../configuration";
import {
    activateUser,
    addAdminAccess,
    disableUser,
    enableUser,
    getUsersList,
    removeAdminAccess
} from "../../utils/APIRequestsUtils";


class UsersList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            users: [],
            page: 0,
            size: USER_LIST_SIZE,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
        this.loadUsersList = this.loadUsersList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
        notification.config({
            placement: 'topLeft',
            top: 70,
            duration: 5,
        });
    }


    loadUsersList(page = 0, size = USER_LIST_SIZE) {
        let promise = getUsersList(page, size);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                const users = this.state.users.slice();
                this.setState({
                    users: response.content,
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
        this.loadUsersList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                users: [],
                page: 0,
                size: USER_LIST_SIZE,
                totalElements: 0,
                totalPages: 0,
                last: true,
                isLoading: false
            });
            this.loadUsersList();
        }
    }

    handleLoadMore() {
        this.loadUsersList(this.state.page + 1);
    }

    activate = (id) => {
        let promise = activateUser(id);

        if (!promise) {
            return;
        }

        promise
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Aktywowano użytkownika!',
                    btn,
                    key
                });
                this.loadUsersList(this.state.page);
            }).catch(() => {
            const key = `open${Date.now()}`;
            const btn = (
                <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
            );
            notification.error({
                message: 'Wystąpił błąd. Spróbuj ponownie później!',
                btn,
                key
            });
        })
    };

    disable = (id) => {
        let promise = disableUser(id);
        if (!promise) {
            return;
        }

        promise
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Wyłączono użytkownika!',
                    btn,
                    key
                });
                this.loadUsersList(this.state.page);
            }).catch(() => {
            const key = `open${Date.now()}`;
            const btn = (
                <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
            );
            notification.error({
                message: 'Wystąpił błąd. Spróbuj ponownie później!',
                btn,
                key
            });
        })
    };

    enable = (id) => {
        let promise = enableUser(id);
        if (!promise) {
            return;
        }

        promise
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Włączono użytkownika!',
                    btn,
                    key
                });
                this.loadUsersList(this.state.page);
            }).catch(() => {
            const key = `open${Date.now()}`;
            const btn = (
                <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
            );
            notification.error({
                message: 'Wystąpił błąd. Spróbuj ponownie później!',
                btn,
                key
            });
        })
    };

    addAdminAccess = (id) => {
        let promise = addAdminAccess(id);
        if (!promise) {
            return;
        }

        promise
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Dodano uprawnienia administratorskie!',
                    btn,
                    key
                });
                this.loadUsersList(this.state.page);
            }).catch(() => {
            const key = `open${Date.now()}`;
            const btn = (
                <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
            );
            notification.error({
                message: 'Wystąpił błąd. Spróbuj ponownie później!',
                btn,
                key
            });
        })
    };

    removeAdminAccess = (id) => {
        let promise = removeAdminAccess(id);
        if (!promise) {
            return;
        }

        promise
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Usunięto uprawnienia administratorskie!',
                    btn,
                    key
                });
                this.loadUsersList(this.state.page);
            }).catch(() => {
            const key = `open${Date.now()}`;
            const btn = (
                <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
            );
            notification.error({
                message: 'Wystąpił błąd. Spróbuj ponownie później!',
                btn,
                key
            });
        })
    };

    render() {
        const state = this.state;
        const columns = [
            {title: 'Imię i nazwisko', dataIndex: 'fullname', key: 'fullname'},
            {title: 'Login', dataIndex: 'login', key: 'login'},
            {title: 'E-mail', dataIndex: 'email', key: 'email'},
            {title: 'Rola', dataIndex: 'role', key: 'role'},
            {title: 'Status', dataIndex: 'status', key: 'status'},

            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <Dropdown disabled={!record.emailVerified || (record.login === this.props.currentUser.username)}
                              overlay={() => (
                                  <Menu>
                                      {!record.activated && record.emailVerified &&
                                      <Menu.Item>
                                          <a title="Aktywuj"
                                             onClick={() => this.activate(record.key)}>Aktywuj</a>
                                      </Menu.Item>
                                      }
                                      {(!record.enabled && record.activated) &&
                                      <Menu.Item>
                                          <a title="Nadaj dostęp" onClick={() => this.enable(record.key)}>Nadaj
                                              dostęp</a>
                                      </Menu.Item>
                                      }
                                      {(record.enabled && record.activated) &&
                                      <Menu.Item>
                                          <a title="Zabierz dostęp" onClick={() => this.disable(record.key)}>Zabierz
                                              dostęp</a>
                                      </Menu.Item>
                                      }
                                      {(record.role !== "Administrator") && record.activated &&
                                      <Menu.Item>
                                          <a title="Mianuj administratorem"
                                             onClick={() => this.addAdminAccess(record.key)}>Mianuj
                                              administratorem</a>
                                      </Menu.Item>
                                      }
                                      {record.role === "Administrator" && record.activated &&
                                      <Menu.Item>
                                          <a title="Odbierz dostęp administratorski"
                                             onClick={() => this.removeAdminAccess(record.key)}>Odbierz dostęp
                                              administratorski</a>
                                      </Menu.Item>
                                      }
                                  </Menu>
                              )}
                              placement="bottomRight">
                        <Button>
                            <Icon type="menu"/>
                        </Button>
                    </Dropdown>
            }
        ];

        const data = [];
        this.state.users.forEach((user, index) => {
            let role = "Użytkownik";
            user.roles.forEach((r) => {
                if (r.name === "ROLE_ADMINISTRATOR") {
                    role = "Administrator"
                }
            });
            console.log(user.login);
            console.log(this.props.currentUser)
            data.push({
                key: user.id,
                fullname: user.fullname,
                login: user.username,
                email: user.email,
                role: role,
                emailVerified: user.emailVerified,
                activated: user.activated,
                enabled: user.enabled,
                status: this.resolveUserStatus(user.emailVerified, user.activated, user.enabled)
            });

        });
        return (
            <div className="users-list-container">
                <div>
                    <Row gutter={16}>
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
                                onShowSizeChange: ((current, size) => this.loadUsersList(current - 1, size)),
                                onChange: ((current, size) => this.loadUsersList(current - 1, size))
                            }}
                        />
                    </Row>
                </div>
            </div>
        );
    }

    resolveUserStatus = (emailVerified, activated, enabled) => {
        if (!emailVerified) {
            return "Nowy(Oczekiwanie na potwierdzenie adresu email)";
        } else if (!activated) {
            return "Nowy(Adres email potwierdzony)"
        } else if (enabled) {
            return "Aktywny"
        } else {
            return "Nieaktywny"
        }
    }
}


export default UsersList;