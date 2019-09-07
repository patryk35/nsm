import React, {Component} from 'react';
import {login} from '../../utils/APIRequestsUtils';
import './Login.css';
import {Link} from 'react-router-dom';
import {ACCESS_TOKEN} from '../../configuration';

import {Button, Checkbox, Form, Icon, Input, notification} from 'antd';

const FormItem = Form.Item;

class Login extends Component {
    render() {
        const AntWrappedLoginForm = Form.create()(LoginForm);
        return (
            <div className="login-container">
                <h1 className="page-title">Login</h1>
                <div className="login-content">
                    <AntWrappedLoginForm onLogin={this.props.onLogin}/>
                </div>
            </div>
        );
    }
}

class LoginForm extends Component {
    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        this.props.form.validateFields((validationError, values) => {
            if (!validationError) {
                const loginRequest = Object.assign({}, values);
                login(loginRequest)
                    .then(response => {
                        localStorage.setItem(ACCESS_TOKEN, response.accessToken);
                        this.props.onLogin();
                    }).catch(error => {
                    if (error.status === 401) {
                        notification.error({
                            message: 'Podano niepoprawne dane logowania!',
                            description: 'Spróbuj ponownie!',
                        });
                    } else if (error.status === 403) {
                        notification.warn({
                            message: 'Konto nie zostało aktywowane przez administratora!',
                            description: 'Spróbuj ponownie później lub skontantuj się z administratorem',
                        });
                    } else {
                        if (error.message) {
                            console.log("API error:" + error.message)
                        }
                        notification.error({
                            message: 'Wystąpił problem podczas logowania',
                            description: ' Spróbuj ponownie później!',
                        });
                    }
                });
            }
        });
    }

    render() {
        const {getFieldDecorator} = this.props.form;
        console.log(this.props.isAuthenticated);

        return (
            <Form onSubmit={this.handleSubmit} className="login-form">
                <FormItem>
                    {getFieldDecorator('usernameOrEmail', {
                        rules: [{required: true, message: 'Pole wymagane!'}],
                    })(
                        <Input
                            prefix={<Icon type="user"/>}
                            size="large"
                            name="usernameOrEmail"
                            placeholder="Username or Email"
                        />
                    )}
                </FormItem>
                <FormItem>
                    {getFieldDecorator('password', {
                        rules: [{required: true, message: 'Pole wymagane!'}],
                    })(
                        <Input
                            prefix={<Icon type="lock"/>}
                            size="large"
                            name="password"
                            type="password"
                            placeholder="Password"/>
                    )}
                </FormItem>
                <FormItem>
                    <Button type="primary" htmlType="submit" size="large" className="login-form-button">Login</Button>
                    {getFieldDecorator('rememberMe', {
                        valuePropName: 'checked',
                        initialValue: false,
                    })(
                        <Checkbox>Zapamiętaj mnie</Checkbox>
                    )}
                </FormItem>
                <Link to="/">Zapomniałem hasła (TBD)</Link><br/>
                <Link to="/register">Zarejestruj się teraz!</Link>
            </Form>
        );
    }
}


export default Login;