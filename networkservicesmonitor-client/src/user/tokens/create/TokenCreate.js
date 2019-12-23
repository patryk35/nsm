import React, {Component} from 'react';
import {createUserToken} from '../../../utils/APIRequestsUtils';
import './TokenCreate.css';
import {Link} from 'react-router-dom';


import {Button, Checkbox, DatePicker, Form, Icon, Input, notification} from 'antd';
import moment from "moment";

const FormItem = Form.Item;


class TokenCreate extends Component {
    constructor(props) {
        super(props);
        this.state = {
            name: {
                value: "",
                message: "Podaj nazwę dla tokenu"
            },
            expirationTime: {
                value: "",
                message: "Wybierz czas ważności tokenu"
            },
            methodOptions: {
                message: "Wybierz przynajmniej jedną wartość",
                validateStatus: "success"
            },
            endpointOption: {
                message: "Wybierz przynajmniej jedną wartość",
                validateStatus: "success"
            },
            mGet: false,
            mPost: false,
            mPatch: false,
            mDelete: false,
            eUser: false,
            eAgent: false,
            eLogs: false,
            eMonitoring: false,
            eAlerts: false,
            eAlertsConfiguration: false,
            token: null
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
            id: null,
            name: state.name.value,
            expirationTime: (state.expirationTime.value !== null) ? state.expirationTime.value.format("YYYY-MM-DD HH:mm:ss") : null,
            allowedMethods: this.createAllowedMethodsResponse(),
            allowedEndpoints: this.createAllowedEndpointsResponse(),
        };
        createUserToken(createRequest)
            .then(response => {

                this.setState(prevState => ({
                    token: response.token
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

    createAllowedEndpointsResponse() {
        let result = "";
        if (this.state.eUser) {
            result += "/api/v1/users,";
        }
        if (this.state.eAgent) {
            result += "/api/v1/agent,";
        }
        if (this.state.eLogs) {
            result += "/api/v1/logs,";
        }
        if (this.state.eMonitoring) {
            result += "/api/v1/monitoring,";
        }
        if (this.state.eAlerts) {
            result += "/api/v1/alerts/config,";
        }
        if (this.state.eAlertsConfiguration) {
            result += "/api/v1/alerts/data,";
        }
        return result;
    }

    createAllowedMethodsResponse() {
        let result = "";
        if (this.state.mGet) {
            result += "GET,";
        }
        if (this.state.mPost) {
            result += "POST,";
        }
        if (this.state.mPatch) {
            result += "PATCH,";
        }
        if (this.state.mDelete) {
            result += "DELETE,";
        }
        return result;
    }

    isFormValid() {
        const state = this.state;
        return state.name.validateStatus === 'success' && state.expirationTime.validateStatus === 'success' &&
            (state.mGet || state.mPost || state.mPatch || state.mDelete) &&
            (state.eUser || state.eAgent || state.eLogs || state.eMonitoring || state.eAlerts || state.eAlertsConfiguration);
    }

    render() {
        const state = this.state;
        return (
            <article className="token-create-container">
                <h1 className="page-title">Stwórz nowy token</h1>
                <div>
                    {state.token === null ? (
                        <Form onSubmit={this.handleSubmit} className="token-create-form">
                            <FormItem label="Nazwa tokenu"
                                      hasFeedback
                                      validateStatus={this.state.name.validateStatus}
                                      help={this.state.name.message}>
                                <Input
                                    prefix={<Icon type="robot"/>}
                                    size="large"
                                    name="name"
                                    value={this.state.name.value}
                                    onChange={(event) => this.handleChange(event, this.validateName)}/>
                            </FormItem>
                            <FormItem
                                validateStatus={this.state.expirationTime.validateStatus}
                                help={this.state.expirationTime.message}
                                hasFeedback>
                                <DatePicker placeholder="Czas ważności" className={"token-create-date-picker"}
                                            disabledDate={d => !d || d.isBefore(moment())}
                                            value={this.state.expirationTime.value}
                                            size={"large"}
                                            onChange={(date) => {
                                                this.setState({
                                                    expirationTime: {
                                                        value: date,
                                                        validateStatus: date === null ? "error" : "success",
                                                        message: date === null ? "Pole jest wymagane" : "",
                                                    }
                                                });
                                            }}/>
                            </FormItem>
                            <FormItem
                                label="Dozwolone metody"
                                validateStatus={this.state.methodOptions.validateStatus}
                                help={this.state.methodOptions.message}
                            >
                                <Checkbox onChange={(event) => {
                                    this.setState({
                                        mGet: event.target.checked
                                    })
                                }}>GET</Checkbox>
                                <Checkbox onChange={(event) => {
                                    this.setState({
                                        mPost: event.target.checked
                                    })
                                }}>POST</Checkbox>
                                <Checkbox onChange={(event) => {
                                    this.setState({
                                        mPatch: event.target.checked
                                    })
                                }}>PATCH</Checkbox>
                                <Checkbox onChange={(event) => {
                                    this.setState({
                                        mDelete: event.target.checked
                                    })
                                }}>DELETE</Checkbox>

                            </FormItem>
                            <FormItem
                                label="Token ma pozwalać na dostęp do:"
                                validateStatus={this.state.endpointOption.validateStatus}
                                help={this.state.endpointOption.message}
                            >
                                <div>
                                    <Checkbox className="token-create-checkbox-first" onChange={(event) => {
                                        this.setState({
                                            eUser: event.target.checked
                                        })
                                    }}>Dane użytkowników</Checkbox>
                                    <Checkbox className="token-create-checkbox" onChange={(event) => {
                                        this.setState({
                                            eAgent: event.target.checked
                                        })
                                    }}>Konfiguracja agentów i serwisów</Checkbox>
                                    <Checkbox className="token-create-checkbox" onChange={(event) => {
                                        this.setState({
                                            eLogs: event.target.checked
                                        })
                                    }}>Zebrane logi</Checkbox>
                                    <Checkbox className="token-create-checkbox" onChange={(event) => {
                                        this.setState({
                                            eMonitoring: event.target.checked
                                        })
                                    }}>Zebrane dane monitorowania</Checkbox>
                                    <Checkbox className="token-create-checkbox" onChange={(event) => {
                                        this.setState({
                                            eAlerts: event.target.checked
                                        })
                                    }}>Alerty</Checkbox>
                                    <Checkbox className="token-create-checkbox" onChange={(event) => {
                                        this.setState({
                                            eAlertsConfiguration: event.target.checked
                                        })
                                    }}>Konfiguracja alertów</Checkbox>
                                </div>
                            </FormItem>


                            <FormItem>
                                <Button type="primary"
                                        htmlType="submit"
                                        size="large"
                                        className="token-create-form-button"
                                        disabled={!this.isFormValid()}>Stwórz</Button>
                            </FormItem>
                        </Form>
                    ) : (
                        <Form className="token-create-form">
                            <FormItem
                                label="Twój token"
                                help="Zachowaj wygenreowany token. Odzyskanie tokenu nie jest możliwe po opuszczeniu strony">
                                <Input
                                    prefix={<Icon type="solution"/>}
                                    size="large"
                                    name="token"
                                    disabled={true}
                                    value={this.state.token}/>
                            </FormItem>
                        </Form>
                    )}
                </div>
                <Button className={"token-create-back-button"}>
                    <Link onClick={() => {
                        this.props.history.goBack()
                    }}>Powrót</Link>
                </Button>
            </article>
        );
    }

    // Validation Functions
    validateName = (name) => {
        let validateStatus = 'success';
        let message = null;

        if (name.length < 1) {
            message = "Token musi posiadać nazwę";
            validateStatus = "error";
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

    };

}

export default TokenCreate;