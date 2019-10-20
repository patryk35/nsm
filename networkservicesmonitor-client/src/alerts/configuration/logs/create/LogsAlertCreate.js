import React, {Component} from 'react';
import {createLogsAlert} from '../../../../utils/APIRequestsUtils';
import './LogsAlertCreate.css';
import {
    ALERT_MESSAGE_MAX_LENGTH,
    ALERT_MESSAGE_MIN_LENGTH,
    LOGS_ALERT_SEARCH_STRING_MAX_LENGTH
} from '../../../../configuration';

import {Button, Form, Icon, Input, notification} from 'antd';

const FormItem = Form.Item;

class LogsAlertCreate extends Component {
    constructor(props) {
        super(props);
        this.state = {
            message: {
                value: "",
                message: "Wprowadź treść wiadomości wyświetlanej przy pojawieniu się alertu. " +
                    "Wiadomość powinna mieć między " + ALERT_MESSAGE_MIN_LENGTH + " a " +
                    ALERT_MESSAGE_MAX_LENGTH + " znaków"
            },
            pathSearchString: {
                value: "",
                message: "Wprowadź ścieżkę do monitorowanego logu lub jej część lub pozostaw puste. " +
                    "Maksymalnie " + LOGS_ALERT_SEARCH_STRING_MAX_LENGTH + " znaków"
            },
            searchString: {
                value: " ",
                message: "Wprowadź frazę szukaną w logu, dla której pojawi się alert lub pozostaw puste. " +
                    "Maksymalnie " + LOGS_ALERT_SEARCH_STRING_MAX_LENGTH + "znaków"
            },
            test: this.props.parent
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.isFormValid = this.isFormValid.bind(this);
    }

    handleChange(event, validationFun) {
        this.setState({
            [event.target.name]: {
                value: event.target.value,
                ...validationFun(event.target.value)
            }
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        const state = this.state;
        const createRequest = {
            serviceId: this.props.match.params.serviceId,
            message: state.message.value,
            pathSearchString: state.pathSearchString.value,
            searchString: state.searchString.value
        };
        createLogsAlert(createRequest)
            .then(response => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Alert utworzony pomyślnie!',
                    description: "",
                    btn,
                    key
                });
                this.props.history.push("/alerts/configuration/list/logs");
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się dodać alertu',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.message.validateStatus === 'success' && state.pathSearchString.validateStatus === 'success' &&
            state.searchString.validateStatus === 'success';
    }

    render() {
        return (
            <article className="logs-alert-create-container">
                <h1 className="page-title">Dodaj nowy alert dla wartości logów dla
                    serwisu {this.props.match.params.serviceName} </h1>
                <div className="logs-alert-create-content">
                    <Form onSubmit={this.handleSubmit} className="logs-alert-create-form">
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
                        <FormItem>
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="logs-alert-create-form-button"
                                    disabled={!this.isFormValid()}>Dodaj</Button>
                            <a onClick={() => {
                                this.props.history.goBack()
                            }}>Powrót</a>
                        </FormItem>
                    </Form>
                </div>
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

export default LogsAlertCreate;