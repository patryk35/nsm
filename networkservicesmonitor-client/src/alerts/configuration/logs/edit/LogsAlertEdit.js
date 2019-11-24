import React, {Component} from 'react';
import {editLogsAlert, loadLogsAlertDetails} from '../../../../utils/APIRequestsUtils';
import './LogsAlertEdit.css';
import {
    ALERT_MESSAGE_MAX_LENGTH,
    ALERT_MESSAGE_MIN_LENGTH,
    LOGS_ALERT_SEARCH_STRING_MAX_LENGTH
} from '../../../../configuration';

import {Button, Checkbox, Form, Icon, Input, notification, Select} from 'antd';
import {validateLevel} from "../../shared/AlertsConfigurationShared";
import {Link} from "react-router-dom";
import LoadingSpin from "../../../../common/spin/LoadingSpin";

const FormItem = Form.Item;
const {Option} = Select;

class LogsAlertEdit extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isLoading: true
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.isFormValid = this.isFormValid.bind(this);
    }

    componentDidMount() {
        this.loadDetails();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            this.setState({
                parameters: [],
                isLoading: false
            });
            this.loadDetails();
        }
    }

    handleChange(event, validationFun) {
        this.setState({
            [event.target.name]: {
                value: event.target.value,
                ...validationFun(event.target.value)
            }
        });
    }

    handleChangeLevel(event, validationFun) {
        this.setState({
            ["level"]: {
                value: event,
                ...validationFun(event)
            }
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        const state = this.state;
        const editRequest = {
            alertId: state.id.value,
            message: state.message.value,
            pathSearchString: state.pathSearchString.value,
            searchString: state.searchString.value,
            enabled: state.enabled.value,
            alertLevel: state.level.value
        };
        editLogsAlert(editRequest)
            .then(response => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Konfiguracja alertu zapisana pomyślnie!',
                    description: "",
                    btn,
                    key
                });
                this.props.history.goBack();
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się zapisać konfiguracji alertu',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    loadDetails() {
        let promise = loadLogsAlertDetails(this.props.match.params.id);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    id: {value: response.id},
                    parameter: {
                        value: response.monitoredParameterType,
                        name: response.monitoredParameterTypeName,
                        validateStatus: "success"
                    },
                    serviceName: response.serviceName,
                    message: {value: response.message, validateStatus: "success"},
                    pathSearchString: {value: response.pathSearchString, validateStatus: "success"},
                    searchString: {value: response.searchString, validateStatus: "success"},
                    enabled: {value: response.enabled, validateStatus: "success"},
                    level: {value: response.alertLevel, validateStatus: "success"},
                    isLoading: false
                })
            }).catch(error => {
            notification.error({
                message: 'Problem podczas pobierania danych!',
                description: ' Spróbuj ponownie później!',
                duration: 5
            });
            this.props.history.goBack();
        });
    }

    isFormValid() {
        const state = this.state;
        return state.message.validateStatus === 'success' && state.pathSearchString.validateStatus === 'success' &&
            state.searchString.validateStatus === 'success' && state.level.validateStatus === 'success';
    }

    render() {
        return (
            <article className="logs-alert-edit-container">
                {this.state.isLoading ? (
                    <LoadingSpin/>
                ) : (
                    <div>
                        <h1 className="page-title">Edycja alertu dla serwisu {this.state.serviceName} </h1>
                        <div className="logs-alert-edit-content">
                            <Form onSubmit={this.handleSubmit} className="logs-alert-edit-form">
                                <FormItem label="Wiadomość"
                                          hasFeedback
                                          validateStatus={this.state.message.validateStatus}
                                          help={this.state.message.message}>
                                    <Input
                                        prefix={<Icon type="message"/>}
                                        size="large"
                                        name="message"
                                        value={this.state.message.value}
                                        onChange={(event) => this.handleChange(event, this.validateMessage)}/>
                                </FormItem>
                                <FormItem
                                    label="Poziom"
                                    hasFeedback
                                    validateStatus={this.state.level.validateStatus}
                                    help={this.state.level.message}>
                                    <Select
                                        value={this.state.level.value}
                                        onChange={(event) => this.handleChangeLevel(event, validateLevel)}>
                                        <Option value='INFO'>Informacja</Option>
                                        <Option value='WARN'>Ostrzeżenie</Option>
                                        <Option value='ERROR'>Błąd</Option>

                                    </Select>
                                </FormItem>
                                <FormItem
                                    label="Ścieżka"
                                    hasFeedback
                                    validateStatus={this.state.pathSearchString.validateStatus}
                                    help={this.state.pathSearchString.message}>
                                    <Input
                                        prefix={<Icon type="read"/>}
                                        size="large"
                                        name="pathSearchString"
                                        value={this.state.pathSearchString.value}
                                        onChange={(event) => this.handleChange(event, this.validatePathSearchString)}/>
                                </FormItem>
                                <FormItem
                                    label="Fraza logu"
                                    hasFeedback
                                    validateStatus={this.state.searchString.validateStatus}
                                    help={this.state.searchString.message}>
                                    <Input
                                        prefix={<Icon type="read"/>}
                                        size="large"
                                        name="searchString"
                                        value={this.state.searchString.value}
                                        onChange={(event) => this.handleChange(event, this.validateSearchString)}/>
                                </FormItem>
                                <FormItem
                                    label="Alert aktywny"
                                    validateStatus={""}
                                    help={this.state.enabled.message}
                                >
                                    <Checkbox
                                        checked={this.state.enabled.value}
                                        onChange={(event) => {
                                            this.setState({
                                                enabled: {
                                                    value: event.target.checked,
                                                }
                                            })
                                        }}>Tak</Checkbox>
                                </FormItem>
                                <FormItem>
                                    <Button type="primary"
                                            htmlType="submit"
                                            size="large"
                                            className="logs-alert-edit-form-button"
                                            disabled={!this.isFormValid()}>Zapisz</Button>
                                    <Button className={"logs-alert-edit-back-button"}>
                                        <Link onClick={() => {
                                            this.props.history.goBack()
                                        }}>Powrót</Link>
                                    </Button>
                                </FormItem>
                            </Form>
                        </div>
                    </div>
                )}
            </article>
        );
    }

    // Validation Functions
    validateMessage = (msg) => {
        let validateStatus = 'success';
        let message = null;

        if (msg.length < ALERT_MESSAGE_MIN_LENGTH || msg.length > ALERT_MESSAGE_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć między ${ALERT_MESSAGE_MIN_LENGTH} a ${ALERT_MESSAGE_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

    };


    validatePathSearchString = (pathSearchString) => {
        let validateStatus = 'success';
        let message = null;

        if (pathSearchString.length > LOGS_ALERT_SEARCH_STRING_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć maksymalnie ${LOGS_ALERT_SEARCH_STRING_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }

    };

    validateSearchString = (searchString) => {
        let validateStatus = 'success';
        let message = null;

        if (searchString.length > LOGS_ALERT_SEARCH_STRING_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć maksymalnie ${LOGS_ALERT_SEARCH_STRING_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }
    };
}

export default LogsAlertEdit;