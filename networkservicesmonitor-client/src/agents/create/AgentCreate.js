import React, {Component} from 'react';
import {createAgent} from '../../utils/APIRequestsUtils';
import './AgentCreate.css';
import {Link} from 'react-router-dom';
import {
    AGENT_ALLOWED_ORIGINS_MAX_LENGTH,
    AGENT_DESCRIPTION_MIN_LENGTH,
    AGENT_DESCRIPTION_MAX_LENGTH,
    AGENT_NAME_MAX_LENGTH,
    AGENT_NAME_MIN_LENGTH
} from '../../configuration';

import {Button, Form, Icon, Input, notification} from 'antd';

const FormItem = Form.Item;


class AgentCreate extends Component {
    constructor(props) {
        super(props);
        this.state = {
            agentName: {value: "", message: "Podaj nazwę agenta. Wymagane " + AGENT_NAME_MIN_LENGTH + "do " + AGENT_NAME_MAX_LENGTH + " znaków"},
            description: {value: "", message: "Podaj opis.Wymagane " + AGENT_DESCRIPTION_MIN_LENGTH + "do " + AGENT_DESCRIPTION_MAX_LENGTH + " znaków"},
            allowedOrigins: {
                value: " ",
                message: "Dozwolone adresy IP agenta. Podaj * lub adresy ip oddzielone przecinkami. Pozostaw puste by automatycznie uzupełnienić podczas pierwszego połączenia"
            },
            agentId: {value: null},
            agentKey: {
                value: null,
                message: "Zachowaj wygenreowany klucz. Odzyskanie klucza nie jest możliwe po opuszczeniu strony"
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
        const agentCreateRequest = {
            name: state.agentName.value,
            description: state.description.value,
            allowedOrigins: state.allowedOrigins.value
        };
        createAgent(agentCreateRequest)
            .then(response => {

                this.setState(prevState => ({
                    agentId: {
                        ...prevState.agentId,
                        value: response.agentId
                    },
                    agentKey: {
                        ...prevState.agentKey,
                        value: response.agentEncryptionKey
                    }
                }));
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Agent utworzony pomyślnie!',
                    description: "Przy pierwszym połączeniu nastąpi rejestracja agenta. Do połączenia użyj wygenerowanych danych z formularza",
                    btn,
                    key
                });
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się dodać agenta',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.agentName.validateStatus === 'success' && state.description.validateStatus === 'success' &&
            state.allowedOrigins.validateStatus === 'success';
    }

    render() {
        const state = this.state;
        return (
            <article className="agent-create-container">
                <h1 className="page-title">Dodaj agenta</h1>
                <div className="register-content">
                    {state.agentId.value === null ? (
                        <Form onSubmit={this.handleSubmit} className="agent-create-form">
                            <FormItem label="Nazwa agenta"
                                      hasFeedback
                                      validateStatus={this.state.agentName.validateStatus}
                                      help={this.state.agentName.message}>
                                <Input
                                    prefix={<Icon type="robot"/>}
                                    size="large"
                                    name="agentName"
                                    value={this.state.agentName.value}
                                    onChange={(event) => this.handleChange(event, this.validateAgentName)}/>
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
                                    onBlur={(event) => this.handleChange(event, this.validateAllowedOrigins)}/>
                            </FormItem>


                            <FormItem>
                                <Button type="primary"
                                        htmlType="submit"
                                        size="large"
                                        className="agent-create-form-button"
                                        disabled={!this.isFormValid()}>Dodaj</Button>
                                <Link to="/agents">Powrót do listy</Link>
                            </FormItem>
                        </Form>
                    ) : (
                        <Form className="agent-create-form">
                            <FormItem
                                label="Identyfikator Agenta"
                                help={this.state.agentId.message}>
                                <Input
                                    prefix={<Icon type="solution"/>}
                                    size="large"
                                    name="agentId"
                                    disabled={true}
                                    value={this.state.agentId.value}/>
                            </FormItem>
                            <FormItem
                                label="Klucz agenta (agent secret key)"
                                validateStatus="error"
                                help={this.state.agentKey.message}>
                                <Input
                                    prefix={<Icon type="key"/>}
                                    size="large"
                                    name="agentKey"
                                    disabled={true}
                                    value={this.state.agentKey.value}/>
                            </FormItem>
                        </Form>
                    )}
                </div>
            </article>
        );
    }

    // Validation Functions
    validateAgentName = (name) => {
        let validateStatus = 'success';
        let message = null;

        if (name.length < AGENT_NAME_MIN_LENGTH || name.length > AGENT_NAME_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć między ${AGENT_NAME_MIN_LENGTH} a ${AGENT_NAME_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

    };


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
}

export default AgentCreate;