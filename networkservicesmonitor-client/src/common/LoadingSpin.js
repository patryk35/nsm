import React from 'react';
import {Icon, Spin} from 'antd/lib/index';

export default function LoadingSpin(props) {
    const antIcon = <Icon type="loading" style={{
        fontSize: 100,
        color: "#00ff00",
        position: "relative",
        float: "left",
        top: "50%",
        left: "50%",
        transform: "translate(-50%, -50%)"
    }} spin/>;
    return (
        <Spin indicator={antIcon}/>
    );
}