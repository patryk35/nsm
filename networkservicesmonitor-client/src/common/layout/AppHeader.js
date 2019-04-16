import React, { Component } from 'react';
import {
    Link,
    withRouter
} from 'react-router-dom';
import './AppHeader.css';
import logo from '../../logo.svg';
import { Layout, Menu, Dropdown, Icon } from 'antd';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

const Header = Layout.Header;

class AppHeader extends Component {
    constructor(props) {
        super(props);
        this.handleMenuClick = this.handleMenuClick.bind(this);
    }

    handleMenuClick({ key }) {
        if(key === "logout") {
            this.props.onLogout();
        }
    }

    render() {
        let menuItems;
        if(this.props.currentUser) {
            menuItems = [
                <Menu.Item key="/">
                    <Link to="/">
                        <Icon type="home" className="nav-icon" /> Home
                    </Link>
                </Menu.Item>,
                <Menu.Item key="/agents">
                    <Link to="/agents">
                        <Icon type="cloud" className="nav-icon" /> Agenci
                    </Link>
                </Menu.Item>,
                <Menu.Item key="/logs">
                    <Link to="/logs">
                        <Icon type="database" className="nav-icon" /> Logi
                    </Link>
                </Menu.Item>,
                <Menu.Item key="/charts">
                    <Link to="/charts">
                        <Icon type="radar-chart" className="nav-icon" /> Wykresy
                    </Link>
                </Menu.Item>,
                <Menu.Item key="/profile" className="profile-menu">
                    <ProfileDropdownMenu
                        currentUser={this.props.currentUser}
                        handleMenuClick={this.handleMenuClick}/>
                </Menu.Item>
            ];
        } else {
            menuItems = [
                <Menu.Item key="/">
                    <Link to="/"><Icon type="home" className="nav-icon" /> Home</Link>
                </Menu.Item>,
                <Menu.Item key="/login">
                    <Link to="/login"><Icon type="login" className="nav-icon" /> Logowanie</Link>
                </Menu.Item>,
                <Menu.Item key="/register">
                    <Link to="/register"><FontAwesomeIcon icon="sign-in-alt" /> Rejestracja</Link>
                </Menu.Item>
            ];
        }

        return (
            <Header className="app-header">
                <div className="container">
                    <div className="app-title" >
                        <Link to="/">
                            <img src={logo} alt="Logo" className="app-logo"/>
                            <p className="app-title-text"> Network Services Monitor</p>
                        </Link>
                    </div>
                    <Menu
                        className="app-menu"
                        mode="horizontal"
                        selectedKeys={[this.props.location.pathname]}
                        style={{ lineHeight: '64px' }} >
                        {menuItems}
                    </Menu>
                </div>
            </Header>
        );
    }
}

function ProfileDropdownMenu(props) {
    const dropdownMenu = (
        <Menu onClick={props.handleMenuClick} className="profile-dropdown-menu">
            <Menu.Item key="user-info" className="dropdown-item" disabled>
                <div className="username-info">
                    @{props.currentUser.username}
                </div>
                <div className="user-full-name-info">
                    {props.currentUser.name}
                </div>
            </Menu.Item>
            <Menu.Divider />
            <Menu.Item key="profile" className="dropdown-item">
                <Link to={`/users/${props.currentUser.username}`}>Edytuj dane</Link>
            </Menu.Item>
            <Menu.Item key="logout" className="dropdown-item">
                Wyloguj
            </Menu.Item>
        </Menu>
    );

    return (
        <Dropdown
            overlay={dropdownMenu}
            trigger={['click']}
            getPopupContainer = { () => document.getElementsByClassName('profile-menu')[0]}>
            <a className="ant-dropdown-link">
                <Icon type="user" className="nav-icon" style={{marginRight: 0}} /> Profil <Icon type="down" />
            </a>
        </Dropdown>
    );
}


export default withRouter(AppHeader);