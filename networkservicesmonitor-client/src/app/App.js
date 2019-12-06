import React, {Component} from 'react';
import './App.css';
import {Route, Switch, withRouter} from 'react-router-dom';

import {getCurrentUser} from '../utils/APIRequestsUtils';
import {ACCESS_TOKEN} from '../configuration';

import Login from '../user/login/Login';
import Register from '../user/register/Register';
import AppHeader from '../common/layout/AppHeader';
import AppFooter from '../common/layout/AppFooter';
import NotFound from '../common/error_pages/NotFound';
import {library} from '@fortawesome/fontawesome-svg-core'
import {faExclamationTriangle, faInfo, faSignInAlt, faTimes} from '@fortawesome/free-solid-svg-icons'


import {Layout, notification, Spin} from 'antd';
import AgentsList from "../agents/list/AgentsList";
import AgentCreate from "../agents/create/AgentCreate";
import LogsViewer from "../logs/LogsViewer";
import Charts from "../charts/Charts";
import AgentEdit from "../agents/edit/AgentEdit";
import ServiceCreate from "../agents/services/create/ServiceCreate";
import AgentDetails from "../agents/details/AgentDetails";
import ServiceEdit from "../agents/services/edit/ServiceEdit";
import ServiceDetails from "../agents/services/details/ServiceDetails";
import CreateMonitoringConfiguration
    from "../agents/services/monitoringConfiguration/create/CreateMonitoringConfiguration";
import CreateLogsConfiguration from "../agents/services/logsConfiguration/create/CreateLogsConfiguration";
import EditLogsConfiguration from "../agents/services/logsConfiguration/edit/EditLogsConfiguration";
import EditMonitoringConfiguration from "../agents/services/monitoringConfiguration/edit/EditMonitoringConfiguration";
import UsersList from "../user/list/UsersList";
import Edit from "../user/edit/Edit";
import Dashboard from "../dashboard/alerts/Dashboard";
import LogsAlertCreate from "../alerts/configuration/logs/create/LogsAlertCreate";
import MonitoringAlertCreate from "../alerts/configuration/monitoring/create/MonitoringAlertCreate";
import MonitoringAlertEdit from "../alerts/configuration/monitoring/edit/MonitoringAlertEdit";
import LogsAlertEdit from "../alerts/configuration/logs/edit/LogsAlertEdit";
import MonitoringAlertsConfigList from "../alerts/configuration/monitoring/list/MonitoringAlertsConfigList";
import LogsAlertsConfigList from "../alerts/configuration/logs/list/LogsAlertsConfigList";
import PasswordReset from "../user/passwordReset/PasswordReset";
import PasswordResetConfirm from "../user/passwordResetConfirm/PasswordResetConfirm";
import InfoCallback from "../user/infoCallback/InfoCallback";
import LoadingSpin from "../common/spin/LoadingSpin";
import PrivateRoute from "../common/PrivateRoute";
import Unauthorized from "../common/error_pages/Unauthorized";

const {Content} = Layout;

library.add(faSignInAlt);
library.add(faTimes);
library.add(faExclamationTriangle);
library.add(faInfo);


class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            currentUser: null,
            isAuthenticated: false,
            isLoading: false
        };
        this.handleLogout = this.handleLogout.bind(this);
        this.loadCurrentUser = this.loadCurrentUser.bind(this);
        this.handleLogin = this.handleLogin.bind(this);

        notification.config({
            placement: 'topLeft',
            top: 70,
            duration: 5,
        });
    }

    loadCurrentUser(justLoggedIn) {
        this.setState({
            isLoading: true
        });
        getCurrentUser()
            .then(response => {
                let roles = [];
                response.authorities.forEach((role) => {
                    roles.push(role.authority)
                });
                let currentUser = {
                    "id": response.id,
                    "username": response.username,
                    "name": response.name,
                    "roles": roles
                };
                localStorage.setItem('currentUser', JSON.stringify(currentUser));

                this.setState({
                    currentUser: currentUser,
                    isAuthenticated: true,
                    isLoading: false
                });
                if (justLoggedIn) {
                    notification.success({
                        message: `Witaj ${this.state.currentUser.name}!`,
                        description: "Zalogowano pomyślnie",
                    });
                }
            }).catch(error => {
            if (localStorage.getItem(ACCESS_TOKEN)) {
                localStorage.removeItem('currentUser');
                this.setState({
                    currentUser: null,
                    isAuthenticated: false,
                    isLoading: false
                });

                this.props.history.push("/");
                notification.warn({
                    message: `Wylogowano!`,
                    description: "Zaloguj się ponownie!",
                });
            } else {
                this.setState({
                    isLoading: false
                });
            }
        });
    }

    componentDidMount() {
        this.loadCurrentUser();
    }

    handleLogout() {
        localStorage.removeItem(ACCESS_TOKEN);
        localStorage.removeItem(ACCESS_TOKEN);

        this.setState({
            currentUser: null,
            isAuthenticated: false
        });

        this.props.history.push("/");

        notification.success({
            message: 'Wylogowano',
        });
    }

    handleLogin() {
        this.loadCurrentUser(true);
        this.props.history.push("/");
    }

    render() {
        if (this.state.isLoading) {
            return <LoadingSpin/>
        }
        if (this.state.isAuthenticated) {
            return (
                <Layout className="app-container">
                    <AppHeader isAuthenticated={this.state.isAuthenticated}
                               currentUser={this.state.currentUser}
                               onLogout={this.handleLogout}/>

                    <Content className="app-content">
                        <div className="container">
                            <Switch>
                                <PrivateRoute authenticated={this.state.isAuthenticated} exact path="/"
                                              component={Dashboard} user={this.state.currentUser} role={"ROLE_USER"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated} path="/users/:login"
                                              component={Edit} user={this.state.currentUser} role={"ROLE_USER"}/>
                                <Route path="/users"
                                       render={(props) => this.state.currentUser.roles.includes("ROLE_ADMINISTRATOR") ? (
                                               <UsersList currentUser={this.state.currentUser}  {...props} />) :
                                           (<Unauthorized/>)}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated} path="/agents/create"
                                              component={AgentCreate} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated} path="/agents/details/:id"
                                              component={AgentDetails} user={this.state.currentUser}
                                              role={"ROLE_USER"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated} path="/agents/edit/:id"
                                              component={AgentEdit} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/agents/:agentId/:agentName/service/details/:serviceId"
                                              component={ServiceDetails} user={this.state.currentUser}
                                              role={"ROLE_USER"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/agents/:agentId/:agentName/service/edit/:serviceId"
                                              component={ServiceEdit} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/agents/:agentId/:agentName/service/create"
                                              component={ServiceCreate} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/agents/service/:serviceId/monitoring/create"
                                              component={CreateMonitoringConfiguration} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/agents/service/:serviceId/logs/create"
                                              component={CreateLogsConfiguration} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/agents/service/logs/edit/:configurationId"
                                              component={EditLogsConfiguration} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/agents/service/monitoring/edit/:configurationId"
                                              component={EditMonitoringConfiguration} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated} path="/agents"
                                              component={AgentsList} user={this.state.currentUser} role={"ROLE_USER"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/alert/logs/create/:serviceName/:serviceId"
                                              component={LogsAlertCreate} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/alert/monitoring/create/:serviceName/:serviceId"
                                              component={MonitoringAlertCreate} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/alert/monitoring/edit/:id"
                                              component={MonitoringAlertEdit} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated} path="/alert/logs/edit/:id"
                                              component={LogsAlertEdit} user={this.state.currentUser}
                                              role={"ROLE_OPERATOR"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/alerts/configuration/list/monitoring"
                                              component={MonitoringAlertsConfigList} user={this.state.currentUser}
                                              role={"ROLE_USER"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/alerts/configuration/list/logs"
                                              component={LogsAlertsConfigList} user={this.state.currentUser}
                                              role={"ROLE_USER"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/logs"
                                              component={LogsViewer} user={this.state.currentUser}
                                              role={"ROLE_USER"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/charts"
                                              component={Charts} user={this.state.currentUser}
                                              role={"ROLE_USER"}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated}
                                              path="/401" component={Unauthorized} user={this.state.currentUser}
                                              role={"ROLE_USER"}/>
                                <Route component={NotFound}></Route>
                            </Switch>
                        </div>
                    </Content>
                    <AppFooter/>
                </Layout>
            );
        } else {
            return (
                <Layout className="app-container-auth">
                    <Content className="app-content-auth">
                        <div className="container-auth">
                            <Switch>
                                <Route exact path="/"
                                       render={(props) => <Login onLogin={this.handleLogin}  {...props} />}></Route>
                                <Route path="/login"
                                       render={(props) => <Login onLogin={this.handleLogin}  {...props} />}></Route>
                                <Route path="/register" component={Register}></Route>
                                <Route path="/password/reset" component={PasswordReset}></Route>
                                <Route path="/password/confirm/reset/:resetKey"
                                       component={PasswordResetConfirm}></Route>
                                <Route path="/user/activate/:status/:admin" component={InfoCallback}></Route>
                                <Route component={Unauthorized}></Route>
                            </Switch>
                        </div>
                    </Content>
                </Layout>
            )
        }
    }
}

export default withRouter(App);