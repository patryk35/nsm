import React, {Component} from 'react';
import './Welcome.css';
import logo from '../../logo.svg';
import {Col, Row} from 'antd';
import QueueAnim from 'rc-queue-anim';
export const isImg = /^http(s)?:\/\/([\w-]+\.)+[\w-]+(\/[\w-./?%&=]*)?/;
class Welcome extends Component {
    render() {
        const { ...currentProps } = this.props;
        const { dataSource } = currentProps;
        //delete currentProps.dataSource;
        //delete currentProps.isMobile;
        return (
            <div {...currentProps} {...dataSource.wrapper}>
                <QueueAnim
                    key="QueueAnim"
                    type={['bottom', 'top']}
                    delay={200}
                    {...dataSource.textWrapper}
                >
                    <div key="title" {...dataSource.title}>
                        {typeof dataSource.title.children === 'string' &&
                        dataSource.title.children.match(isImg) ? (
                            <img src={dataSource.title.children} width="100%" alt="img" />
                        ) : (
                            dataSource.title.children
                        )}
                    </div>
                    <div key="content" {...dataSource.content}>
                        {dataSource.content.children}
                    </div>
                </QueueAnim>
            </div>
        );
    }
}


export default Welcome;