import React, {Component} from 'react';
import {editAgent, getAgentDetails} from '../../utils/APIRequestsUtils';
import './AgentEdit.css';
import {Link} from 'react-router-dom';
import {
    AGENT_ALLOWED_ORIGINS_MAX_LENGTH,
    AGENT_DESCRIPTION_MAX_LENGTH,
    AGENT_DESCRIPTION_MIN_LENGTH
} from '../../configuration';

import {Button, Form, Icon, Input, notification} from 'antd';
import LoadingSpin from "../../common/LoadingSpin";

const FormItem = Form.Item;


class AgentEdit extends Component {
    constructor(props) {
        super(props);
        this.loadDetails(this.props.match.params.id);
        this.state = {
            isLoading: true
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
        const agentEditRequest = {
            agentId: state.agentId.value,
            description: state.description.value,
            allowedOrigins: state.allowedOrigins.value,
            sendingInterval: state.sendingInterval.value
        };
        editAgent(agentEditRequest)
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Zmainay zostały zapisane!',
                    btn,
                    key
                });
                this.props.history.push("/agents");
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się zapisać danych',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.description.validateStatus === 'success' &&
            state.allowedOrigins.validateStatus === 'success' && state.sendingInterval.validateStatus;
    }

    render() {
        return (
            <article className="agent-edit-container">
                {this.state.isLoading ? (
                    <div>Trwa wczytywanie danych <LoadingSpin/></div>
                ) : (
                    <div>
                        <h1 className="page-title">Edycja agenta <b>{this.state.agentName.value}</b></h1>
                        <Form onSubmit={this.handleSubmit} className="agent-edit-form">
                            <FormItem label="Id">
                                <Input
                                    prefix={<Icon type="tag"/>}
                                    size="large"
                                    name="agentId"
                                    value={this.state.agentId.value}
                                    disabled={true}
                                />
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
                            <FormItem
                                label="Dozwolone adresy IP, z których łączy się agent"
                                hasFeedback
                                validateStatus={this.state.allowedOrigins.validateStatus}
                                help={this.state.allowedOrigins.message}>
                                <Input
                                    prefix={<Icon type="cluster"/>}
                                    size="large"
                                    name="allowedOrigins"
                                    value={this.state.allowedOrigins.value}
                                    onChange={(event) => this.handleChange(event, this.validateAllowedOrigins)}/>
                            </FormItem>
                            <FormItem
                                label="Częstotliwość wysyłania pakietów"
                                hasFeedback
                                validateStatus={this.state.sendingInterval.validateStatus}
                                help={this.state.sendingInterval.message}>
                                <Input
                                    prefix={<Icon type="number"/>}
                                    size="large"
                                    name="sendingInterval"
                                    value={this.state.sendingInterval.value}
                                    onChange={(event) => this.handleChange(event, this.validateSendingInterval)}/>
                            </FormItem>

                            <FormItem>
                                <Button type="primary"
                                        htmlType="submit"
                                        size="large"
                                        className="agent-edit-form-button"
                                        disabled={!this.isFormValid()}>Zapisz</Button>
                            </FormItem>
                        </Form>
                        <Button className={"agent-edit-back-button"}>
                            <Link onClick={() => {
                                this.props.history.goBack()
                            }}>Powrót</Link>
                        </Button>
                    </div>
                )}
            </article>
        );
    }

    // Validation Functions


    validateDescription = (description) => {
        let validateStatus = 'success';
        let message = null;
        if (description.length > AGENT_DESCRIPTION_MAX_LENGTH || description.length < AGENT_DESCRIPTION_MIN_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć między ${AGENT_DESCRIPTION_MIN_LENGTH} a ${AGENT_DESCRIPTION_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }

    };

    validateAllowedOrigins = (allowedOrigins) => {
        let validateStatus = 'success';
        let message = null;
        if (allowedOrigins.length > AGENT_ALLOWED_ORIGINS_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć maksymalnie ${AGENT_ALLOWED_ORIGINS_MAX_LENGTH} znaków`;
        }

        /* TODO: Create TODO regex
        } else if (!IP_REGEX.test(email)) {
            validateStatus = 'error';
            message = 'Podano adres jest nieprawidłowy';*/

        return {
            validateStatus: validateStatus,
            message: message
        }
    };

    validateSendingInterval = (sendingInterval) => {
        let validateStatus = 'success';
        let message = null;
        if (sendingInterval < 1) {
            validateStatus = 'error';
            message = `Pole powinno mieć wartośc nie mniejszą niż 100! `;
        } else if (!this.isInt(sendingInterval)) {
            validateStatus = 'error';
            message = `Pole musi być liczbą! `;
        }
        return {
            validateStatus: validateStatus,
            message: message
        }
    };

    isInt = (value) => {
        return !isNaN(value) &&
            parseInt(Number(value)) == value &&
            !isNaN(parseInt(value, 10));
    };

    loadDetails(id) {
        let promise = getAgentDetails(id);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    agentId: {value: response.agentId},
                    agentName: {value: response.name},
                    description: {value: response.description, validateStatus: "success"},
                    allowedOrigins: {value: response.allowedOrigins, validateStatus: "success"},
                    sendingInterval: {value: response.sendingInterval, validateStatus: "success"},
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
}

export default AgentEdit;