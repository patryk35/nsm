import React, {Component} from 'react';
import './Charts.css';
import {notification, Row} from 'antd/lib/index';
import {AutoComplete, Button, DatePicker, Input, Select, TimePicker} from 'antd';
import {LOGS_LIST_SIZE} from "../configuration";
import {getMonitoredParameterValues} from "../utils/APIRequestsUtils";
import LoadingSpin from '../common/spin/LoadingSpin';
import moment from 'moment';
import {Chart} from 'react-google-charts';


const Option = AutoComplete.Option;
const OptGroup = AutoComplete.OptGroup;

//"2019-09-30T20:14:59.064+0000"
class Charts extends Component {
    constructor(props) {
        super(props);
        this.state = {
            monitoredParametersValues: [],
            isLoading: false,
            query: "agent=\"test\"",
            dateFrom: null,
            timeFrom: null,
            dateTo: null,
            timeTo: null,
            chartType: "LineChart"
        };
        this.loadCharts = this.loadCharts.bind(this);
        this.handleLoadMore = this.handleLoadMore.bind(this);
    }

    checkForDataBreaks(intervals, i) {
        if (intervals.length < 3) {
            return true;
        }

        if (i === intervals.length - 1) {
            return Math.abs(1 - intervals[i] / intervals[i - 1]) > 0.15
        } else if (i === 0) {
            return Math.abs(1 - intervals[i] / intervals[i + 1]) > 0.15
        }
        console.log(intervals[i] + " " + intervals[i + 1] + " " + Math.abs(1 - intervals[i] / intervals[i + 1]));
        return Math.abs(1 - intervals[i] / intervals[i - 1]) > 0.15 && Math.abs(1 - intervals[i] / intervals[i + 1]) > 0.15;

    }

    loadCharts(page = 0, size = LOGS_LIST_SIZE) {
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
    }

    componentDidUpdate(nextProps) {
        if (this.props.isAuthenticated !== nextProps.isAuthenticated) {
            // Reset State
            this.setState({
                monitoredParametersValues: [],
                isLoading: false,
            });
        }
    }

    handleLoadMore() {
        this.loadLogsList(this.state.page + 1);
    }

    convertDate = (date, offset) => {
        let regexp = /(\w+)-(\w+)-(\w+)T(\w+):(\w+):(\w+).(\w+)\+0000/g;
        let match = regexp.exec(date);
        if (match !== null) {
            return new Date(parseInt(match[1]), parseInt(match[2]) - 1, parseInt(match[3]), parseInt(match[4]), parseInt(match[5]), parseInt(match[6]), parseInt(match[7]) + offset)
        }

    };

    render() {
        const state = this.state;
        const data = [];

        if (this.state.monitoredParametersValues !== null && this.state.monitoredParametersValues.length > 0) {
            this.state.monitoredParametersValues.forEach((monitoredParameter) => {
                console.log("not Ok");
                let parameterData = [];
                const title = monitoredParameter.name;
                parameterData.push(
                    [
                        {type: 'datetime', id: 'Data'},
                        {type: 'number', id: title}
                    ]
                );
                let lastDate = null;
                let interval;
                let intervals = [];
                monitoredParameter.content.forEach((val) => {
                    if (lastDate !== null) {
                        interval = this.convertDate(val.timestamp, 0) - lastDate;
                        intervals.push(interval);
                    }
                    lastDate = this.convertDate(val.timestamp, 0);
                });
                console.log(intervals);
                let i = 0;
                monitoredParameter.content.forEach((val) => {
                    let date = this.convertDate(val.timestamp, 0);
                    if (i > 0 && this.checkForDataBreaks(intervals, i - 1)) {
                        parameterData.push([
                            this.convertDate(val.timestamp, -1),
                            null
                        ]);
                    }
                    parameterData.push([
                        date,
                        parseFloat(val.value)
                    ]);
                    i++;

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
        this.items = data.map((d, key) =>
            <Chart
                height={'300px'}
                chartType={this.state.chartType}
                loader={<div>Loading Chart</div>}
                data={d.data}
                options={{
                    title: d.title,
                    hAxis: {textPosition: 'none'},
                    vAxis: {minValue: 0},
                    // For the legend to fit, we make the chart area smaller
                    chartArea: {width: '80%', height: '80%'},
                    explorer: {
                        actions: ['dragToZoom', 'rightClickToReset'],
                        axis: 'horizontal',
                        keepInBounds: true,
                        maxZoomIn: 100.0
                    },
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
                                        this.loadCharts()
                                    }}>
                                Szukaj
                            </Button>
                        </div>
                        <div>
                            <Select className="charts-form-button" defaultValue={"Wykres Liniowy"}
                                    onChange={(event) => this.handleChangeChartType(event)}>
                                <Option key="LineChart" title="LineChart">Wykres Liniowy</Option>
                                <Option key="AreaChart" title="AreaChart">Wykres Warstwowy</Option>
                                <Option key="Calendar" title="Calendar">Calendar</Option>
                                <Option key="ScatterChart" title="ScatterChart">ScatterChart</Option>
                                <Option key="Table" title="Table">Table</Option>
                            </Select>
                        </div>
                    </Row>
                </div>
                <div className="charts-container">

                    {state.isLoading ? (<LoadingSpin/>) : (
                        <p className={"charts-info"}>Tu pojawią się wykresy dla wybranych danych</p>)}

                    {state.monitoredParametersValues !== [] && this.items}
                </div>
            </div>

        );
    }

    handleChangeChartType(event) {
        this.setState({
            chartType: event
        })
    }
}


export default Charts;
