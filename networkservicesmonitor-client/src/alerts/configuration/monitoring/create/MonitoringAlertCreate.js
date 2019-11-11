import React, {Component} from 'react';
import {createMonitoringAlert, loadServiceMonitoringParameters} from '../../../../utils/APIRequestsUtils';
import './MonitoringAlertCreate.css';
import {
    ALERT_MESSAGE_MAX_LENGTH,
    ALERT_MESSAGE_MIN_LENGTH,
    MONITORING_ALERT_VALUE_MAX_LENGTH,
    MONITORING_ALERT_VALUE_MIN_LENGTH
} from '../../../../configuration';

import {Button, Form, Icon, Input, notification, Select} from 'antd';
import {validateLevel} from "../../shared/AlertsConfigurationShared";
import {Link} from "react-router-dom";

const FormItem = Form.Item;
const {Option} = Select;


class MonitoringAlertCreate extends Component {
    constructor(props) {
        super(props);
        this.state = {
            message: {
                value: "",
                message: "Wprowadź treść wiadomości wyświetlanej przy pojawieniu się alertu. " +
                    "Wiadomość powinna mieć między " + ALERT_MESSAGE_MIN_LENGTH + " a " +
                    ALERT_MESSAGE_MAX_LENGTH + " znaków"
            },
            level: {
                value: "",
                message: "Wybierz poziom alertu"
            },
            parameter: {
                value: "",
                message: "Wybierz parametr, którego ma dotyczyć alert"
            },
            condition: {
                value: "",
                message: "Wybierz warunek po spełnieniu, którego utworzony zostanie alert."
            },
            val: {
                value: "",
                message: "Podaj wartość odniesienia dla warunku"
            },
            allowedConditions: [
                {key: "<", value: "<"},
                {key: "<=", value: "<="},
                {key: "=", value: "="},
                {key: "!=", value: "!="},
                {key: ">=", value: ">="},
                {key: ">", value: ">"}
            ],
            isLoading: false,
            parameters: []
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.isFormValid = this.isFormValid.bind(this);
    }

    componentDidMount() {
        this.loadParametersList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            this.setState({
                parameters: [],
                isLoading: false
            });
            this.loadParametersList();
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

    handleChangeParameter(event, validationFun) {
        this.setState({
            ["parameter"]: {
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
        const createRequest = {
            serviceId: this.props.match.params.serviceId,
            monitoredParameterTypeId: state.parameter.value,
            message: state.message.value,
            condition: state.condition.value,
            value: state.val.value,
            alertLevel: state.level.value
        };
        createMonitoringAlert(createRequest)
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
                this.props.history.push("/alerts/configuration/list/monitoring");
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

    loadParametersList() {
        let promise = loadServiceMonitoringParameters(this.props.match.params.serviceId);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.state.parameters.slice();
                this.setState({
                    parameters: response,
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                isLoading: false
            })
        });
    }

    isFormValid() {
        const state = this.state;
        return state.message.validateStatus === 'success' && state.val.validateStatus === 'success' &&
            state.condition.validateStatus === 'success' && state.parameter.validateStatus  &&
            state.level.validateStatus === 'success';
    }

    render() {
        return (
            <article className="monitoring-alert-create-container">
                <h1 className="page-title">Dodaj nowy alert dla wartości monitoingu dla
                    serwisu {this.props.match.params.serviceName} </h1>
                <div className="monitoring-alert-create-content">
                    <Form onSubmit={this.handleSubmit} className="monitoring-alert-create-form">
                        <FormItem
                            label="Parametr"
                            hasFeedback
                            validateStatus={this.state.parameter.validateStatus}
                            help={this.state.parameter.message}>
                            <Select
                                onChange={(event) => this.handleChangeParameter(event, this.validateParameter)}>
                                {this.state.parameters &&
                                this.state.parameters.map(function (record) {
                                    return <Option key={record.id} title={record.description}>{record.name}</Option>;
                                })}
                            </Select>
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
                                onChange={(event) => this.handleChangeCondition(event, this.validateCondition)}>
                                {this.state.allowedConditions.map(function (record) {
                                    return <Option key={record.key} title={record.value}>{record.value}</Option>;
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
                                value={this.state.val.value}
                                onChange={(event) => this.handleChange(event, this.validateValue)}/>
                        </FormItem>
                        <FormItem>
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="monitoring-alert-create-form-button"
                                    disabled={!this.isFormValid()}>Dodaj</Button>
                            <Button className={"monitoring-alert-create-back-button"}>
                                <Link onClick={() => {
                                    this.props.history.goBack()
                                }}>Powrót</Link>
                            </Button>
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

    validateParameter = (parameter) => {
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

    validateCondition = (condition) => {
        let validateStatus = 'success';
        let message = null;
        if (condition === null) {
            validateStatus = 'error';
            message = `Pole powinno zostać uzupełnione`;
        }
        // TODO(high): Should check whether it is a string or number and disable some operators for string
        return {
            validateStatus: validateStatus,
            message: message
        };

    };

    validateValue = (val) => {
        let validateStatus = 'success';
        let message = null;

        // TODO(high): Should check whether parameter type is string or number and check value field value
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

export default MonitoringAlertCreate;