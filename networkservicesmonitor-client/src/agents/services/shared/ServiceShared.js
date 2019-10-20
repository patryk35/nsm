import {deleteService} from "../../../utils/APIRequestsUtils";
import {Button, notification} from "antd";
import React from "react";

export const executeDeleteService = (refreshFunction, id) => {
    let promise = deleteService(id);

    if (!promise) {
        return;
    }

    promise
        .then(() => {
            openNotificationWithIcon('success', 'Pomyślnie usunięto', 'Serwis został usunięty');
            refreshFunction();
        }).catch(error => {
            openNotificationWithIcon('error', 'Nie udało się usunąć serwisu!', 'Spróbuj ponownie później')
        }
    );
};


export const handleAgentServiceDeleteClick = (refreshFunction, serviceId, name) => {
    const key = `open${Date.now()}`;
    const btn = (
        <Button type="primary" size="large" className="agent-list-delete-button"
                onClick={() => {
                    notification.close(key);
                    executeDeleteService(refreshFunction, serviceId);
                }}>
            Potwierdź
        </Button>
    );
    notification.open({
        message: 'Usuń serwis',
        description:
            'Serwis ' + name + "(" + serviceId + ") zostanie usunięty. Dane zebrane dla serwisu nie zostaną usunięte.",
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