import React, {Component} from 'react';
import './AppFooter.css';
import {Layout, Divider} from 'antd';

const Footer = Layout.Footer;

class AppFooter extends Component {
    constructor(props) {
        super(props);
    }


    render() {
        return (
            <div className="footer-content">
                <Divider/>
                <Footer className="footer-text">
                    Network Services Monitor ©2019
                </Footer>
            </div>
        );
    }
}


export default AppFooter;