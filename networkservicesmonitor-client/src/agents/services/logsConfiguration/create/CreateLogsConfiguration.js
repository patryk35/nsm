import React, {Component} from 'react';
import {
    createAgentService, createLogsConfiguration, loadNewAvailableMonitoringParameters
} from '../../../../utils/APIRequestsUtils';
import './CreateLogsConfiguration.css';
import {Link} from 'react-router-dom';
import {
    AGENT_MONITORING_PARAMETER_DESCRIPTION_MAX_LENGTH, AGENT_MONITORING_PARAMETER_DESCRIPTION_MIN_LENGTH,
    AGENT_SERVICE_DESCRIPTION_MAX_LENGTH,
    AGENT_SERVICE_DESCRIPTION_MIN_LENGTH,
    AGENT_SERVICE_NAME_MAX_LENGTH,
    AGENT_SERVICE_NAME_MIN_LENGTH, AGENT_SERVICES_CONFIGURATION_LIST_SIZE
} from '../../../../configuration';

import {Button, Form, Icon, Input, notification, Select} from 'antd';
const FormItem = Form.Item;

const { Option } = Select;

class CreateLogsConfiguration extends Component {
    constructor(props) {
        super(props);
        this.state = {
            path: {
                value: "",
                message: "Wprowadź ścieżkę do zbierania logów."
            },
            monitoredFilesMask: {
                value: "",
                message: "Podaj maskę nazwy plików do monitorowania lub pozostaw pole wolne w celu monitorowania wszystkich plików ze ścieżki. Pozostaw puste, aby akceptować wszystkie.",
                validateStatus: "success"
            },
            logLineRegex: {
                value: "",
                message: "Podaj maskę lini z monitorowanych plików lub pozostaw pole puste w celu zbierania wszystkich logów. Pozostaw puste, aby akceptować wszystkie.",
                validateStatus: "success"
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
        const logsConfigurationRequest = {
            serviceId: this.props.match.params.serviceId,
            path: state.path.value,
            monitoredFilesMask: state.monitoredFilesMask.value,
            logLineRegex: state.logLineRegex.value
        };
        createLogsConfiguration(logsConfigurationRequest)
            .then((response) => {
                const key = `open${Date.now()}`;
                const btn = (
                    <Button type="primary" size="small" onClick={() => notification.close(key)}>OK</Button>
                );
                notification.success({
                    message: 'Konfiguracja została utworzona pomyślnie!',
                    btn,
                    key
                });
                this.props.history.goBack();
            }).catch(error => {
            if (error.message) {
                console.log("API error:" + error.message)
            }
            notification.error({
                message: 'Nie udało się dodać konfiguracji',
                description: ' Spróbuj ponownie później!',
                duration: 3
            });
        });
    }

    isFormValid() {
        const state = this.state;
        return state.path.validateStatus === 'success' && state.monitoredFilesMask.validateStatus === 'success' && state.logLineRegex.validateStatus;
    }

    render() {
        return (
            <article className="agent-create-service-logs-configuration-container">
                <h1 className="page-title">Dodaj konfigurację zbierania logów</h1>
                <div className="add-service-logs-configuration-content">
                    <Form onSubmit={this.handleSubmit} className="agent-create-service-logs-configuration-form">
                        <FormItem
                            label="Ścieżka"
                            hasFeedback
                            validateStatus={this.state.path.validateStatus}
                            help={this.state.path.message}>
                            <Input
                                prefix={<Icon type="read"/>}
                                size="large"
                                name="path"
                                value={this.state.path.value}
                                onChange={(event) => this.handleChange(event, this.validatePath)}/>
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
                                    disabled={!this.isFormValid()}>Dodaj</Button>
                        </FormItem>
                        <Button className={"agent-create-service-logs-configuration-back-button"}>
                            <Link onClick={() => {
                                this.props.history.goBack()
                            }}>Powrót</Link>
                        </Button>
                    </Form>
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

    validatePath = (path) => {
        let validateStatus = 'success';
        let message = null;
        if (path.length === 0 ) {
            validateStatus = 'error';
            message = `Podaj ścieżkę do monitorowania logów`;
        }

        return {
            validateStatus: validateStatus,
            message: message
        }

    };
}

export default CreateLogsConfiguration;