import React, {Component} from 'react';
import {Link, withRouter} from 'react-router-dom';
import './AppHeader.css';
import logo from '../../img/logo.svg';
import {Dropdown, Icon, Layout, Menu} from 'antd';

const Header = Layout.Header;

class AppHeader extends Component {
    constructor(props) {
        super(props);
        this.handleMenuClick = this.handleMenuClick.bind(this);
    }

    handleMenuClick({key}) {
        if (key === "logout") {
            this.props.onLogout();
        }
    }

    resolveSelectedKeys(path){
        if(path.match(/^.*users.*$/) && path !== "/users"){
            return "/profile";
        } else if (path === "/alerts/configuration/list/logs" || path === "/alerts/configuration/list/monitoring") {
            return "/";
        } else if(path.match(/^.*alert.*$/) || path.match(/^.*agent.*$/)){
            return "/agents";
        }
        return path;
    }

    render() {
        let menuItems;
        menuItems = [
            <Menu.Item className="app-title-text2" key="/">
                <Link to="/">
                    <Icon type="home" className="nav-icon"/> Home
                </Link>
            </Menu.Item>,
            <Menu.Item key="/agents">
                <Link to="/agents">
                    <Icon type="cloud" className="nav-icon"/> Agenci
                </Link>
            </Menu.Item>,
            <Menu.Item key="/logs">
                <Link to="/logs">
                    <Icon type="database" className="nav-icon"/> Logi
                </Link>
            </Menu.Item>,
            <Menu.Item key="/charts">
                <Link to="/charts">
                    <Icon type="radar-chart" className="nav-icon"/> Wykresy
                </Link>
            </Menu.Item>
        ];
        if (this.props.currentUser.roles.includes("ROLE_ADMINISTRATOR")) {
            menuItems.push(
                <Menu.Item key="/users">
                    <Link to="/users">
                        <Icon type="user" className="nav-icon"/> Użytkownicy
                    </Link>
                </Menu.Item>,
            );
        }

        menuItems.push(
            <Menu.Item key="/profile" className="profile-menu">
                <ProfileDropdownMenu
                    currentUser={this.props.currentUser}
                    handleMenuClick={this.handleMenuClick}/>
            </Menu.Item>
        );

        return (
            <Header className="app-header">
                <div className="container">
                    <div className="app-title">
                        <Link to="/">
                            <img src={logo} alt="Logo" className="app-logo"/>
                            <p className="app-title-text"> Network Services Monitor</p>
                        </Link>
                    </div>
                    <Menu
                        className="app-menu"
                        mode="horizontal"
                        selectedKeys={[this.resolveSelectedKeys(this.props.location.pathname)]}
                        style={{lineHeight: '64px'}}>
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
            <Menu.Divider/>
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
            getPopupContainer={() => document.getElementsByClassName('profile-menu')[0]}>
            <a className="ant-dropdown-link">
                <Icon type="user" className="nav-icon" style={{marginRight: 0}}/> Profil <Icon type="down"/>
            </a>
        </Dropdown>
    );
}


export default withRouter(AppHeader);