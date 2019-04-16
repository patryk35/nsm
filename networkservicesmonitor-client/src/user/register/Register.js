import React, {Component} from 'react';
import {register, checkUsernameAvailability, checkEmailAvailability} from '../../utils/APIRequestsUtils';
import './Register.css';
import {Link} from 'react-router-dom';
import {
    FULLNAME_MIN_LENGTH, FULLNAME_MAX_LENGTH,
    USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH,
    EMAIL_MAX_LENGTH,
    PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH
} from '../../configuration';

import {Form, Input, Button, notification, Icon} from 'antd';

const FormItem = Form.Item;
const EMAIL_REGEX = RegExp('^(([^<>()\\[\\]\\\\.,;:\\s@"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@"]+)*)|(".+"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$');


class Register extends Component {
    constructor(props) {
        super(props);
        this.state = {
            username: {value: "", message: "Podaj nazwę użytkownika. Wymagane conajmniej 3 znaki"},
            fullname: {value: "", message: "Podaj swoje imię i nazwisko. Wymagane conajmniej 3 znaki"},
            email: {value: "", message: "Podaj adres email"},
            password: {value: "", message: "Podaj hasło. Wymagane od 8 do 100 znaków"},
            passwordRetype: {value: "", message: "Wpisz hasło ponownie"}

        }

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.checkUsernameAvailability = this.checkUsernameAvailability.bind(this);
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
        const registerRequest = {
            username: state.username.value,
            fullname: state.fullname.value,
            email: state.email.value,
            password: state.password.value,
            passwordRetype: state.passwordRetype.value
        };
        register(registerRequest)
            .then(response => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                if (response.additionalEntries && response.additionalEntries.isFirstAccount) {
                    notification.success({
                        message: 'Zarejestrowano pomyślnie!',
                        description: "Utworzono pierwsze konto. " +
                            "Konto posiada uprawnienia administratorskie. Możesz się teraz zalogować",
                        btn,
                        key
                    });
                } else {
                    notification.success({
                        message: 'Zarejestrowano pomyślnie!',
                        description: "Po aktywowaniu konta przez administratora uzyskasz dostęp do systemu",
                        btn,
                        key
                    });

                }
                this.props.history.push("/login");
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się dokonać rejestracji',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.fullname.validateStatus === 'success' && state.username.validateStatus === 'success' &&
            state.email.validateStatus === 'success' && state.password.validateStatus === 'success' &&
            state.passwordRetype.validateStatus === 'success';
    }

    render() {
        return (
            <article className="register-container">
                <h1 className="page-title">Rejestracja</h1>
                <div className="register-content">
                    <Form onSubmit={this.handleSubmit} className="register-form">
                        <FormItem label="Nazwa użytkownika"
                                  hasFeedback
                                  validateStatus={this.state.username.validateStatus}
                                  help={this.state.username.message}>
                            <Input
                                prefix={<Icon type="user"/>}
                                size="large"
                                name="username"
                                value={this.state.username.value}
                                onBlur={this.checkUsernameAvailability}
                                onChange={(event) => this.handleChange(event, this.validateUsername)}/>
                        </FormItem>
                        <FormItem
                            label="Imię i Nazwisko"
                            validateStatus={this.state.fullname.validateStatus}
                            help={this.state.fullname.message}>
                            <Input
                                prefix={<Icon type="idcard"/>}
                                size="large"
                                name="fullname"
                                value={this.state.fullname.value}
                                onChange={(event) => this.handleChange(event, this.validateFullname)}/>
                        </FormItem>
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
                                onChange={(event) => this.handleChange(event, this.validateEmail)}/>
                        </FormItem>
                        <FormItem
                            label="Hasło"
                            validateStatus={this.state.password.validateStatus}
                            help={this.state.password.message}>
                            <Input
                                prefix={<Icon type="lock"/>}
                                size="large"
                                name="password"
                                type="password"
                                value={this.state.password.value}
                                onChange={(event) => this.handleChange(event, this.validatePassword)}/>
                        </FormItem>
                        <FormItem
                            label="Powtórz hasło"
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
                                    className="login-form-button"
                                    disabled={!this.isFormValid()}>Zarejestruj</Button>
                            Posiadasz już konto? <Link to="/login">Zaloguj się</Link>
                        </FormItem>
                    </Form>
                </div>
            </article>
        );
    }

    // Validation Functions
    validateFullname = (name) => {
        let validateStatus = 'success';
        let message = null;

        if (name.length < FULLNAME_MIN_LENGTH || name.length > FULLNAME_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Pole powinno zawierać mieć między ${USERNAME_MIN_LENGTH} a ${USERNAME_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

    }

    validateEmail = (email) => {
        let validateStatus = null;
        let message = null;

        if (!email) {
            validateStatus = 'error';
            message = 'Pole nie może być puste';
        } else if (!EMAIL_REGEX.test(email)) {
            validateStatus = 'error';
            message = 'Podano adres jest nieprawidłowy';
        } else if (email.length > EMAIL_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Podany adres jest zbyt długi (email nie może być dłuższy niż ${EMAIL_MAX_LENGTH} znaków)`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }
    };

    validateUsername = (username) => {
        let validateStatus = null;
        let message = null;
        if (username.length < USERNAME_MIN_LENGTH || username.length > USERNAME_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Nazwa użytkownika powinna mieć między ${USERNAME_MIN_LENGTH} a ${USERNAME_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }

    };

    validatePassword = (password) => {
        let validateStatus = 'success';
        let message = null;
        if (password.length < PASSWORD_MIN_LENGTH || password.length > PASSWORD_MAX_LENGTH) {
            validateStatus = 'error';
            message = `Hasło powinno mieć między ${PASSWORD_MIN_LENGTH} a ${PASSWORD_MAX_LENGTH} znaków`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        };

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

    checkUsernameAvailability() {
        const usernameValue = this.state.username.value;
        const usernameValidation = this.validateUsername(usernameValue);

        if (usernameValidation.validateStatus === 'error') {
            this.setState({
                username: {
                    value: usernameValue,
                    ...usernameValidation
                }
            });
            return;
        }

        this.setState({
            username: {
                value: usernameValue,
                validateStatus: 'validating',
                message: null
            }
        });

        checkUsernameAvailability(usernameValue)
            .then(response => {
                if (response.available) {
                    this.setState({
                        username: {
                            value: usernameValue,
                            validateStatus: 'success',
                            message: null
                        }
                    });
                } else {
                    this.setState({
                        username: {
                            value: usernameValue,
                            validateStatus: 'error',
                            message: 'Nazwa użytkownika jest zajęta'
                        }
                    });
                }
            }).catch(error => {
            this.setState({
                username: {
                    value: usernameValue,
                    validateStatus: 'success',
                    message: null
                }
            });
        });
    }

    checkEmailAvailability() {
        const emailValue = this.state.email.value;
        const emailValidation = this.validateEmail(emailValue);

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
                    this.setState({
                        email: {
                            value: emailValue,
                            validateStatus: 'error',
                            message: 'Adres Email jest już używany.'
                        }
                    });
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
}

export default Register;