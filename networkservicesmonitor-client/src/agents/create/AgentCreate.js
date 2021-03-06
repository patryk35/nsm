import React, {Component} from 'react';
import {checkAgentNameAvailability, createAgent} from '../../utils/APIRequestsUtils';
import './AgentCreate.css';
import {Link} from 'react-router-dom';
import {
    AGENT_DESCRIPTION_MAX_LENGTH,
    AGENT_DESCRIPTION_MIN_LENGTH,
    AGENT_NAME_MAX_LENGTH,
    AGENT_NAME_MIN_LENGTH
} from '../../configuration';

import {Button, Checkbox, Form, Icon, Input, notification} from 'antd';
import {validateAllowedOrigins} from "../shared/AgentShared";

const FormItem = Form.Item;


class AgentCreate extends Component {
    constructor(props) {
        super(props);
        this.state = {
            agentName: {
                value: "",
                message: "Podaj nazwę agenta. Wymagane " + AGENT_NAME_MIN_LENGTH + " do " + AGENT_NAME_MAX_LENGTH + " znaków"
            },
            description: {
                value: "",
                message: "Podaj opis. Wymagane " + AGENT_DESCRIPTION_MIN_LENGTH + " do " + AGENT_DESCRIPTION_MAX_LENGTH + " znaków"
            },
            allowedOrigins: {
                value: "",
                message: "Dozwolone adresy IP agenta. Podaj * lub adresy ip oddzielone przecinkami. Pozostaw puste by automatycznie uzupełnienić podczas pierwszego połączenia",
                validateStatus: "success"
            },
            agentId: {value: null},
            agentKey: {
                value: null,
                message: "Zachowaj wygenreowany klucz. Odzyskanie klucza nie jest możliwe po opuszczeniu strony"
            },
            proxy: {
                value: false,
                message: "Zaznacz jeżeli agent ma pełnić rolę proxy dla innych agentów"
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
            allowedOrigins: state.allowedOrigins.value,
            isProxyAgent: state.proxy.value
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
                <div>
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
                                    onBlur={this.checkAgentNameAvailability}
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
                                    onChange={(event) => this.handleChange(event, validateAllowedOrigins)}/>
                            </FormItem>
                            <FormItem
                                label="Agent proxy"
                                help={this.state.proxy.message}>
                                <Checkbox onChange={(event) => {
                                    this.setState({
                                        proxy: {
                                            value: event.target.checked,
                                            message: this.state.proxy.message
                                        }
                                    })
                                }}>Tak</Checkbox>
                            </FormItem>


                            <FormItem>
                                <Button type="primary"
                                        htmlType="submit"
                                        size="large"
                                        className="agent-create-form-button"
                                        disabled={!this.isFormValid()}>Dodaj</Button>
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
                <Button className={"agent-create-back-button"}>
                    <Link onClick={() => {
                        this.props.history.goBack()
                    }}>Powrót</Link>
                </Button>
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

    checkAgentNameAvailability = () => {
        const agentNameValue = this.state.agentName.value;
        const agentNameValidation = this.validateAgentName(agentNameValue);

        if (agentNameValidation.validateStatus === 'error') {
            this.setState({
                agentName: {
                    value: agentNameValue,
                    ...agentNameValidation
                }
            });
            return;
        }

        this.setState({
            agentName: {
                value: agentNameValue,
                validateStatus: 'validating',
                message: null
            }
        });

        checkAgentNameAvailability(agentNameValue)
            .then(response => {
                if (response.available) {
                    this.setState({
                        agentName: {
                            value: agentNameValue,
                            validateStatus: 'success',
                            message: null
                        }
                    });
                } else {
                    this.setState({
                        agentName: {
                            value: agentNameValue,
                            validateStatus: 'error',
                            message: 'Nazwa agenta jest zajęta'
                        }
                    });
                }
            }).catch(error => {
            this.setState({
                agentName: {
                    value: agentNameValue,
                    validateStatus: 'success',
                    message: null
                }
            });
        });
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
}

export default AgentCreate;