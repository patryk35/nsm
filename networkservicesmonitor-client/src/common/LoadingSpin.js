import React from 'react';
import {Icon, Spin} from 'antd/lib/index';

export default function LoadingSpin(props) {
    const antIcon = <Icon type="loading" style={{fontSize: 24}} spin/>;
    return (
        <Spin indicator={antIcon}/>
    );
}