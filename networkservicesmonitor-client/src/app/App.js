import React, {Component} from 'react';
import './App.css';
import {
    Route,
    withRouter,
    Switch
} from 'react-router-dom';

import {getCurrentUser} from '../utils/APIRequestsUtils';
import {ACCESS_TOKEN} from '../configuration';

import Login from '../user/login/Login';
import Register from '../user/register/Register';
import AppHeader from '../common/layout/AppHeader';
import AppFooter from '../common/layout/AppFooter';
import NotFound from '../common/error_pages/NotFound';
import LoadingSpin from '../common/LoadingSpin';
import PrivateRoute from '../common/error_pages/PrivateRoute';
import {library} from '@fortawesome/fontawesome-svg-core'
import {faSignInAlt} from '@fortawesome/free-solid-svg-icons'


import {Layout, notification} from 'antd';
import Welcome from "../dashboard/welcome/Welcome";

const {Content} = Layout;

library.add(faSignInAlt);

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            currentUser: null,
            isAuthenticated: false,
            isLoading: false
        }
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
                this.setState({
                    currentUser: response,
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
                if(localStorage.getItem(ACCESS_TOKEN)) {
                    localStorage.removeItem(ACCESS_TOKEN);
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
        console.log(this.state.isAuthenticated)
        return (
            <Layout className="app-container">
                <AppHeader isAuthenticated={this.state.isAuthenticated}
                           currentUser={this.state.currentUser}
                           onLogout={this.handleLogout}/>
                <Content className="app-content">
                    <div className="container">
                        <Switch>
                            <Route exact path="/" component={Welcome}></Route>
                            <Route path="/login"
                                   render={(props) => <Login onLogin={this.handleLogin}  {...props} />}></Route>
                            <Route path="/register" component={Register}></Route>
                            <Route path="/users/:username"
                                //render={(props) => <Profile isAuthenticated={this.state.isAuthenticated} currentUser={this.state.currentUser} {...props}  />}
                            >
                            </Route>
                            <Route component={NotFound}></Route>
                        </Switch>
                    </div>
                </Content>
                <AppFooter/>
            </Layout>
        );
    }
}

export default withRouter(App);