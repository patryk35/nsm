import React, {Component} from 'react';
import {
    createMonitoringConfiguration,
    editMonitoringConfiguration, getMonitoringConfigurationDetails,
    loadNewAvailableMonitoringParameters
} from '../../../../utils/APIRequestsUtils';
import './EditMonitoringConfiguration.css';
import {Link} from 'react-router-dom';
import {
    AGENT_MONITORING_PARAMETER_DESCRIPTION_MAX_LENGTH,
    AGENT_MONITORING_PARAMETER_DESCRIPTION_MIN_LENGTH,
    AGENT_SERVICE_DESCRIPTION_MAX_LENGTH,
    AGENT_SERVICE_DESCRIPTION_MIN_LENGTH
} from '../../../../configuration';

import {Button, Form, Icon, Input, notification, Select} from 'antd';

const FormItem = Form.Item;

const { Option } = Select;

class EditMonitoringConfiguration extends Component {
    constructor(props) {
        super(props);
        this.loadDetails(this.props.match.params.configurationId)
        this.state = {
            parameter: {
                value: ""
            },
            description: {
                value: "",
                message: "Podaj opis. Wymagane " + AGENT_SERVICE_DESCRIPTION_MIN_LENGTH + " do " + AGENT_SERVICE_DESCRIPTION_MAX_LENGTH + " znaków"
            },
            monitoringInterval: {
                value: "",
                message: "Podaj odstęp monitorowania w milisekundach (minimum 100)"
            },
            isLoading: false
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
        const monitoringConfigurationEditRequest = {
            configurationId: this.props.match.params.configurationId,
            description: state.description.value,
            monitoringInterval: state.monitoringInterval.value
        };
        editMonitoringConfiguration(monitoringConfigurationEditRequest)
            .then((response) => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Konfiguracja została zaktualizowana pomyślnie!',
                    btn,
                    key
                });
                this.props.history.goBack();
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się zaktualizować konfiguracji',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.description.validateStatus === 'success' && state.monitoringInterval.validateStatus;
    }

    render() {
        return (
            <article className="agent-edit-service-monitoring-configuration-container">
                <h1 className="page-title">Dodaj konfigurację monitoringu dla serwisu</h1>
                <div className="agent-edit-service-monitoring-configuration-content">
                    <Form onSubmit={this.handleSubmit} className="agent-edit-service-monitoring-configuration-form">
                        <FormItem
                            label="Opis"
                            hasFeedback
                            validateStatus={this.state.description.validateStatus}
                            help={this.state.description.message}>
                            <Input
                                prefix={<Icon type="read"/>}
                                size="large"
                                name="description"
                                value={this.state.description.value}
                                onChange={(event) => this.handleChange(event, this.validateDescription)}/>
                        </FormItem>
                        <FormItem label="Parametr">
                            <Select defaultValue="default" disabled>
                                <Option value="default">{this.state.parameter.value}</Option>
                            </Select>
                        </FormItem>
                        <FormItem
                            label="Odstęp monitorowania"
                            hasFeedback
                            validateStatus={this.state.monitoringInterval.validateStatus}
                            help={this.state.monitoringInterval.message}>
                            <Input
                                prefix={<Icon type="number"/>}
                                size="large"
                                name="monitoringInterval"
                                value={this.state.monitoringInterval.value}
                                onChange={(event) => this.handleChange(event, this.validateMonitoringInterval)}/>
                        </FormItem>
                        <FormItem>
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="agent-create-service-monitoring-configuration-form-button"
                                    disabled={!this.isFormValid()}>Dodaj</Button>
                            <Link to="/agents">Powrót do listy</Link>
                        </FormItem>
                    </Form>
                </div>
            </article>
        );
    }

    // Validation Functions
    validateMonitoringInterval = (monitoringInterval) => {
        let validateStatus = 'success';
        let message = null;

        if (!this.isInt(monitoringInterval) || monitoringInterval < 100) {
            validateStatus = 'error';
            message = `Pole powinno mieć wartość co najmniej 100`;
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

    validateDescription = (description) => {
        let validateStatus = 'success';
        let message = null;
        if (description.length > AGENT_MONITORING_PARAMETER_DESCRIPTION_MAX_LENGTH || description.length < AGENT_MONITORING_PARAMETER_DESCRIPTION_MIN_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć między ${AGENT_MONITORING_PARAMETER_DESCRIPTION_MIN_LENGTH} a ${AGENT_MONITORING_PARAMETER_DESCRIPTION_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }

    };

    loadDetails(id) {
        let promise = getMonitoringConfigurationDetails(id);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    parameter: {value: response.parameterName},
                    description: {value: response.description},
                    monitoringInterval: {value: response.monitoringInterval},
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                isLoading: false
            })
        });
    };
}

export default EditMonitoringConfiguration;