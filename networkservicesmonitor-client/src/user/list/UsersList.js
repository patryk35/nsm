import React, {Component} from 'react';
import './UsersList.css';
import {notification, Row} from 'antd/lib/index';
import {Button, Icon, Table} from 'antd';
import {USER_LIST_SIZE} from "../../configuration";
import {activateUser, deactivateUser, getUsersList} from "../../utils/APIRequestsUtils";
import LoadingSpin from '../../common/LoadingSpin';


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


    handlePaginationChange = e => {
        const {value} = e.target;
        this.setState({
            pagination: value === 'none' ? false : {position: value},
        });
    };

    refresh = () => {
        this.loadUsersList(this.state.page);
    };

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

    deactivate = (id) => {
        let promise = deactivateUser(id);

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
                    message: 'Dezaktywowano użytkownika!',
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
            {title: 'Status', dataIndex: 'status', key: 'status'},

            {
                title: 'Akcje', key: 'operation', render: (text, record) =>
                    <span className="agent-operation">
                        {record.status !== "Aktywny" ? (
                            <a onClick={() => this.activate(record.key)} title={"Aktywuj"}><Icon type="check"/></a>
                        ) : (
                            <a onClick={() => this.deactivate(record.key)} title={"Dezktywuj"}><Icon type="close"/></a>
                        )}
                    </span>
            }
        ];

        const data = [];
        this.state.users.forEach((user, index) => {
            data.push({
                key: user.id,
                fullname: user.fullname,
                login: user.username,
                email: user.email,
                status: user.isEnabled ? "Aktywny" : "Nieaktywny"
            });

        });
        return (
            <div className="welcome-container">
                <div className="welcome-content">
                    <Row gutter={16} className="welcome-top-content">
                        {state.isLoading && <div>Trwa wczytywanie danych <LoadingSpin/></div>}

                        <Table
                            className="components-table-demo-nested"
                            columns={columns}
                            dataSource={data}
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
}


export default UsersList;