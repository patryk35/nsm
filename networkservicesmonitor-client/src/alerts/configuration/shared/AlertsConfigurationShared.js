import {deleteLogsAlert, deleteMonitoringAlert} from "../../../utils/APIRequestsUtils";
import {Button, notification} from "antd";
import React from "react";
import {validateEmailOnce} from "../../../user/shared/SharedFunctions";

export const executeDeleteConfiguration = (refreshFunction, id, option) => {
    let promise;
    if (option === "monitoring") {
        promise = deleteMonitoringAlert(id);
    } else if (option === "logs") {
        promise = deleteLogsAlert(id);
    }

    if (!promise) {
        return;
    }

    promise
        .then(() => {
            openNotificationWithIcon('success', 'Pomyślnie usunięto', 'Konfiguracja została usunięty');
            refreshFunction();
        }).catch(() => {
            openNotificationWithIcon('error', 'Nie udało się usunąć konfiguracji!', 'Spróbuj ponownie później')
        }
    );
};

export const validateRecipients = (recipients) => {
    let validateStatus = 'success';
    let message = null;

    if(recipients.length === 0){
        validateStatus = 'error';
        message = `Podaj przynajmniej jeden adres e-mail`;
    } else{
        recipients.split(";").forEach((part) => {
            let result = validateEmailOnce(part);
            if(result.validateStatus !== 'success'){
                validateStatus = result.validateStatus;
                message = result.message + ": " + part;
            }
        });
    }

    return {
        validateStatus: validateStatus,
        message: message
    };
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

export const validateLevel = (parameter) => {
    let validateStatus = 'success';
    let message = null;
    if (parameter === null) {
        validateStatus = 'error';
        message = `Pole powinno zostać uzupełnione`;
    }

    return {
        validateStatus: validateStatus,
        message: message
    };
};

export const convertLevelToName = (level) => {
    if (level === 'ERROR') {
        return 'Błąd';
    } else if (level === 'WARN') {
        return 'Ostrzeżenie';
    } else {
        return 'Informacja';
    }
}