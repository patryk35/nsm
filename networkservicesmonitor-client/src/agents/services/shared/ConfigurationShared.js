import {deleteLogsConfiguration, deleteMonitoringConfiguration} from "../../../utils/APIRequestsUtils";
import {Button, notification} from "antd";
import React from "react";

export const executeDeleteConfiguration = (refreshFunction, id, option) => {
    let promise;
    if (option === "monitoring") {
        promise = deleteMonitoringConfiguration(id);
    } else if (option === "logs") {
        promise = deleteLogsConfiguration(id);
    }

    if (!promise) {
        return;
    }

    promise
        .then(() => {
            openNotificationWithIcon('success', 'Pomyślnie usunięto', 'Konfiguracja została usunięty');
            refreshFunction();
        }).catch(error => {
            openNotificationWithIcon('error', 'Nie udało się usunąć konfiguracji!', 'Spróbuj ponownie później')
        }
    );
};


export const handleConfigurationDeleteClick = (refreshFunction, configurationId, option) => {
    const key = `open${Date.now()}`;
    const btn = (
        <Button type="primary" size="large" className="agent-list-delete-button"
                onClick={() => {
                    notification.close(key);
                    executeDeleteConfiguration(refreshFunction, configurationId, option);
                }}>
            Potwierdź
        </Button>
    );
    notification.open({
        message: 'Usuń konfigurację',
        description:
            'Konfiguracja ' + configurationId + " zostanie usunięty. Dane zebrane dla konfiguracj nie zostaną usunięte.",
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