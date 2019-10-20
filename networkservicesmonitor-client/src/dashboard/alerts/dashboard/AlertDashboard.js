import React, {Component} from 'react';
import './AlertDashboard.css';
import {Button, Drawer, Row} from "antd";
import LogsAlertsList from "./logsList/LogsAlertsList";
import MonitoringAlertsList from "./monitoringList/MonitoringAlertsList";
import UsersAlertsList from "./userList/UsersAlertsList";

class AlertDashboard extends Component {
    render() {
        return (
            <div>
                <article className="agent-details-service-container">
                    <h1>Alerty o zebranych logach</h1>
                    <Row gutter={16} className="welcome-top-content">
                        <LogsAlertsList></LogsAlertsList>
                    </Row>
                    <Button type="primary"
                            href={"/agents/service/" + this.props.serviceId + "/monitoring/create"}>
                        Konfiguracje alertów
                    </Button>
                </article>
                <article className="agent-details-service-container">
                    <h1>Alerty o monitorowanych parametrach</h1>
                    <Row gutter={16} className="welcome-top-content">
                        <MonitoringAlertsList></MonitoringAlertsList>
                    </Row>
                    <Button type="primary"
                            href={"/agents/service/" + this.props.serviceId + "/monitoring/create"}>
                        Konfiguracje alertów
                    </Button>
                </article>
                <article className="agent-details-service-container">
                    <h1>Alerty o użytkownikach</h1>
                    <Row gutter={16} className="welcome-top-content">
                        <UsersAlertsList></UsersAlertsList>
                    </Row>
                </article>
            </div>
        );
    }
}


export default AlertDashboard;