import React, {Component} from 'react';
import {getAgentServiceDetails} from '../../../utils/APIRequestsUtils';
import './ServiceDetails.css';
import {Link} from 'react-router-dom';

import {Button, Form, Icon, Input, notification} from 'antd';
import LoadingSpin from "../../../common/spin/LoadingSpin";
import {getCurrentUser} from "../../../utils/SharedUtils";
import {handleAgentServiceDeleteClick} from "../shared/ServiceShared";
import LogsConfigurationList from "../logsConfiguration/list/LogsConfigurationList";
import MonitoringConfigurationList from "../monitoringConfiguration/list/MonitoringConfigurationList";

const FormItem = Form.Item;


class ServiceDetails extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isLoading: true
        };
    }

    componentDidMount() {
        this.loadDetails(this.props.match.params.serviceId);
    };

    render() {
        return (
            <article className="agent-details-service-container">
                {this.state.isLoading ? (
                    <LoadingSpin/>
                ) : (
                    <div>
                        <h1>Szczegółowe informacje o serwisie <b>{this.state.serviceName.value}</b></h1>
                        <div className="agent-details-service-subcontainer">
                            <Form className="agent-details-service-form">
                                <FormItem label="Id serwisu">
                                    <Input
                                        prefix={<Icon type="tag"/>}
                                        size="large"
                                        name="serviceName"
                                        value={this.state.serviceId.value}
                                        disabled={true}/>
                                </FormItem>
                                <FormItem
                                    label="Opis">
                                    <Input
                                        prefix={<Icon type="read"/>}
                                        size="large"
                                        name="description"
                                        value={this.state.description.value}
                                        disabled={true}/>
                                </FormItem>
                                <div>
                                    {getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") &&
                                    <Button type="primary"
                                            htmlType="submit"
                                            size="large"
                                            className="agent-service-details-form-button-left">
                                        <Link
                                            to={"/agents/" + this.props.agentId + "/" + this.props.agentName + "/service/edit/" + this.state.serviceId.value}>Edytuj</Link></Button>
                                    }
                                    {getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") &&
                                    <Button type="primary"
                                            htmlType="submit"
                                            size="large"
                                            className="agent-service-details-form-button-right"
                                            onClick={() => handleAgentServiceDeleteClick(this.refresh, this.state.serviceId.value, this.state.serviceName.value)}>Usuń</Button>
                                    }
                                </div>
                            </Form>
                            <Button className={"agent-service-details-back-button"}>
                                <Link onClick={() => {
                                    this.props.history.goBack()
                                }}>Powrót</Link>
                            </Button>
                        </div>
                        <div className="agent-details-service-subcontainer">
                            <h4>Konfiguracja zbierania logów</h4>
                            <LogsConfigurationList serviceId={this.props.match.params.serviceId}
                                                   editAccess={false}></LogsConfigurationList>

                        </div>
                        <div className="agent-details-service-subcontainer">
                            <h4>Konfiguracja monitorowania parametrów</h4>
                            <MonitoringConfigurationList serviceId={this.props.match.params.serviceId}
                                                         editAccess={false}></MonitoringConfigurationList>
                        </div>
                        {getCurrentUser().roles.includes("ROLE_ADMINISTRATOR") &&
                        <div className="agent-details-service-subcontainer">
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="agent-service-details-form-button-left"><Link
                                to={"/alert/monitoring/create/" + this.state.serviceName.value + "/" + this.state.serviceId.value}>Dodaj
                                konfigurację alertu dla monitoringu</Link>
                            </Button>
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    className="agent-service-details-form-button-left"><Link
                                to={"/alert/logs/create/" + this.state.serviceName.value + "/" + this.state.serviceId.value}>Dodaj
                                konfigurację alertu dla logów</Link>
                            </Button>
                        </div>
                        }
                    </div>
                )}



            </article>
        );
    }

    refresh = () => {
        this.props.history.push("/agents");
    };

    loadDetails(id) {
        let promise = getAgentServiceDetails(id);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    serviceId: {value: response.serviceId},
                    serviceName: {value: response.name},
                    description: {value: response.description},
                    isLoading: false
                })
            }).catch(error => {
            notification.error({
                message: 'Problem podczas pobierania danych!',
                description: ' Spróbuj ponownie później!',
                duration: 5
            });
            this.props.history.goBack();
        });
    }
}

export default ServiceDetails;