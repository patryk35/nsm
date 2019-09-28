import React, {Component} from 'react';
import {createAgentService, editService, getAgentServiceDetails} from '../../../utils/APIRequestsUtils';
import './ServiceEdit.css';
import {Link} from 'react-router-dom';
import {AGENT_SERVICE_DESCRIPTION_MAX_LENGTH, AGENT_SERVICE_DESCRIPTION_MIN_LENGTH} from '../../../configuration';

import {Button, Form, Icon, Input, notification} from 'antd';
import LogsConfigurationList from "../logsConfiguration/list/LogsConfigurationList";
import MonitoringConfigurationList from "../monitoringConfiguration/list/MonitoringConfigurationList";

const FormItem = Form.Item;


class ServiceEdit extends Component {
    constructor(props) {
        super(props);
        this.loadDetails(this.props.match.params.serviceId);
        this.state = {
            serviceId: {
                value: "",
            },
            serviceName: {
                value: "",
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
        const agentServiceEditRequest = {
            serviceId: state.serviceId.value,
            description: state.description.value
        };
        editService(agentServiceEditRequest)
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Serwis został zaktualizowany pomyślnie!',
                    btn,
                    key
                });
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się zaktualizować serwisu',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.description.validateStatus === 'success';
    }

    render() {
        return (
            <article className="agent-edit-service-container">
                <h1 >Edycja serwisu <b>{this.state.serviceName.value}</b></h1>
                <div className="agent-edit-service-subcontainer">
                    <Form onSubmit={this.handleSubmit} className="agent-edit-service-form">
                        <FormItem label="Id serwisu">
                            <Input
                                prefix={<Icon type="tag"/>}
                                size="large"
                                name="serviceName"
                                value={this.state.serviceId.value}
                                disabled={true}/>
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
                                    className="agent-edit-form-button"
                                    disabled={!this.isFormValid()}>Zapisz</Button>
                            <Link to="/agents">Powrót do listy</Link>
                        </FormItem>
                    </Form>
                </div>
                <div className="agent-edit-service-subcontainer">
                    <h4>Konfiguracja zbierania logów</h4>
                    <LogsConfigurationList serviceId={this.props.match.params.serviceId} editAccess={true}></LogsConfigurationList>

                </div>
                <div className="agent-edit-service-subcontainer">
                    <h4>Konfiguracja monitorowania parametrów</h4>
                    <MonitoringConfigurationList serviceId={this.props.match.params.serviceId} editAccess={true}></MonitoringConfigurationList>
                </div>
            </article>
        );
    }

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

    loadDetails(id) {
        let promise = getAgentServiceDetails(id);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    serviceId: {value: response.serviceId},
                    serviceName: {value: response.name},
                    description: {value: response.description},
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                isLoading: false
            })
        });
    }
}

export default ServiceEdit;