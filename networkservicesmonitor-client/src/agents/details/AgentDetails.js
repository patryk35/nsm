import React, {Component} from 'react';
import {getAgentDetails} from '../../utils/APIRequestsUtils';
import './AgentDetails.css';
import {Link} from 'react-router-dom';

import {Button, Checkbox, Form, Icon, Input} from 'antd';
import LoadingSpin from "../../common/LoadingSpin";
import {handleAgentDeleteClick} from "../shared/AgentShared";

const FormItem = Form.Item;


class AgentDetails extends Component {
    constructor(props) {
        super(props);
        this.loadDetails(this.props.match.params.id);
        this.state = {
            isLoading: true
        };
    }

    render() {
        return (
            <article className="agent-details-container">
                {this.state.isLoading ? (
                    <div>Trwa wczytywanie danych <LoadingSpin/></div>
                ) : (
                    <div>
                        <h1 className="page-title">Sczegółowe informacje o agencie</h1>
                        <div className="details-content">
                            <Form className="agent-details-form">
                                <FormItem label="Id">
                                    <Input
                                        prefix={<Icon type="tag"/>}
                                        size="large"
                                        name="agentId"
                                        value={this.state.agentId.value}
                                        disabled={true}
                                    />
                                </FormItem>
                                <FormItem label="Nazwa">
                                    <Input
                                        prefix={<Icon type="robot"/>}
                                        size="large"
                                        name="agentName"
                                        value={this.state.agentName.value}
                                        disabled={true}
                                    />
                                </FormItem>
                                <FormItem label="Opis">
                                    <Input
                                        prefix={<Icon type="read"/>}
                                        size="large"
                                        name="description"
                                        value={this.state.description.value}
                                        disabled={true}/>
                                </FormItem>
                                <FormItem label="Dozwolone adresy IP, z których łączy się agent">
                                    <Input
                                        prefix={<Icon type="cluster"/>}
                                        size="large"
                                        name="allowedOrigins"
                                        value={this.state.allowedOrigins.value}
                                        disabled={true}/>
                                </FormItem>
                                <FormItem label="Częstotliwość wysyłania pakietów">
                                    <Input
                                        prefix={<Icon type="number"/>}
                                        size="large"
                                        name="sendingInterval"
                                        value={this.state.sendingInterval.value}
                                        disabled={true}/>
                                </FormItem>
                                <FormItem
                                    label="Agent proxy"
                                    help={this.state.proxy.message}>
                                    <Checkbox onChange={ (event) => {
                                        this.setState({
                                            proxy: {
                                                value: event.target.checked,
                                            }
                                        })}} disabled={true} checked={this.state.proxy.value}>Tak</Checkbox>
                                </FormItem>
                                <div>
                                    <Button type="primary"
                                            htmlType="submit"
                                            size="large"
                                            className="agent-details-form-button-left"
                                            href={"/agents/edit/" + this.state.agentId.value}>Edytuj</Button>
                                    <Button type="primary"
                                            htmlType="submit"
                                            size="large"
                                            className="agent-details-form-button-right"
                                            onClick={() => handleAgentDeleteClick(this.refresh, this.state.agentId.value, this.state.agentName.value)}>Usuń</Button>

                                </div>
                                <Link to="/agents">Powrót do listy</Link>
                            </Form>

                        </div>
                    </div>
                )}
            </article>
        );
    }

    refresh = () => {
        this.props.history.push("/agents");
    };

    loadDetails(id) {
        let promise = getAgentDetails(id);

        if (!promise) {
            return;
        }

        this.setState({
            isLoading: true
        });

        promise
            .then(response => {
                this.setState({
                    agentId: {value: response.agentId},
                    agentName: {value: response.name},
                    description: {value: response.description},
                    allowedOrigins: {value: response.allowedOrigins},
                    sendingInterval: {value: response.sendingInterval},
                    proxy: {value: response.proxyAgent},

                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                isLoading: false
            })
        });
    }
}

export default AgentDetails;