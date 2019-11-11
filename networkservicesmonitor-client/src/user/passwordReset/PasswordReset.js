import React, {Component} from 'react';
import {register, resetPassword} from '../../utils/APIRequestsUtils';
import './PasswordReset.css';
import {Button, Form, Icon, Input, notification} from 'antd';
import {validateEmail, validateEmailOnce} from "../shared/SharedFunctions";
import {Link} from "react-router-dom";

const FormItem = Form.Item;

class PasswordReset extends Component {
    constructor(props) {
        super(props);
        this.state = {
            email: {value: "", message: "Podaj adres email, na który zostanie wysłana wiadomość z linkiem do resetu hasłą!"}
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
        const resetRequest = {
            email: state.email.value
        };
        resetPassword(resetRequest)
            .then(response => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Wysłano wiadomość!',
                    description: "Sprawdź swoją pocztę. Na podany adres została wysłana wiadomość z linkiem do resetu hasłą.",
                    btn,
                    key
                });

                this.props.history.push("/login");
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Niepowodzenie',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        return this.state.email.validateStatus === 'success';
    }

    render() {
        return (
            <article className="reset-password-container">
                <div className="reset-password-container-box">
                    <h1>Reset hasła</h1>
                    <Form onSubmit={this.handleSubmit} className="reset-password-content">
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
                                onChange={(event) => this.handleChange(event, validateEmailOnce)}/>
                        </FormItem>
                        <FormItem>
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="reset-password-form-button"
                                    disabled={!this.isFormValid()}>Wyślij</Button>
                        </FormItem>
                    </Form>
                </div>
                <div className="reset-password-container-box-links">
                    <Link onClick={() => this.props.history.push("/login")}>Powrót</Link>
                </div>
            </article>
        );
    }
}

export default PasswordReset;