import {
    ACCESS_TOKEN,
    AGENT_LIST_SIZE,
    AGENT_SERVICES_CONFIGURATION_LIST_SIZE,
    AGENT_SERVICES_LIST_SIZE,
    API_URL, USER_LIST_SIZE
} from '../configuration';

const request = (options) => {
    const headers = new Headers({
        'Content-Type': 'application/json',
    });

    if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(ACCESS_TOKEN))
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);

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

export function deactivateUser(id) {
    return request({
        url: API_URL + "/users/deactivate/" + id,
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
        method: 'PUT',
        body: JSON.stringify(agentEditRequest)
    });
}

export function editService(serviceEditRequest) {
    return request({
        url: API_URL + "/agent/service",
        method: 'PUT',
        body: JSON.stringify(serviceEditRequest)
    });
}

export function editLogsConfiguration(logsConfigurationEditRequest) {
    return request({
        url: API_URL + "/agent/service/logConfig",
        method: 'PUT',
        body: JSON.stringify(logsConfigurationEditRequest)
    });
}

export function editMonitoringConfiguration(monitoringConfigurationEditRequest) {
    return request({
        url: API_URL + "/agent/service/parameterConfig",
        method: 'PUT',
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