import React, {Component} from 'react';
import './Charts.css';
import {notification, Row} from 'antd/lib/index';
import {AutoComplete, Button, DatePicker, Input, TimePicker} from 'antd';
import {LOGS_LIST_SIZE} from "../configuration";
import {getMonitoredParameterValues} from "../utils/APIRequestsUtils";
import LoadingSpin from '../common/LoadingSpin';
import moment from 'moment';
import {Chart} from 'react-google-charts';


const Option = AutoComplete.Option;
const OptGroup = AutoComplete.OptGroup;

class Charts extends Component {
    constructor(props) {
        super(props);
        this.state = {
            monitoredParametersValues: [],
            isLoading: false,
            query: "",
            dateFrom: null,
            timeFrom: moment('00:00:00', 'HH:mm:ss'),
            dateTo: null,
            timeTo: null
        };
        this.loadLogsList = this.loadLogsList.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }


    loadLogsList(page = 0, size = LOGS_LIST_SIZE) {
        // TODO: loading is not working
        const state = this.state;
        if (state.query === "") {
            return;
        }
        //todo split time and date
        let monitoredParameterValuesRequest = {
            query: state.query || "",
            datetimeFrom: (state.dateFrom !== null && state.timeFrom !== null) && (state.dateFrom !== "" && state.timeFrom !== "") ? state.dateFrom + " " + state.timeFrom : null,
            datetimeTo: (state.dateTo !== null && state.timeTo !== null) && (state.dateTo !== "" && state.timeTo !== "") ? state.dateTo + " " + state.timeTo : null,
        };
        console.log(monitoredParameterValuesRequest);
        this.setState({
            isLoading: true
        });

        let promise = getMonitoredParameterValues(monitoredParameterValuesRequest);

        if (!promise) {
            return;
        }

        promise
            .then(response => {
                this.setState({
                    monitoredParametersValues: response,
                    isLoading: false
                })
            }).catch(error => {
            this.setState({
                monitoredParameterValues: [],
                isLoading: false
            });
            notification.error({
                message: 'Problem podczas pobierania danych!',
                description: ' Spróbuj ponownie później!',
                duration: 5
            });
        });
    }

    componentDidMount() {
        this.loadLogsList();
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                monitoredParametersValues: [],
                isLoading: false
            });
            this.loadLogsList();
        }
    }

    handleLoadMore() {
        this.loadLogsList(this.state.page + 1);
    }

    render() {
        const state = this.state;
        const data = [];
        if (this.state.monitoredParametersValues !== null && this.state.monitoredParametersValues.length > 0) {
            let i = 0;
            this.state.monitoredParametersValues.forEach((monitoredParameter) => {
                let parameterData = [];
                const title = monitoredParameter.name;
                parameterData.push([
                    'Data', title
                ]);
                monitoredParameter.content.forEach((val) => {
                    parameterData.push([
                        val.timestamp,
                        parseFloat(val.value)
                    ]);
                });
                if (parameterData.length > 1) {
                    data.push({
                        key: i,
                        title: title,
                        data: parameterData
                    })
                }
                i++;
            })
        }

        const items = [];
        console.log(data);
        this.items = data.map((d, key) =>
            <Chart
                height={'300px'}
                chartType="AreaChart"
                loader={<div>Loading Chart</div>}
                data={d.data}
                options={{
                    title: d.title,
                    hAxis: {textPosition: 'none'},
                    vAxis: {minValue: 0},
                    // For the legend to fit, we make the chart area smaller
                    chartArea: {width: '50%', height: '70%'},


                    // lineWidth: 25
                }}
            />
        );

        //TODO: Checking time not works like it should work
        //TODO: checking time from and time to relation (time to cannot be earlier than time from)
        return (
            <div>
                <div className="charts-container">
                    <Row gutter={16}>
                        <div>
                            <AutoComplete
                                className="certain-category-search"
                                dropdownClassName="certain-category-search-dropdown"
                                dropdownMatchSelectWidth={false}
                                dropdownStyle={{width: 300}}
                                size="large"
                                style={{width: '100%'}}
                                // TODO: Try to do below in some better way
                                //dataSource={options}
                                placeholder="Wyszukaj (format agent= service= parameter= )"
                                optionLabelProp="value"
                            >
                                <Input onPressEnter={(e) => {
                                    this.setState({
                                        query: e.target.value
                                    });
                                }} onBlur={(e) => {
                                    this.setState({
                                        query: e.target.value
                                    });
                                }}/>
                            </AutoComplete>
                        </div>

                        <div>
                            <DatePicker placeholder="Od dnia" className="charts-date-picker"
                                        onChange={(date, dateString) => {
                                            this.setState({
                                                dateFrom: dateString
                                            });
                                        }}/>
                            <TimePicker placeholder="Od godziny" defaultOpenValue={moment('00:00:00', 'HH:mm:ss')}
                                        className="charts-time-picker" onChange={(moment, timeString) => {
                                this.setState({
                                    timeFrom: timeString
                                });
                            }}/>
                        </div>
                        <div>
                            <DatePicker placeholder="Do dnia" className="charts-date-picker"
                                        onChange={(date, dateString) => {
                                            this.setState({
                                                dateTo: dateString
                                            });
                                        }}/>
                            <TimePicker placeholder="Do godziny" defaultOpenValue={moment('00:00:00', 'HH:mm:ss')}
                                        className="charts-time-picker" onChange={(moment, timeString) => {
                                this.setState({
                                    timeTo: timeString
                                });
                            }}/>
                        </div>
                        <div>
                            <Button type="primary" htmlType="submit" size="small" className="charts-form-button"
                                    onClick={(e) => {
                                        this.loadLogsList()
                                    }}>
                                Szukaj
                            </Button>
                        </div>
                    </Row>
                </div>
                <div className="charts-container">

                    {state.isLoading ? (<div>Trwa wczytywanie danych <LoadingSpin/></div>) : (
                        <p className={"charts-info"}>Tu pojawią się wykresy dla wybranych danych</p>)}

                    {state.monitoredParametersValues !== [] && this.items}
                </div>
            </div>

        );
    }
}


export default Charts;
