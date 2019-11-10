import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import React from "react";

export const genIcon = (level) => {
    if (level === 'ERROR') {
        return (
            <span className="alert-dashboard-level-error">
                                <FontAwesomeIcon icon="times"/>
                            </span>
        )
    } else if (level === 'WARN') {
        return (
            <span className="alert-dashboard-level-warn">
                    <FontAwesomeIcon icon="exclamation-triangle"/>
                </span>
        )
    } else {
        return (
            <span className="alert-dashboard-level-info">
                    <FontAwesomeIcon icon="info"/>
                </span>
        )
    }
};