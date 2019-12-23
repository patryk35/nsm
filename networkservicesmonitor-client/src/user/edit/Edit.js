import React, {Component} from 'react';
import {
    changeEmail,
    changePassword,
    checkEmailAvailability,
    getUserEmail,
    validatePassword
} from '../../utils/APIRequestsUtils';
import './Edit.css';

import {Button, Form, Icon, Input, notification} from 'antd';
import {validateEmail, validatePassword as validatePasswordShared} from "../shared/SharedFunctions";
import LoadingSpin from "../../common/spin/LoadingSpin";

const FormItem = Form.Item;


class Edit extends Component {
    constructor(props) {
        super(props);
        this.loadDetails(this.props.match.params.login);
        this.state = {
            id: {value: "", message: ""},
            email: {value: "", message: "Podaj adres email"},
            currentPassword: {value: "", message: ""},
            password: {
                value: "",
                message: "Wymagane od 8 do 100 znaków oraz co najmniej jedna duża litera, mała litera, cyfra i znak specjalny"
            },
            passwordRetype: {value: "", message: "Wpisz hasło ponownie"},
            isLoading: true,
            originalEmail: ""
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.checkEmailAvailability = this.checkEmailAvailability.bind(this);
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
        const emailChangeRequest = {
            email: state.email.value,
        };
        changeEmail(emailChangeRequest)
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                this.setState({
                    originalEmail: state.email.value
                });
                notification.success({
                    message: 'Zapisano pomyślnie!',
                    description: "",
                    btn,
                    key
                });
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się zapisać danych!',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    handlePasswordSubmit(event) {
        event.preventDefault();
        const passwordChangeRequest = {
            password: this.state.currentPassword.value,
            newPassword: this.state.password.value,
        };
        changePassword(passwordChangeRequest)
            .then(() => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Zapisano pomyślnie!',
                    description: "",
                    btn,
                    key
                });
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się zapisać danych!',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.currentPassword.validateStatus === 'success' && state.password.validateStatus === 'success' &&
            state.passwordRetype.validateStatus === 'success';
    }

    render() {
        return (
            <div>
                <article className="edit-user-container">
                    {this.state.isLoading ? (
                        <LoadingSpin/>
                    ) : (
                        <div>
                            <h1 className="page-title">Podstawowe dane</h1>
                            <div className="edit-user-content">
                                <Form onSubmit={this.handleSubmit} className="edit-user-form">
                                    <FormItem
                                        label="Email"
                                        hasFeedback
                                        validateStatus={this.state.email.validateStatus}
                                        help={this.state.email.message}>
                                        <Input
                                            prefix={<Icon type="mail"/>}
                                            size="large"
                                            name="email"
                                            type="email"
                                            placeholder="mail@poczta.com"
                                            value={this.state.email.value}
                                            onBlur={this.checkEmailAvailability}
                                            onChange={(event) => this.handleChange(event, validateEmail)}/>
                                    </FormItem>
                                    <FormItem>
                                        <Button type="primary"
                                                htmlType="submit"
                                                size="large"
                                                className="edit-user-form-button"
                                                disabled={!(this.state.email.validateStatus === 'success')}>Zapisz</Button>
                                    </FormItem>
                                </Form>
                            </div>
                        </div>
                    )}
                </article>
                <article className="edit-user-container">
                    <h1 className="page-title">Hasło</h1>
                    <div className="edit-user-content">
                        <Form onSubmit={this.handlePasswordSubmit.bind(this)} className="edit-user-form">
                            <FormItem
                                label="Obecne hasło"
                                hasFeedback
                                validateStatus={this.state.currentPassword.validateStatus}
                                help={this.state.currentPassword.message}>
                                <Input
                                    prefix={<Icon type="lock"/>}
                                    size="large"
                                    name="currentPassword"
                                    type="password"
                                    value={this.state.currentPassword.value}
                                    onBlur={this.validateCurrentPassword}
                                    onChange={(event) => this.setState({
                                        [event.target.name]: {
                                            value: event.target.value
                                        }
                                    })}/>
                            </FormItem>
                            <FormItem
                                label="Nowe hasło"
                                validateStatus={this.state.password.validateStatus}
                                help={this.state.password.message}>
                                <Input
                                    prefix={<Icon type="lock"/>}
                                    size="large"
                                    name="password"
                                    type="password"
                                    value={this.state.password.value}
                                    onChange={(event) => this.handleChange(event, validatePasswordShared)}/>
                            </FormItem>
                            <FormItem
                                label="Powtórz nowe hasło"
                                validateStatus={this.state.passwordRetype.validateStatus}
                                help={this.state.passwordRetype.message}>
                                <Input
                                    prefix={<Icon type="lock"/>}
                                    size="large"
                                    name="passwordRetype"
                                    type="password"
                                    value={this.state.passwordRetype.value}
                                    onChange={(event) => this.handleChange(event, this.validatePasswordRetype)}/>
                            </FormItem>
                            <FormItem>
                                <Button type="primary"
                                        htmlType="submit"
                                        size="large"
                                        className="edit-user-form-button"
                                        disabled={!this.isFormValid()}>Zapisz</Button>
                            </FormItem>
                        </Form>
                    </div>


                </article>
            </div>
        );
    }

    validateCurrentPassword = () => {
        const passwordValue = this.state.currentPassword.value;

        this.setState({
            currentPassword: {
                value: passwordValue,
                validateStatus: 'validating',
                message: null
            }
        });

        validatePassword(passwordValue)
            .then(response => {
                if (response.success === true) {
                    this.setState({
                        currentPassword: {
                            value: passwordValue,
                            validateStatus: 'success',
                            message: null
                        }
                    });
                } else {
                    this.setState({
                        currentPassword: {
                            value: passwordValue,
                            validateStatus: 'error',
                            message: "Hasło nie jest poprawne"
                        }
                    });
                }
            }).catch(error => {
            this.setState({
                currentPassword: {
                    value: passwordValue,
                    validateStatus: 'error',
                    message: "Wystąpił błąd podczas sprawdzania hasła. Spróbuj ponownie za chwilę!"
                }
            });
        });
    };

    validatePasswordRetype = (passwordRetype, password) => {
        let validateStatus = 'success';
        let message = null;
        if (passwordRetype !== this.state.password.value) {
            validateStatus = 'error';
            message = `Hasła niezgodne`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

    };


    checkEmailAvailability() {
        const emailValue = this.state.email.value;
        const emailValidation = validateEmail(emailValue);

        if (emailValidation.validateStatus === 'error') {
            this.setState({
                email: {
                    value: emailValue,
                    ...emailValidation
                }
            });
            return;
        }

        this.setState({
            email: {
                value: emailValue,
                validateStatus: 'validating',
                message: null
            }
        });

        checkEmailAvailability(emailValue)
            .then(response => {
                if (response.available) {
                    this.setState({
                        email: {
                            value: emailValue,
                            validateStatus: 'success',
                            message: null
                        }
                    });
                } else {
                    if (emailValue === this.state.originalEmail) {
                        this.setState({
                            email: {
                                value: emailValue,
                                validateStatus: '',
                                message: null
                            }
                        });
                    } else {
                        this.setState({
                            email: {
                                value: emailValue,
                                validateStatus: 'error',
                                message: 'Adres Email jest już używany.'
                            }
                        });
                    }
                }
            }).catch(error => {
            this.setState({
                email: {
                    value: emailValue,
                    validateStatus: 'success',
                    message: null
                }
            });
        });
    }

    loadDetails(login) {
        let promise = getUserEmail(login);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    email: {value: response.email},
                    originalEmail: response.email,
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                isLoading: false
            })
        });
    }
}

export default Edit;