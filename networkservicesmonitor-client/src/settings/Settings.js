import React, {Component} from 'react';
import {changeEmail, changePassword, getAppSetting, updateAppSettings} from '../utils/APIRequestsUtils';
import './Settings.css';

import {Button, Form, Icon, Input, notification} from 'antd';
import LoadingSpin from "../common/spin/LoadingSpin";
import {LOGS_ALERT_SEARCH_STRING_MAX_LENGTH} from "../configuration";
import {validateEmail} from "../user/shared/SharedFunctions";

const FormItem = Form.Item;


class Settings extends Component {
    constructor(props) {
        super(props);
        this.loadDetails(this.props.match.params.login)
        this.state = {
            webserviceWorkersCount: {value: "", message: ""},
            alertsCheckingInterval: {value: "", message: ""},
            smtpServer: {value: "", message: ""},
            smtpUsername: {value: "", message: ""},
            smtpPassword: {value: "", message: ""},
            smtpPort: {value: "", message: ""},
            smtpFromAddress: {value: "", message: ""},
            chartsMaxValuesCount: {value: "", message: ""},
            smtpMailsFooterName: {value: "", message: ""},

            isLoading: true,
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.isFormValid = this.isFormValid.bind(this);
    }

    handleChange(event, validationFun) {
        this.setState({
            changed: true,
            [event.target.name]: {
                value: event.target.value,
                ...validationFun(event.target.value)
            }
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        const state = this.state;
        const updateRequest = {
            webserviceWorkersCount: state.webserviceWorkersCount.value,
            alertsCheckingInterval: state.alertsCheckingInterval.value,
            smtpServer: state.smtpServer.value,
            smtpUsername: state.smtpUsername.value,
            smtpPassword: state.smtpPassword.value,
            smtpPort: state.smtpPort.value,
            smtpFromAddress: state.smtpFromAddress.value,
            chartsMaxValuesCount: state.chartsMaxValuesCount.value,
            smtpMailsFooterName: state.smtpMailsFooterName.value,
            changed: false,
            isLoading: false
        };
        updateAppSettings(updateRequest)
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                this.setState({
                    changed: false
                });
                notification.success({
                    message: 'Zapisano pomyślnie!',
                    description: "",
                    btn,
                    key
                });
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się zapisać danych!',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }


    isFormValid() {
        const state = this.state;
        return state.webserviceWorkersCount.validateStatus === 'success' &&
            state.alertsCheckingInterval.validateStatus === 'success' &&
            state.smtpServer.validateStatus === 'success' &&
            state.smtpUsername.validateStatus === 'success' &&
            state.smtpPassword.validateStatus === 'success' &&
            state.smtpPort.validateStatus === 'success' &&
            state.smtpFromAddress.validateStatus === 'success' &&
            state.chartsMaxValuesCount.validateStatus === 'success' &&
            state.smtpMailsFooterName.validateStatus === 'success' && state.changed;
    }

    render() {
        return (
            <div>
                <article className="settings-container">
                    {this.state.isLoading ? (
                        <LoadingSpin/>
                    ) : (
                        <Form onSubmit={this.handleSubmit} className="settings-form">
                            <div className="settings-sub-container">
                                <h1 className="page-title">Ustawinenia wysyłania wiadomości e-mail</h1>
                                <FormItem
                                    label="Adres serwera SMTP"
                                    hasFeedback
                                    validateStatus={this.state.smtpServer.validateStatus}
                                    help={this.state.smtpServer.message}>
                                    <Input
                                        size="large"
                                        name="smtpServer"
                                        value={this.state.smtpServer.value}
                                        onChange={(event) => this.handleChange(event, this.validateString)}
                                    />
                                </FormItem>
                                <FormItem
                                    label="Port serwera SMTP"
                                    hasFeedback
                                    validateStatus={this.state.smtpPort.validateStatus}
                                    help={this.state.smtpPort.message}>
                                    <Input
                                        size="large"
                                        name="smtpPort"
                                        type="number"
                                        value={this.state.smtpPort.value}
                                        onChange={(event) => this.handleChange(event, this.validatePort)}
                                    />
                                </FormItem>
                                <FormItem
                                    label="Użytkownik serwera SMTP"
                                    hasFeedback
                                    validateStatus={this.state.smtpUsername.validateStatus}
                                    help={this.state.smtpUsername.message}>
                                    <Input
                                        size="large"
                                        name="smtpUsername"
                                        value={this.state.smtpUsername.value}
                                        onChange={(event) => this.handleChange(event, this.validateString)}
                                    />
                                </FormItem>
                                <FormItem
                                    label="Hasło serwera SMTP"
                                    hasFeedback
                                    validateStatus={this.state.smtpPassword.validateStatus}
                                    help={this.state.smtpPassword.message}>
                                    <Input
                                        size="large"
                                        name="smtpPassword"
                                        type="password"
                                        value={this.state.smtpPassword.value}
                                        onChange={(event) => this.handleChange(event, this.validateString)}
                                    />
                                </FormItem>
                                <FormItem
                                    label="Adres e-mail adresata"
                                    hasFeedback
                                    validateStatus={this.state.smtpFromAddress.validateStatus}
                                    help={this.state.smtpFromAddress.message}>
                                    <Input
                                        size="large"
                                        name="smtpFromAddress"
                                        value={this.state.smtpFromAddress.value}
                                        onChange={(event) => this.handleChange(event, validateEmail)}
                                    />
                                </FormItem>
                                <FormItem
                                    label="Nazwa wyświetlana w stopce wiadomości"
                                    hasFeedback
                                    validateStatus={this.state.smtpMailsFooterName.validateStatus}
                                    help={this.state.smtpMailsFooterName.message}>
                                    <Input
                                        size="large"
                                        name="smtpMailsFooterName"
                                        value={this.state.smtpMailsFooterName.value}
                                        onChange={(event) => this.handleChange(event, this.validateString)}
                                    />
                                </FormItem>
                            </div>

                            <div className="settings-sub-container">
                                <h1 className="page-title">Ustawinenia serwera aplikacji</h1>
                                <FormItem
                                    label="Liczba jednostek przetwarzających dane otrzymane od agetów po stronie serwera aplikacji"
                                    hasFeedback
                                    validateStatus={this.state.webserviceWorkersCount.validateStatus}
                                    help={this.state.webserviceWorkersCount.message}>
                                    <Input
                                        size="large"
                                        name="webserviceWorkersCount"
                                        type="number"
                                        value={this.state.webserviceWorkersCount.value}
                                        onChange={(event) => this.handleChange(event, this.validateWorkersCount)}
                                    />
                                </FormItem>
                                <FormItem
                                    label="Interwał sprawdzania wystąpień zdarzeń zdefiniowanych w konfiguracji alertów"
                                    hasFeedback
                                    validateStatus={this.state.alertsCheckingInterval.validateStatus}
                                    help={this.state.alertsCheckingInterval.message}>
                                    <Input
                                        size="large"
                                        name="alertsCheckingInterval"
                                        type="number"
                                        value={this.state.alertsCheckingInterval.value}
                                        onChange={(event) => this.handleChange(event, this.validateInterval)}
                                    />
                                </FormItem>
                            </div>

                            <div className="settings-sub-container">
                                <h1 className="page-title">Ustawinenia aplikacji</h1>
                                <FormItem
                                    label="Maksymalna ilość danych wyświetlana na wykresach"
                                    hasFeedback
                                    validateStatus={this.state.chartsMaxValuesCount.validateStatus}
                                    help={this.state.chartsMaxValuesCount.message}>
                                    <Input
                                        size="large"
                                        name="chartsMaxValuesCount"
                                        type="number"
                                        value={this.state.chartsMaxValuesCount.value}
                                        onChange={(event) => this.handleChange(event, this.validateChartValuesCount)}
                                    />
                                </FormItem>
                            </div>


                            <FormItem>
                                <Button type="primary"
                                        htmlType="submit"
                                        size="large"
                                        className="settings-form-button"
                                    disabled={!this.isFormValid()}
                                >Zapisz</Button>
                            </FormItem>
                        </Form>

                    )}
                </article>

            </div>
        );
    }

    validateString = (str) =>  {
        let validateStatus = 'success';
        let message = null;

        if (str.length < 0) {
            validateStatus = 'error';
            message = `Pole nie może być puste`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }
    };

    validatePort = (port) =>  {
        let validateStatus = 'success';
        let message = null;

        if (!this.isInt(port) || port < 0 || port > 65535) {
            validateStatus = 'error';
            message = `Pole powinno mieć wartość między 0 a 65535`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };
    };

    validateWorkersCount = (workersCount) =>  {
        let validateStatus = 'success';
        let message = null;

        if (!this.isInt(workersCount) || workersCount < 1 || workersCount > 100) {
            validateStatus = 'error';
            message = `Pole powinno mieć wartość między 1 a 100`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };
    };

    validateInterval = (interval) => {
        let validateStatus = 'success';
        let message = null;

        if (!this.isInt(interval) || interval < 1000 || interval > 360000) {
            validateStatus = 'error';
            message = `Pole powinno mieć wartość między 1000 a 360000`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

    };

    validateChartValuesCount = (count) => {
        let validateStatus = 'success';
        let message = null;

        if (!this.isInt(count) || count < 10 || count > 100000) {
            validateStatus = 'error';
            message = `Pole powinno mieć wartość między 10 a 100000`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

    };

    isInt = (value) => {
        return !isNaN(value) &&
            parseInt(Number(value)) == value &&
            !isNaN(parseInt(value, 10));
    };

    loadDetails() {
        let promise = getAppSetting();

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    webserviceWorkersCount: {value: response.webserviceWorkersCount, validateStatus: 'success'},
                    alertsCheckingInterval: {value: response.alertsCheckingInterval, validateStatus: 'success'},
                    smtpServer: {value: response.smtpServer, validateStatus: 'success'},
                    smtpUsername: {value: response.smtpUsername, validateStatus: 'success'},
                    smtpPassword: {value: response.smtpPassword, validateStatus: 'success'},
                    smtpPort: {value: response.smtpPort, validateStatus: 'success'},
                    smtpFromAddress: {value: response.smtpFromAddress, validateStatus: 'success'},
                    chartsMaxValuesCount: {value: response.chartsMaxValuesCount, validateStatus: 'success'},
                    smtpMailsFooterName: {value: response.smtpMailsFooterName, validateStatus: 'success'},
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                isLoading: false
            })
        });
    }
}

export default Settings;