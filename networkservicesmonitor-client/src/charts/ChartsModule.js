import {Chart} from "react-google-charts";
import React, {Component} from "react";
import render from "less/lib/less/render";

class ChartsModule extends Component {
    shouldComponentUpdate(nextProps, nextState) {
        if(this.props.data !== nextProps.data || this.props.chartType !== nextProps.chartType) {
            return true
        }
        return false
    }

    render() {
        return (
            this.props.data.map((d, key) =>
                <div className={"chartContainer"}>
                    <Chart
                        height={'300px'}
                        chartType={this.props.chartType}
                        loader={<div>Wczytywanie wykresu</div>}
                        data={d.data}
                        options={{
                            title: d.title,
                            legend: {position: 'none'},
                            //hAxis: {textPosition: 'none'},
                            hAxis: {
                                title: 'Czas', titleTextStyle: {color: '#333'},
                                slantedText: true, slantedTextAngle: 80
                            },
                            vAxis: {minValue: 0, format:'# ' + d.unit},
                            // For the legend to fit, we make the chart area smaller
                            chartArea: {width: '80%', height: '80%'},
                            explorer: {
                                actions: ['dragToZoom', 'rightClickToReset'],
                                axis: 'horizontal',
                                keepInBounds: true,
                                maxZoomIn: 100.0
                            },
                            language: 'pl'
                            // lineWidth: 25
                        }}
                    />
                    <p className={"additionalInfo"}>{d.additionalMessage}</p>
                </div>
            )
        )
    }
}

export default ChartsModule;