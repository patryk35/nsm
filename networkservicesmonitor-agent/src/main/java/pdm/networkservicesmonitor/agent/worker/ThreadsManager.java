package pdm.networkservicesmonitor.agent.worker;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.MonitoredParameterTypes;
import pdm.networkservicesmonitor.agent.payloads.configuration.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.payloads.data.ServiceLogEntries;
import pdm.networkservicesmonitor.agent.payloads.data.ServiceMonitoringParametersEntries;
import pdm.networkservicesmonitor.agent.worker.monitored_parameters.CPUUsage;

import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class ThreadsManager extends Thread {
    //TODO: agent send to monitor all problems ( agent logs section in agent tab in client)

    @Setter
    List<LogsCollectingConfiguration> logsCollectingConfigurations;
    @Setter
    List<MonitoredParameterConfiguration> monitoredParameterConfigurations;
    @Autowired
    ConnectionWorker connectionWorker;
    @Autowired
    private ApplicationContext appContext;

    @Override
    public void run() {

        final ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) appContext.getBean("taskExecutor");
        taskExecutor.setCorePoolSize(100);
        ConnectionWorker connectionWorker = (ConnectionWorker) appContext.getBean("connectionWorker");
        taskExecutor.execute(connectionWorker);

        //TODO(critical): how update/delete: keep here logWorkers and monitoringWorkers lists and update by methods like setInterval, etc.
        //TODO(critical): catching exceptions in workesrs, question how to inform about problems here
        //PacketManager packetManager = (PacketManager) appContext.getBean("packetManager");
        taskExecutor.execute(this.connectionWorker);

        logsCollectingConfigurations.forEach(l -> {
            ServiceLogEntries serviceLogEntries = new ServiceLogEntries(l.getServiceId(), l.getPath());
            int ordinal = this.connectionWorker.getServiceLogEntries().size();
            connectionWorker.addNewServiceLogEntries(serviceLogEntries);
            LogWorker worker = new LogWorker(connectionWorker, l, ordinal);
            taskExecutor.execute(worker);
        });

        monitoredParameterConfigurations.forEach(m -> {
            int ordinal = this.connectionWorker.getServiceMonitoredParametersEntries().size();
            ServiceMonitoringParametersEntries serviceMonitoringParametersEntries = new ServiceMonitoringParametersEntries(m.getServiceId(), m.getParameterId());
            switch (m.getParameterId().toString()) {
                case MonitoredParameterTypes.CPU_USAGE:
                    this.connectionWorker.addNewServiceMonitoredParametersEntries(serviceMonitoringParametersEntries);
                    CPUUsage cpuUsage = new CPUUsage(connectionWorker, m.getMonitoringInterval(), ordinal);
                    taskExecutor.execute(cpuUsage);
                    break;
                default:
                    throw new IllegalArgumentException("Parameter parameterId not implemented");

            }
        });

        for (; ; ) {
            int count = taskExecutor.getActiveCount();
            log.info("Active Threads : " + count);
            try {
                Thread.sleep(2000); //move ot to constants
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (count == 0) {
                taskExecutor.shutdown();
                break;
            }
        }

    }
}
