package pdm.networkservicesmonitor.agent.worker;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.AppConstants;
import pdm.networkservicesmonitor.agent.MonitoredParameterTypes;
import pdm.networkservicesmonitor.agent.payloads.configuration.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.payloads.data.ServiceLogEntries;
import pdm.networkservicesmonitor.agent.payloads.data.ServiceMonitoringParametersEntries;
import pdm.networkservicesmonitor.agent.worker.monitoring_workers.CPUUsage;
import pdm.networkservicesmonitor.agent.worker.monitoring_workers.FreePhysicalMemory;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class ThreadsManager extends Thread {
    //TODO: agent send to monitor all problems ( agent logs section in agent tab in client)

    @Autowired
    ConnectionWorker connectionWorker;
    @Autowired
    private ApplicationContext appContext;
    @Setter
    private List<LogsCollectingConfiguration> logsCollectingConfigurations;
    @Setter
    private List<MonitoredParameterConfiguration> monitoredParameterConfigurations;

    private List<LogWorker> logWorkers;

    private List<Runnable> monitoringWorkers;

    private int taskExecutorCorePoolSize;

    public ThreadsManager() {
        logWorkers = new ArrayList<>();
        monitoringWorkers = new ArrayList<>();
    }

    @Override
    public void run() {
        final ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) appContext.getBean("taskExecutor");
        taskExecutorCorePoolSize = logsCollectingConfigurations.size() + monitoredParameterConfigurations.size() + 1;
        taskExecutor.setCorePoolSize(taskExecutorCorePoolSize);
        taskExecutor.execute(connectionWorker);


        //TODO(critical): how update/delete: keep here logWorkers and monitoringWorkers lists and update by methods like setInterval, etc.
        //TODO(critical): catching exceptions in workers, question how to inform about problems here

        logsCollectingConfigurations.forEach(l -> {
            ServiceLogEntries serviceLogEntries = new ServiceLogEntries(l.getServiceId(), l.getPath());
            int ordinal = this.connectionWorker.getServiceLogEntries().size();
            connectionWorker.addNewServiceLogEntries(serviceLogEntries);
            LogWorker worker = new LogWorker(connectionWorker, l, ordinal);
            logWorkers.add(worker);
            taskExecutor.execute(worker);
        });

        monitoredParameterConfigurations.forEach(m -> {
            int ordinal = this.connectionWorker.getServiceMonitoredParametersEntries().size();
            ServiceMonitoringParametersEntries serviceMonitoringParametersEntries = new ServiceMonitoringParametersEntries(m.getServiceId(), m.getParameterId());
            Runnable runnable;
            switch (m.getParameterId().toString()) {
                case MonitoredParameterTypes.CPU_USAGE:
                    runnable = new CPUUsage(connectionWorker, m.getMonitoringInterval(), ordinal);
                    break;
                case MonitoredParameterTypes.FREE_PHYSICAL_MEMORY:
                    runnable = new FreePhysicalMemory(connectionWorker, m.getMonitoringInterval(), ordinal);
                    break;
                default:
                    throw new IllegalArgumentException("Parameter parameterId not implemented " + m.getParameterId().toString());
            }
            this.connectionWorker.addNewServiceMonitoredParametersEntries(serviceMonitoringParametersEntries);
            taskExecutor.execute(runnable);


        });

        for (; ; ) {
            int count = taskExecutor.getActiveCount();
            log.info("Active Threads : " + count);
            try {
                Thread.sleep(AppConstants.WAIT_WHEN_CHECKING_THREADS_ACTIVITY);
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
