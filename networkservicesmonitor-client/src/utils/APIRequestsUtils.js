import {ACCESS_TOKEN, AGENT_LIST_SIZE, API_URL} from '../configuration';

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

export function register(registerRequest) {
    return request({
        url: API_URL + "/auth/register",
        method: 'POST',
        body: JSON.stringify(registerRequest)
    });
}

export function checkUsernameAvailability(username) {
    return request({
        url: API_URL + "/users/getUsernameAvailability?username=" + username,
        method: 'GET'
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

export function getAgentsList(page, size) {
    page = page || 0;
    size = size || AGENT_LIST_SIZE;
    return request({
        url: API_URL + "/agent?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getAgentServicesList(agentId, page, size) {
    page = page || 0;
    size = size || AGENT_LIST_SIZE;
    return request({
        url: API_URL + "/agent/services/" + agentId + "?page=" + page + "&size=" + size,
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