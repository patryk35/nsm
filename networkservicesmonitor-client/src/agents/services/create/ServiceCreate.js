import React, {Component} from 'react';
import {checkServiceNameAvailability, createAgentService} from '../../../utils/APIRequestsUtils';
import './ServiceCreate.css';
import {Link} from 'react-router-dom';
import {
    AGENT_SERVICE_DESCRIPTION_MAX_LENGTH,
    AGENT_SERVICE_DESCRIPTION_MIN_LENGTH,
    AGENT_SERVICE_NAME_MAX_LENGTH,
    AGENT_SERVICE_NAME_MIN_LENGTH
} from '../../../configuration';

import {Button, Form, Icon, Input, notification} from 'antd';

const FormItem = Form.Item;


class ServiceCreate extends Component {
    constructor(props) {
        super(props);
        this.state = {
            serviceName: {
                value: "",
                message: "Podaj nazwę servisu. Wymagane " + AGENT_SERVICE_NAME_MIN_LENGTH + " do " + AGENT_SERVICE_NAME_MAX_LENGTH + " znaków"
            },
            description: {
                value: "",
                message: "Podaj opis. Wymagane " + AGENT_SERVICE_DESCRIPTION_MIN_LENGTH + " do " + AGENT_SERVICE_DESCRIPTION_MAX_LENGTH + " znaków"
            }
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
        const agentServiceCreateRequest = {
            name: state.serviceName.value,
            description: state.description.value,
            agentId: this.props.match.params.agentId
        };
        createAgentService(agentServiceCreateRequest)
            .then((response) => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Serwis został utworzony pomyślnie!',
                    btn,
                    key
                });
                this.props.history.push("/agents/" + this.props.match.params.agentId + "/" + this.props.match.params.agentName + "/service/edit/" + response.id);
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się dodać serwisu',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.serviceName.validateStatus === 'success' && state.description.validateStatus === 'success';
    }


    render() {
        const state = this.state;
        return (
            <article className="agent-create-service-container">
                <h1 className="page-title">Dodaj serwis dla agenta <b>{this.props.match.params.agentName}</b></h1>
                <div className="add-service-content">
                    <Form onSubmit={this.handleSubmit} className="agent-create-service-form">
                        <FormItem label="Nazwa serwisu"
                                  hasFeedback
                                  validateStatus={this.state.serviceName.validateStatus}
                                  help={this.state.serviceName.message}>
                            <Input
                                prefix={<Icon type="robot"/>}
                                size="large"
                                name="serviceName"
                                value={this.state.serviceName.value}
                                onBlur={this.checkServiceNameAvailability}
                                onChange={(event) => this.handleChange(event, this.validateServiceName)}/>
                        </FormItem>
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
                        <FormItem>
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="agent-create-form-button"
                                    disabled={!this.isFormValid()}>Dodaj</Button>
                        </FormItem>
                    </Form>
                    <Button className={"agent-create-service-back-button"}>
                        <Link onClick={() => {
                            this.props.history.goBack()
                        }}>Powrót</Link>
                    </Button>
                </div>
            </article>
        );
    }

    // Validation Functions
    validateServiceName = (name) => {
        let validateStatus = 'success';
        let message = null;

        if (name.length < AGENT_SERVICE_NAME_MIN_LENGTH || name.length > AGENT_SERVICE_NAME_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć między ${AGENT_SERVICE_NAME_MIN_LENGTH} a ${AGENT_SERVICE_NAME_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

    };


    validateDescription = (description) => {
        let validateStatus = 'success';
        let message = null;
        if (description.length > AGENT_SERVICE_DESCRIPTION_MAX_LENGTH || description.length < AGENT_SERVICE_DESCRIPTION_MIN_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć między ${AGENT_SERVICE_DESCRIPTION_MIN_LENGTH} a ${AGENT_SERVICE_DESCRIPTION_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }

    };

    checkServiceNameAvailability = () => {
        const serviceNameValue = this.state.serviceName.value;
        const serviceNameValidation = this.validateServiceName(serviceNameValue);

        if (serviceNameValidation.validateStatus === 'error') {
            this.setState({
                serviceName: {
                    value: serviceNameValue,
                    ...serviceNameValidation
                }
            });
            return;
        }

        this.setState({
            serviceName: {
                value: serviceNameValue,
                validateStatus: 'validating',
                message: null
            }
        });

        checkServiceNameAvailability(serviceNameValue, this.props.match.params.agentId)
            .then(response => {
                if (response.available) {
                    this.setState({
                        serviceName: {
                            value: serviceNameValue,
                            validateStatus: 'success',
                            message: null
                        }
                    });
                } else {
                    this.setState({
                        serviceName: {
                            value: serviceNameValue,
                            validateStatus: 'error',
                            message: 'Nazwa serwisu jest zajęta'
                        }
                    });
                }
            }).catch(error => {
            this.setState({
                serviceName: {
                    value: serviceNameValue,
                    validateStatus: 'success',
                    message: null
                }
            });
        });
    };

}

export default ServiceCreate;