import React, {Component} from 'react';
import {editLogsConfiguration, getLogsConfigurationDetails} from '../../../../utils/APIRequestsUtils';
import './EditLogsConfiguration.css';
import {Link} from 'react-router-dom';

import {Button, Form, Icon, Input, notification, Select} from 'antd';

const FormItem = Form.Item;

const {Option} = Select;

class EditLogsConfiguration extends Component {
    constructor(props) {
        super(props);
        this.loadDetails(this.props.match.params.configurationId);
        this.state = {
            path: {
                value: "",
            },
            monitoredFilesMask: {
                value: "",
                message: "Podaj maskę nazwy plików do monitorowania lub pozostaw pole wolne w celu monitorowania wszystkich plików ze ścieżki."
            },
            logLineRegex: {
                value: "",
                message: "Podaj maskę lini z monitorowanych plików lub pozostaw pole puste w celu zbierania wszystkich logów."
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
        const logsConfigurationEditRequest = {
            configurationId: this.props.match.params.configurationId,
            monitoredFilesMask: state.monitoredFilesMask.value,
            logLineRegex: state.logLineRegex.value
        };
        editLogsConfiguration(logsConfigurationEditRequest)
            .then((response) => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Konfiguracja została zaktualizowana pomyślnie!',
                    btn,
                    key
                });
                this.props.history.goBack();
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się zaktualizować konfiguracji',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.monitoredFilesMask.validateStatus === 'success' && state.logLineRegex.validateStatus;
    }

    render() {
        return (
            <article className="agent-edit-service-logs-configuration-container">
                <h1 className="page-title">Edytuj konfigurację zbierania logów dla serwisu</h1>
                <div className="edit-service-logs-configuration-content">
                    <Form onSubmit={this.handleSubmit} className="agent-edit-service-logs-configuration-form">
                        <FormItem label="Ścieżka">
                            <Input
                                prefix={<Icon type="read"/>}
                                size="large"
                                name="path"
                                value={this.state.path.value}
                                disabled={true}/>
                        </FormItem>
                        <FormItem
                            label="Maska nazwy pliku"
                            hasFeedback
                            validateStatus={this.state.monitoredFilesMask.validateStatus}
                            help={this.state.monitoredFilesMask.message}>
                            <Input
                                prefix={<Icon type="read"/>}
                                size="large"
                                name="monitoredFilesMask"
                                value={this.state.monitoredFilesMask.value}
                                onChange={(event) => this.handleChange(event, this.validateMonitoredFilesMask)}/>
                        </FormItem>

                        <FormItem
                            label="Maska lini"
                            hasFeedback
                            validateStatus={this.state.logLineRegex.validateStatus}
                            help={this.state.logLineRegex.message}>
                            <Input
                                prefix={<Icon type="number"/>}
                                size="large"
                                name="logLineRegex"
                                value={this.state.logLineRegex.value}
                                onChange={(event) => this.handleChange(event, this.validateLogLineRegex)}/>
                        </FormItem>
                        <FormItem>
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="agent-create-service-logs-configuration-form-button"
                                    disabled={!this.isFormValid()}>Zapisz</Button>
                        </FormItem>
                    </Form>
                    <Button className={"agent-edit-service-logs-configuration-back-button"}>
                        <Link onClick={() => {
                            this.props.history.goBack()
                        }}>Powrót</Link>
                    </Button>
                </div>
            </article>
        );
    }

    // Validation Functions
    validateLogLineRegex = (logLineRegex) => {
        let validateStatus = 'success';
        let message = null;

        //TODO: validation of this param

        return {
            validateStatus: validateStatus,
            message: message
        };

    };


    validateMonitoredFilesMask = (monitoredFileMask) => {
        let validateStatus = 'success';
        let message = null;

        //TODO: validation of this param


        return {
            validateStatus: validateStatus,
            message: message
        };

    };

    loadDetails(id) {
        let promise = getLogsConfigurationDetails(id);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    path: {value: response.path},
                    monitoredFilesMask: {value: response.monitoredFilesMask},
                    logLineRegex: {value: response.logLineRegex},
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                isLoading: false
            })
        });
    }
}

export default EditLogsConfiguration;