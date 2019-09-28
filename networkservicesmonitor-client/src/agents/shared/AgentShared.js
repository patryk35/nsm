import {deleteAgent} from "../../utils/APIRequestsUtils";
import {Button, notification} from "antd";
import React from "react";

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