import React, {Component} from 'react';
import {confirmPasswordReset, register, resetPassword} from '../../utils/APIRequestsUtils';
import './PasswordResetConfirm.css';
import {Button, Form, Icon, Input, notification} from 'antd';
import {validateEmail, validateEmailOnce, validatePassword} from "../shared/SharedFunctions";
import {Link} from "react-router-dom";

const FormItem = Form.Item;

class PasswordResetConfirm extends Component {
    constructor(props) {
        super(props);
        this.state = {
            password: {value: "", message: "Podaj hasło. Wymagane od 8 do 100 znaków"},
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
        const confirmResetRequest = {
            password: state.password.value,
            resetKey: this.props.match.params.resetKey
        };
        confirmPasswordReset(confirmResetRequest)
            .then(response => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Zapisano hasło!',
                    description: "Możesz się teraz zalogować.",
                    btn,
                    key
                });

                this.props.history.push("/login");
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            if(error.status === 404){
                notification.error({
                    message: 'Niepowodzenie',
                    description: 'Użyty link jest niepoprawny, wygasł lub został już wykorzystany.',
                });
            } else {
                notification.error({
                    message: 'Niepowodzenie',
                    description: ' Spróbuj ponownie później!',
                });
            }

        });
    }

    isFormValid() {
        return this.state.password.validateStatus === 'success';
    }

    render() {
        return (
            <article className="reset-password-confirm-container">
                <div className="reset-password-confirm-container-box">
                    <h1>Reset hasła</h1>
                    <Form onSubmit={this.handleSubmit} className="reset-password-confirm-content">
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
                                onChange={(event) => this.handleChange(event, validatePassword)}/>
                        </FormItem>
                        <FormItem>
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="reset-password-confirm-form-button"
                                    disabled={!this.isFormValid()}>Wyślij</Button>
                        </FormItem>
                    </Form>
                </div>
                <div className="reset-password-confirm-container-box-links">
                    Pamiętasz hasło? <Link onClick={() => this.props.history.push("/login")}>Zajoguj się</Link>
                </div>
            </article>
        );
    }
}

export default PasswordResetConfirm;