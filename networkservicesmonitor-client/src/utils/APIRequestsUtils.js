import {
    ACCESS_TOKEN,
    AGENT_LIST_SIZE,
    AGENT_SERVICES_CONFIGURATION_LIST_SIZE,
    AGENT_SERVICES_LIST_SIZE,
    ALERTS_CONFIGS_LIST_SIZE,
    ALERTS_LIST_SIZE,
    API_URL,
    USER_LIST_SIZE
} from '../configuration';
import {sleep} from "./TestUtils";

//TODO(minor): split it
const request = async (options) => {
    const headers = new Headers({
        'Content-Type': 'application/json',
    });

    if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(ACCESS_TOKEN))
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);
    //await sleep(1000);
    return fetch(options.url, options)
        .then(response =>
            response.json().then(json => {
                if (!response.ok) {
                    return Promise.reject(json);
                }
                return json;
            })
        );
};


export function login(loginRequest) {
    return request({
        url: API_URL + "/auth/login",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function activateUser(id) {
    return request({
        url: API_URL + "/users/activate/" + id,
        method: 'POST',
    });
}

export function disableUser(id) {
    return request({
        url: API_URL + "/users/disable/" + id,
        method: 'POST',
    });
}

export function enableUser(id) {
    return request({
        url: API_URL + "/users/enable/" + id,
        method: 'POST',
    });
}

export function addAdminAccess(id) {
    return request({
        url: API_URL + "/users/admin/enable/" + id,
        method: 'POST',
    });
}

export function removeAdminAccess(id) {
    return request({
        url: API_URL + "/users/admin/disable/" + id,
        method: 'POST',
    });
}

export function addOperatorAccess(id) {
    return request({
        url: API_URL + "/users/operator/enable/" + id,
        method: 'POST',
    });
}

export function removeOperatorAccess(id) {
    return request({
        url: API_URL + "/users/operator/disable/" + id,
        method: 'POST',
    });
}

export function register(registerRequest) {
    return request({
        url: API_URL + "/auth/register",
        method: 'POST',
        body: JSON.stringify(registerRequest)
    });
}

export function getUserEmail() {
    return request({
        url: API_URL + "/users/email",
        method: 'GET'
    });
}

export function changeEmail(emailChangeRequest) {
    return request({
        url: API_URL + "/users/email",
        method: 'PATCH',
        body: JSON.stringify(emailChangeRequest)
    });
}

export function changePassword(passwordChangeRequest) {
    return request({
        url: API_URL + "/users/password",
        method: 'PATCH',
        body: JSON.stringify(passwordChangeRequest)
    });
}

export function getUsersList(page, size) {
    page = page || 0;
    size = size || USER_LIST_SIZE;
    return request({
        url: API_URL + "/users?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function checkUsernameAvailability(username) {
    return request({
        url: API_URL + "/users/getUsernameAvailability?username=" + username,
        method: 'GET'
    });
}

export function validatePassword(password) {
    return request({
        url: API_URL + "/users/password/validate",
        body: JSON.stringify({"password": password}),
        method: 'POST'
    });
}

export function checkEmailAvailability(email) {
    return request({
        url: API_URL + "/users/getEmailAvailability?email=" + email,
        method: 'GET'
    });
}


export function getCurrentUser() {
    if (!localStorage.getItem(ACCESS_TOKEN)) {
        return Promise.reject("No access token provided.");
    }

    return request({
        url: API_URL + "/users/details",
        method: 'GET'
    });
}


export function createAgent(agentCreateRequest) {
    return request({
        url: API_URL + "/agent",
        method: 'POST',
        body: JSON.stringify(agentCreateRequest)
    });
}

export function checkAgentNameAvailability(name) {
    return request({
        url: API_URL + "/agent/getNameAvailability?name=" + name,
        method: 'GET'
    });
}

export function checkServiceNameAvailability(name, agentId) {
    return request({
        url: API_URL + "/agent/service/getNameAvailability/" + agentId + "?name=" + name,
        method: 'GET'
    });
}

export function deleteAgent(agentId) {
    return request({
        url: API_URL + "/agent/" + agentId,
        method: 'DELETE'
    });
}

export function deleteService(serviceId) {
    return request({
        url: API_URL + "/agent/service/" + serviceId,
        method: 'DELETE'
    });
}

export function deleteMonitoringConfiguration(configurationId) {
    return request({
        url: API_URL + "/agent/service/parameterConfig/" + configurationId,
        method: 'DELETE'
    });
}

export function deleteLogsConfiguration(configurationId) {
    return request({
        url: API_URL + "/agent/service/logConfig/" + configurationId,
        method: 'DELETE'
    });
}

export function createAgentService(agentServiceCreateRequest) {
    return request({
        url: API_URL + "/agent/service",
        method: 'POST',
        body: JSON.stringify(agentServiceCreateRequest)
    });
}

export function createMonitoringConfiguration(monitoringConfigurationRequest) {
    return request({
        url: API_URL + "/agent/service/parameterConfig",
        method: 'POST',
        body: JSON.stringify(monitoringConfigurationRequest)
    });
}

export function createLogsConfiguration(logsConfigurationRequest) {
    return request({
        url: API_URL + "/agent/service/logConfig",
        method: 'POST',
        body: JSON.stringify(logsConfigurationRequest)
    });
}


export function editAgent(agentEditRequest) {
    return request({
        url: API_URL + "/agent",
        method: 'PATCH',
        body: JSON.stringify(agentEditRequest)
    });
}

export function editService(serviceEditRequest) {
    return request({
        url: API_URL + "/agent/service",
        method: 'PATCH',
        body: JSON.stringify(serviceEditRequest)
    });
}

export function editLogsConfiguration(logsConfigurationEditRequest) {
    return request({
        url: API_URL + "/agent/service/logConfig",
        method: 'PATCH',
        body: JSON.stringify(logsConfigurationEditRequest)
    });
}

export function editMonitoringConfiguration(monitoringConfigurationEditRequest) {
    return request({
        url: API_URL + "/agent/service/parameterConfig",
        method: 'PATCH',
        body: JSON.stringify(monitoringConfigurationEditRequest)
    });
}

export function getAgentsList(page, size) {
    page = page || 0;
    size = size || AGENT_LIST_SIZE;
    return request({
        url: API_URL + "/agent?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getAgentDetails(agentId) {
    return request({
        url: API_URL + "/agent/details/" + agentId,
        method: 'GET'
    });
}

export function getAgentServiceDetails(agentId) {
    return request({
        url: API_URL + "/agent/service/details/" + agentId,
        method: 'GET'
    });
}

export function getLogsConfigurationDetails(configurationId) {
    return request({
        url: API_URL + "/agent/service/logConfig/details/" + configurationId,
        method: 'GET'
    });
}

export function getMonitoringConfigurationDetails(configurationId) {
    return request({
        url: API_URL + "/agent/service/parameterConfig/details/" + configurationId,
        method: 'GET'
    });
}

export function getAgentServicesList(agentId, page, size) {
    page = page || 0;
    size = size || AGENT_SERVICES_LIST_SIZE;
    return request({
        url: API_URL + "/agent/services/" + agentId + "?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getAgentServicesLogsConfigurationsList(serviceId, page, size) {
    page = page || 0;
    size = size || AGENT_SERVICES_CONFIGURATION_LIST_SIZE;
    return request({
        url: API_URL + "/agent/service/logConfigs/details/" + serviceId + "?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getAgentServicesMonitoringConfigurationsList(serviceId, page, size) {
    page = page || 0;
    size = size || AGENT_SERVICES_CONFIGURATION_LIST_SIZE;
    return request({
        url: API_URL + "/agent/service/parameterConfigs/details/" + serviceId + "?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getLogs(logsRequest) {
    return request({
        url: API_URL + "/logs/load",
        method: 'POST',
        body: JSON.stringify(logsRequest)
    });
}

export function getMonitoredParameterValues(logsRequest) {

    return request({
        url: API_URL + "/monitoring/load",
        method: 'POST',
        body: JSON.stringify(logsRequest)
    });
}

export function loadNewAvailableMonitoringParameters(serviceId) {
    return request({
        url: API_URL + "/agent/service/parameterConfig/available/" + serviceId,
        method: 'GET'
    });
}

export function loadServiceMonitoringParameters(serviceId) {
    return request({
        url: API_URL + "/agent/service/parameterConfig/added/" + serviceId,
        method: 'GET'
    });
}

export function getLogsAlertsList(page, size) {
    page = page || 0;
    size = size || ALERTS_LIST_SIZE;
    return request({
        url: API_URL + "/alerts/logs?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getMonitoringAlertsList(page, size) {
    page = page || 0;
    size = size || ALERTS_LIST_SIZE;
    return request({
        url: API_URL + "/alerts/monitoring?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getUserAlertsList(page, size) {
    page = page || 0;
    size = size || ALERTS_LIST_SIZE;
    return request({
        url: API_URL + "/alerts/user?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function createLogsAlert(createRequest) {
    return request({
        url: API_URL + "/alerts/config/logs",
        method: 'POST',
        body: JSON.stringify(createRequest)
    });
}

export function createMonitoringAlert(createRequest) {
    return request({
        url: API_URL + "/alerts/config/monitoring",
        method: 'POST',
        body: JSON.stringify(createRequest)
    });
}

export function editLogsAlert(editRequest) {
    return request({
        url: API_URL + "/alerts/config/logs",
        method: 'PATCH',
        body: JSON.stringify(editRequest)
    });
}

export function editMonitoringAlert(editRequest) {
    return request({
        url: API_URL + "/alerts/config/monitoring",
        method: 'PATCH',
        body: JSON.stringify(editRequest)
    });
}

export function deleteLogsAlert(id) {
    return request({
        url: API_URL + "/alerts/config/logs/" + id,
        method: 'DELETE'
    });
}

export function deleteMonitoringAlert(id) {
    return request({
        url: API_URL + "/alerts/config/monitoring/" + id,
        method: 'DELETE'
    });
}

export function loadLogsAlertDetails(id) {
    return request({
        url: API_URL + "/alerts/config/logs/details/" + id,
        method: 'GET'
    });
}

export function loadMonitoringAlertDetails(id) {
    return request({
        url: API_URL + "/alerts/config/monitoring/details/" + id,
        method: 'GET'
    });
}

export function getLogsAlertConfigList(page, size) {
    page = page || 0;
    size = size || ALERTS_CONFIGS_LIST_SIZE;
    return request({
        url: API_URL + "/alerts/config/logs?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getMonitoringAlertConfigList(page, size) {
    page = page || 0;
    size = size || ALERTS_CONFIGS_LIST_SIZE;
    return request({
        url: API_URL + "/alerts/config/monitoring?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getLogAlert(alertId) {
    return request({
        url: API_URL + "/alerts/logs/" + alertId,
        method: 'GET'
    });
}

export function getMonitoringAlert(alertId) {
    return request({
        url: API_URL + "/alerts/monitoring/" + alertId,
        method: 'GET'
    });
}

export function getUserAlert(alertId) {
    return request({
        url: API_URL + "/alerts/user/" + alertId,
        method: 'GET'
    });
}

export function resetPassword(body) {
    return request({
        url: API_URL + "/users/resetPassword",
        method: 'POST',
        body: JSON.stringify(body)
    });
}

export function confirmPasswordReset(body) {
    return request({
        url: API_URL + "/users/resetPassword/confirm",
        method: 'POST',
        body: JSON.stringify(body)
    });
}