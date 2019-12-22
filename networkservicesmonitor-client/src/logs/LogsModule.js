import React, {Component} from "react";
import {Chart} from "react-google-charts";
import {Table} from "antd";
import {convertDate} from "../utils/SharedUtils";

class LogsModule extends Component {
    shouldComponentUpdate(nextProps, nextState) {
        if(this.props.logs !== nextProps.logs || this.props.page !== nextProps.page) {
            return true
        }
        return false
    }

    render() {
        const columns = [
            {title: 'Czas', dataIndex: 'time', key: 'time'},
            {title: 'Serwis', dataIndex: 'service', key: 'service'},
            {title: 'Ścieżka', dataIndex: 'path', key: 'path'},
            {title: 'Log', dataIndex: 'log', key: 'log', width: "50%"},
        ];
        const data = [];
        let i = 0;
        this.props.logs.forEach((log, index) => {
            data.push({
                key: i,
                time: convertDate(log.timestamp),
                service: log.serviceName,
                path: log.path,
                log: log.log,
            });
            i = i + 1;
        });
        return (
            <div className="logs-viewer-container">
                <Table
                    columns={columns}
                    dataSource={data}
                    size={"small"}
                    loading={this.props.isLoading}
                    locale={{
                        emptyText: "Brak danych"
                    }}
                    scroll={{x: true}}
                    pagination={{
                        current: this.props.page + 1,
                        defaultPageSize: this.props.size,
                        hideOnSinglePage: true,
                        total: this.props.totalElements,
                        onShowSizeChange: ((current, size) => this.props.loadLogsList(current - 1, size)),
                        onChange: ((current, size) => this.props.loadLogsList(current - 1, size)),
                        loading: this.props.isLoading
                    }}
                />
            </div>
        )
    }
}

export default LogsModule;