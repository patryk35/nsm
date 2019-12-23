import React, {Component} from 'react';
import {editMonitoringAlert, loadMonitoringAlertDetails} from '../../../../utils/APIRequestsUtils';
import './MonitoringAlertEdit.css';
import {
    ALERT_MESSAGE_MAX_LENGTH,
    ALERT_MESSAGE_MIN_LENGTH,
    MONITORING_ALERT_VALUE_MAX_LENGTH,
    MONITORING_ALERT_VALUE_MIN_LENGTH
} from '../../../../configuration';

import {Button, Checkbox, Form, Icon, Input, notification, Select} from 'antd';
import {validateLevel, validateRecipients} from "../../shared/AlertsConfigurationShared";
import {Link} from "react-router-dom";
import LoadingSpin from "../../../../common/spin/LoadingSpin";

const FormItem = Form.Item;
const {Option} = Select;


class MonitoringAlertEdit extends Component {
    constructor(props) {
        super(props);
        this.state = {
            allowedConditions: [
                {key: "<", value: "<"},
                {key: "<=", value: "<="},
                {key: "=", value: "="},
                {key: "!=", value: "!="},
                {key: ">=", value: ">="},
                {key: ">", value: ">"}
            ],
            isLoading: true,
            parameters: []
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

    handleChangeCondition(event, validationFun) {
        this.setState({
            ["condition"]: {
                value: event,
                ...validationFun(event)
            }
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        const state = this.state;
        const editRequest = {
            alertId: this.props.match.params.id,
            message: state.message.value,
            condition: state.condition.value,
            value: state.val.value,
            enabled: state.enabled.value,
            alertLevel: state.level.value,
            emailNotification: state.emailNotification.value,
            recipients: state.emailNotification.value ? state.recipients.value : ""
        };
        editMonitoringAlert(editRequest)
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
        let promise = loadMonitoringAlertDetails(this.props.match.params.id);

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
                        validateStatus: true
                    },
                    serviceName: response.serviceName,
                    message: {value: response.message, validateStatus: 'success'},
                    condition: {value: response.condition, validateStatus: 'success'},
                    val: {value: response.value, validateStatus: 'success'},
                    enabled: {value: response.enabled, validateStatus: 'success'},
                    level: {value: response.alertLevel, validateStatus: 'success'},
                    emailNotification: {value: response.emailNotification, validateStatus: "success"},
                    recipients: {value: response.recipients, validateStatus: "success"},
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
        return state.message.validateStatus === 'success' && state.val.validateStatus === 'success' &&
            state.condition.validateStatus === 'success' && state.level.validateStatus === 'success' && (!state.emailNotification.value || state.recipients.validateStatus === 'success');
    }

    render() {
        return (
            <article className="monitoring-alert-edit-container">
                {this.state.isLoading ? (
                    <LoadingSpin/>
                ) : (
                    <div>.
                        <h1 className="page-title">Edycja konfiguracji alertu dla
                            serwisu <b>{this.state.serviceName}</b></h1>
                        <div className="monitoring-alert-edit-content">
                            <Form onSubmit={this.handleSubmit} className="monitoring-alert-edit-form">
                                <FormItem label="Parametr">
                                    <Input
                                        prefix={<Icon type="robot"/>}
                                        size="large"
                                        name="message"
                                        value={this.state.parameter.name}
                                        disabled={true}/>
                                </FormItem>
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
                                        <Option key='WARN'>Ostrzeżenie</Option>
                                        <Option key='ERROR'>Błąd</Option>

                                    </Select>
                                </FormItem>
                                <FormItem
                                    label="Warunek"
                                    hasFeedback
                                    validateStatus={this.state.condition.validateStatus}
                                    help={this.state.condition.message}>
                                    <Select
                                        value={this.state.condition.value}
                                        onChange={(event) => this.handleChangeCondition(event, this.validateCondition)}>
                                        {this.state.allowedConditions.map(function (record) {
                                            return <Option key={record.key}
                                                           title={record.value}>{record.value}</Option>;
                                        })}
                                    </Select>
                                </FormItem>
                                <FormItem
                                    label="Wartość"
                                    hasFeedback
                                    validateStatus={this.state.val.validateStatus}
                                    help={this.state.val.message}>
                                    <Input
                                        prefix={<Icon type="number"/>}
                                        size="large"
                                        name="val"
                                        type="number"
                                        value={this.state.val.value}
                                        onChange={(event) => this.handleChange(event, this.validateValue)}/>
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
                                <FormItem
                                    label="Wiadomość e-mail"
                                    help={this.state.emailNotification.message}>
                                    <Checkbox
                                        checked={this.state.emailNotification.value}
                                        onChange={(event) => {
                                            this.setState({
                                                emailNotification: {
                                                    value: event.target.checked,
                                                    message: this.state.emailNotification.message
                                                }
                                            })
                                        }}>Tak</Checkbox>
                                </FormItem>
                                <FormItem
                                    label="Odbiorcy wiadomości e-mail"
                                    hasFeedback
                                    validateStatus={this.state.recipients.validateStatus}
                                    help={this.state.recipients.message}
                                    hidden={!this.state.emailNotification.value}
                                >
                                    <Input
                                        prefix={<Icon type="mail"/>}
                                        size="large"
                                        name="recipients"
                                        value={this.state.recipients.value}
                                        onChange={(event) => this.handleChange(event, validateRecipients)}/>
                                </FormItem>
                                <FormItem>
                                    <Button type="primary"
                                            htmlType="submit"
                                            size="large"
                                            className="monitoring-alert-edit-form-button"
                                            disabled={!this.isFormValid()}>Zapisz</Button>
                                    <Button className={"monitoring-alert-edit-back-button"}>
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

    validateCondition = (condition) => {
        let validateStatus = 'success';
        let message = null;
        if (condition === null) {
            validateStatus = 'error';
            message = `Pole powinno zostać uzupełnione`;
        }
        return {
            validateStatus: validateStatus,
            message: message
        };

    };

    validateValue = (val) => {
        let validateStatus = 'success';
        let message = null;

        if (val.length < MONITORING_ALERT_VALUE_MIN_LENGTH || val.length > MONITORING_ALERT_VALUE_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć między ${MONITORING_ALERT_VALUE_MIN_LENGTH} a ${MONITORING_ALERT_VALUE_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }

    };

}

export default MonitoringAlertEdit;