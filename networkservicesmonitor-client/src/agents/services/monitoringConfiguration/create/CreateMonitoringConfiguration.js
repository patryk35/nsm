import React, {Component} from 'react';
import {createMonitoringConfiguration, loadNewAvailableMonitoringParameters} from '../../../../utils/APIRequestsUtils';
import './CreateMonitoringConfiguration.css';
import {Link} from 'react-router-dom';
import {
    AGENT_MONITORING_PARAMETER_DESCRIPTION_MAX_LENGTH,
    AGENT_MONITORING_PARAMETER_DESCRIPTION_MIN_LENGTH,
    AGENT_SERVICE_DESCRIPTION_MAX_LENGTH,
    AGENT_SERVICE_DESCRIPTION_MIN_LENGTH
} from '../../../../configuration';

import {Button, Form, Icon, Input, notification, Select} from 'antd';

const FormItem = Form.Item;

const {Option} = Select;

class CreateMonitoringConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            parameter: {
                value: "",
                message: "Wybierz parametr do monitorowania"
            },
            description: {
                value: "",
                message: "Podaj opis. Wymagane " + AGENT_SERVICE_DESCRIPTION_MIN_LENGTH + " do " + AGENT_SERVICE_DESCRIPTION_MAX_LENGTH + " znaków"
            },
            monitoringInterval: {
                value: "",
                message: "Podaj odstęp monitorowania w milisekundach (minimum 100)"
            },
            isLoading: false,
            parameters: []
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.isFormValid = this.isFormValid.bind(this);
    }

    loadParametersList() {
        let promise = loadNewAvailableMonitoringParameters(this.props.match.params.serviceId);

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

    handleChangeParameter(event, validationFun) {
        this.setState({
            ["parameter"]: {
                value: event,
                ...validationFun(event)
            }
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        const state = this.state;
        const monitoringConfigurationRequest = {
            serviceId: this.props.match.params.serviceId,
            parameterTypeId: state.parameter.value,
            description: state.description.value,
            monitoringInterval: state.monitoringInterval.value
        };
        createMonitoringConfiguration(monitoringConfigurationRequest)
            .then((response) => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Konfiguracja została utworzona pomyślnie!',
                    btn,
                    key
                });
                this.props.history.goBack();
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się dodać konfiguracji',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.parameter.validateStatus === 'success' && state.description.validateStatus === 'success' && state.monitoringInterval.validateStatus;
    }

    render() {
        const state = this.state;
        return (
            <article className="agent-create-service-monitoring-configuration-container">
                <h1 className="page-title">Dodaj konfigurację monitoringu dla serwisu</h1>
                <div className="add-service-monitoring-configuration-content">
                    <Form onSubmit={this.handleSubmit} className="agent-create-service-monitoring-configuration-form">
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
                        </FormItem>
                    </Form>
                </div>
                <Button className={"agent-create-service-monitoring-configuration-back-button"}>
                    <Link onClick={() => {
                        this.props.history.goBack()
                    }}>Powrót</Link>
                </Button>
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
}

export default CreateMonitoringConfiguration;