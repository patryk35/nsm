import {deleteAgent} from "../../utils/APIRequestsUtils";
import {Button, notification} from "antd";
import React from "react";
import {AGENT_ALLOWED_ORIGINS_MAX_LENGTH} from "../../configuration";
const IP_REGEX = RegExp('^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$');

export const executeDeleteAgent = (refreshFunction, id) => {
    let promise = deleteAgent(id);

    if (!promise) {
        return;
    }

    promise
        .then(() => {
            openNotificationWithIcon('success', 'Pomyślnie usunięto', 'Agent został usunięty');
            refreshFunction();
        }).catch(error => {
            openNotificationWithIcon('error', 'Nie udało się usunąć agenta!', 'Spróbuj ponownie później')
        }
    );
};


export const handleAgentDeleteClick = (refreshFunction, agentId, name) => {
    const key = `open${Date.now()}`;
    const btn = (
        <Button type="primary" size="large" className="agent-list-delete-button"
                onClick={() => {
                    notification.close(key);
                    executeDeleteAgent(refreshFunction, agentId);
                }}>
            Potwierdź
        </Button>
    );
    notification.open({
        message: 'Usuń agenta',
        description:
            'Agent ' + name + "(" + agentId + ") zostanie usunięty. Dane zebrane przez agenta nie zostaną usunięte.",
        btn,
        key
    });
};

export const openNotificationWithIcon = (type, message, description) => {
    notification[type]({
        message: message,
        description:
        description,
    });
};

export const validateAllowedOrigins = (allowedOrigins) => {
    let validateStatus = 'success';
    let message = null;
    if (allowedOrigins.length > AGENT_ALLOWED_ORIGINS_MAX_LENGTH) {
        validateStatus = 'error';
        message = `Pole powinno zawierać mieć maksymalnie ${AGENT_ALLOWED_ORIGINS_MAX_LENGTH} znaków`;
    } else if (allowedOrigins !== "") {
        let isValid = true;
        allowedOrigins.split(",").forEach(v => {
            if ((v !== "*") && (!IP_REGEX.test(v))) {
                isValid = false;
            }
        });
        if (!isValid) {
            validateStatus = 'error';
            message = 'Wprowadzono nieprwaidłowe dane';
        }
    }

    return {
        validateStatus: validateStatus,
        message: message
    }
};