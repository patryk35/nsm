import React, {Component} from 'react';
import './Dashboard.css';
import {Button, Col, Drawer, Row, Table} from "antd";
import LogsAlertsList from "./logsList/LogsAlertsList";
import MonitoringAlertsList from "./monitoringList/MonitoringAlertsList";
import UsersAlertsList from "./userList/UsersAlertsList";
import {getLogAlert, getMonitoringAlert, getUserAlert} from "../../../utils/APIRequestsUtils";
import {Link} from "react-router-dom";

class Dashboard extends Component {
    state = {
        visible: false,
        isLoading: true,
        data: []
    };

    formatLog = (logResponse) => {
        let log = [];
        log.push({key: 0, name: "Czas pojawienia się logu", value: logResponse["timestamp"]});
        log.push({key: 1, name: "Wartość logu", value: logResponse["log"]});
        log.push({key: 2, name: "Wiadomość", value: logResponse["message"]});
        log.push({key: 3, name: "Ścieżka", value: logResponse["pathSearchString"]});
        log.push({key: 4, name: "Fraza logu", value: logResponse["searchString"]});
        log.push({key: 5, name: "Agent", value: logResponse["agentName"]});
        log.push({key: 6, name: "Serwis", value: logResponse["serviceName"]});

        return log;
    };

    formatMonitoring = (monitoringResponse) => {
        let monitoring = [];
        monitoring.push({key: 0, name: "Czas pojawienia się wartości", value: monitoringResponse["timestamp"]});
        monitoring.push({key: 0, name: "Zmierzona wartość", value: monitoringResponse["measuredValue"]});
        monitoring.push({key: 0, name: "Wiadomość", value: monitoringResponse["message"]});
        monitoring.push({key: 0, name: "Typ parametru", value: monitoringResponse["parameterTypeName"]});
        monitoring.push({key: 0, name: "Warunek", value: monitoringResponse["condition"]});
        monitoring.push({key: 0, name: "Wartość dla warunku", value: monitoringResponse["limitValue"]});
        monitoring.push({key: 0, name: "Agent", value: monitoringResponse["agentName"]});
        monitoring.push({key: 0, name: "Serwis", value: monitoringResponse["serviceName"]});
        return monitoring;
    };

    formatUser = (userResponse) => {
        let user = [];
        user.push({key: 0, name: "Adres email", value: userResponse["email"]});
        user.push({key: 1, name: "Imię i Nazwisko", value: userResponse["fullname"]});
        user.push({key: 2, name: "Login", value: userResponse["username"]});
        user.push({key: 3, name: "Wiadomość", value: userResponse["message"]});
        user.push({key: 4, name: "Czas zdarzenia", value: userResponse["timestamp"]});

        return user;
    };

    showDrawer = (id, type) => {
        this.setState({
            visible: true,
            isLoading: true
        });

        let promise = null;
        if (type == "log") {
            promise = getLogAlert(id);
        } else if (type == "monitoring") {
            promise = getMonitoringAlert(id);
        } else if (type == "user") {
            promise = getUserAlert(id);
        }

        if (!promise) {
            return;
        }

        promise
            .then(response => {
                let formattedData = [];
                if (type == "log") {
                    formattedData = this.formatLog(response);
                } else if (type == "monitoring") {
                    formattedData = this.formatMonitoring(response);
                } else if (type == "user") {
                    formattedData = this.formatUser(response);
                }

                this.setState({
                    data: formattedData,
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                data: [],
                isLoading: false
            })
        });

    };

    onClose = () => {
        this.setState({
            visible: false,
        });
    };

    onChange = e => {
        this.setState({
            placement: e.target.value,
        });
    };

    render() {
        const columns = [
            {title: 'Nazwa', dataIndex: 'name', key: 'name'},
            {title: 'Wartość', dataIndex: 'value', key: 'value'}
        ];
        return (
            <div>
                <Row>
                    <Col span={4}>
                        <article className="alert-dashboard-container">
                            Users: TODO
                        </article>
                    </Col>
                    <Col span={4}>
                        <article className="alert-dashboard-container">
                            Alerts: TODO
                        </article>
                    </Col>
                    <Col span={4}>
                        <article className="alert-dashboard-container">
                            Agents: TODO
                        </article>
                    </Col>
                    <Col span={4}>
                        <article className="alert-dashboard-container">
                            Logs: TODO
                        </article>
                    </Col>
                    <Col span={4}>
                        <article className="alert-dashboard-container">
                            Monitoring entries: TODO
                        </article>
                    </Col>
                    <Col span={4}>
                        <article className="alert-dashboard-container">
                            Other: TODO
                        </article>
                    </Col>

                </Row>
                <article className="alert-dashboard-container">
                    <h1>Alerty o użytkownikach</h1>
                    <Row className="alert-dashboard-content">
                        <UsersAlertsList showDrawer={this.showDrawer}></UsersAlertsList>
                    </Row>
                </article>
                <Row>
                    <Col span={12}>
                        <article className="alert-dashboard-container ">
                            <h1>Alerty o zebranych logach</h1>
                            <Row className="alert-dashboard-content">
                                <LogsAlertsList showDrawer={this.showDrawer}></LogsAlertsList>
                            </Row>
                            <Button type="primary">
                                <Link to={"/alerts/configuration/list/logs"}>Konfiguracje alertów</Link>
                            </Button>
                        </article>
                    </Col>
                    <Col span={12}>
                        <article className="alert-dashboard-container ">
                            <h1>Alerty o monitorowanych parametrach</h1>
                            <Row className="alert-dashboard-content">
                                <MonitoringAlertsList showDrawer={this.showDrawer}></MonitoringAlertsList>
                            </Row>
                            <Button type="primary">
                                <Link to={"/alerts/configuration/list/monitoring"}>Konfiguracje alertów</Link>
                            </Button>
                        </article>
                    </Col>
                </Row>

                <Drawer
                    placement={"bottom"}
                    closable={false}
                    onClose={this.onClose}
                    visible={this.state.visible}
                    height={350}
                    destroyOnClose={true}
                >
                    <Table
                        tableLayout={"fixed"}
                        columns={columns}
                        showHeader={false}
                        scroll={{y: 300}}
                        pagination={false}
                        dataSource={this.state.data}
                        loading={this.state.isLoading}
                        locale={{
                            emptyText: "Brak danych"
                        }}
                        bordered={true}
                        size={"small"}
                        className={"alert-dashboard-drawer-table"}
                    />
                </Drawer>
            </div>
        );
    }
}


export default Dashboard;