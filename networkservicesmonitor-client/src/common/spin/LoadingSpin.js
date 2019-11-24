import React from 'react';
import './LoadingSpin.css';
import {Spin} from "antd";

export default function LoadingSpin(props) {
    return (
        <div className="spin-container">
            <Spin size="large"/>
            <p className={"loading-text"}>Trwa wczytywanie</p>
        </div>
    );
}