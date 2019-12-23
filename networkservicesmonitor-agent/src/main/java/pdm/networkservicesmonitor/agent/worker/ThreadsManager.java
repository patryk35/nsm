package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.configuration.AppConstants;
import pdm.networkservicesmonitor.agent.configuration.MonitoredParameterTypes;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.payloads.configuration.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.*;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom.*;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.system.*;

import javax.management.monitor.Monitor;
import java.util.*;

@Component
@Scope("prototype")
@Slf4j
public class ThreadsManager extends Thread {

    @Autowired
    private ConnectionWorker connectionWorker;
    @Autowired
    private ApplicationContext appContext;
    @Autowired
    private AgentConfigurationManager agentConfigurationManager;

    private Set<SpecializedWorker> specializedWorkers;

    private int taskExecutorCorePoolSize;

    private ThreadPoolTaskExecutor taskExecutor;

    public ThreadsManager() {
        specializedWorkers = new HashSet<>();
    }

    private int updateWorkers() {
        List<UUID> requestedWorkersConfigurationIds = new ArrayList<>();

        agentConfigurationManager.getAgentConfiguration().getServicesConfigurations().forEach(serviceConfiguration -> {
            serviceConfiguration.getLogsCollectingConfigurations().forEach(logsCollectingConfiguration -> {
                updateLogWorker(serviceConfiguration.getServiceId(), logsCollectingConfiguration);
                requestedWorkersConfigurationIds.add(logsCollectingConfiguration.getId());
            });
            serviceConfiguration.getMonitoredParametersConfigurations().forEach(monitoredParameter -> {
                updateMonitoringWorker(serviceConfiguration.getServiceId(), monitoredParameter);
                requestedWorkersConfigurationIds.add(monitoredParameter.getId());
            });
        });

        specializedWorkers.forEach(w -> {
            if (!requestedWorkersConfigurationIds.contains(w.getConfigurationId())) {
                w.disable();
            }
        });

        return requestedWorkersConfigurationIds.size();
    }

    private void updateLogWorker(UUID serviceId, LogsCollectingConfiguration logsCollectingConfiguration) {
        SpecializedWorker worker = specializedWorkers.parallelStream().filter(w -> w.getConfigurationId().equals(logsCollectingConfiguration.getId())).findFirst().orElse(null);
        if (worker != null && !worker.isRunning()) {
            specializedWorkers.remove(worker);
            worker = null;
        }
        if (worker != null) {
            ((LogWorker) worker).update(logsCollectingConfiguration);

        } else {
            worker = new LogWorker(connectionWorker, serviceId, logsCollectingConfiguration);
            specializedWorkers.add(worker);
            executeTask((LogWorker) worker);
        }
    }

    private void updateMonitoringWorker(UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        SpecializedWorker worker = specializedWorkers.parallelStream().filter(w -> w.getConfigurationId().equals(monitoredParameterConfiguration.getId())).findFirst().orElse(null);

        if (worker != null && !worker.isRunning()) {
            specializedWorkers.remove(worker);
            worker = null;
        }

        if (worker != null) {
            ((MonitoringWorker) worker).update(monitoredParameterConfiguration.getMonitoringInterval());
        } else {
            UUID parameterId = monitoredParameterConfiguration.getParameterParentId() == null ?
                    monitoredParameterConfiguration.getParameterId() : monitoredParameterConfiguration.getParameterParentId();
            switch (parameterId.toString()) {
                case MonitoredParameterTypes.CPU_USAGE:
                    worker = new CPUUsageWorker(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.FREE_PHYSICAL_MEMORY:
                    worker = new FreePhysicalMemoryWorker(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.AGENT_CPU_USAGE:
                    worker = new AgentCPUUsageWorker(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.FREE_SWAP_SPACE_SIZE:
                    worker = new FreeSwapSpaceSizeWorker(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.USED_MEMORY_SIZE:
                    worker = new UsedMemorySizeWorker(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.USED_SWAP_SPACE_SIZE:
                    worker = new UsedSwapSpaceSizeWorker(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.FILES_COUNT:
                    worker = new FilesCountWorker(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.DIRECTORY_SIZE:
                    worker = new DirectorySizeWorker(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.PORT_OPEN_CONNECTIONS:
                    worker = new PortOpenConnections(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.PROCESSES_COUNT_FOR_COMMAND:
                    worker = new ProcessesCountForCommand(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.PROCESSES_CPU_USAGE:
                    worker = new ProcessesCPUUsage(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                case MonitoredParameterTypes.PROCESSES_MEMORY_USAGE:
                    worker = new ProcessesMemoryUsage(connectionWorker, serviceId, monitoredParameterConfiguration);
                    break;
                default:
                    log.error("Parameter parameterId not implemented " + parameterId.toString());
            }
            if (worker != null) {
                specializedWorkers.add(worker);
                executeTask((MonitoringWorker) worker);
            }
        }
    }


    @Override
    public void run() {
        taskExecutor = (ThreadPoolTaskExecutor) appContext.getBean("taskExecutor");
        taskExecutorCorePoolSize = 16;
        taskExecutor.setCorePoolSize(taskExecutorCorePoolSize);
        taskExecutor.execute(connectionWorker);

        int configuredWorkers = updateWorkers();

        while (true) {
            log.info(String.format("Active Threads : %d ", taskExecutor.getActiveCount()));
            if (agentConfigurationManager.isUpdated() || configuredWorkers != taskExecutor.getActiveCount() - 1) {
                configuredWorkers = updateWorkers();
            }
            try {
                Thread.sleep(AppConstants.WAIT_WHEN_CHECKING_THREADS_ACTIVITY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (taskExecutor.getActiveCount() == 0) {
                taskExecutor.shutdown();
                break;
            }
        }
    }

    private void resizePool() {
        taskExecutorCorePoolSize = taskExecutorCorePoolSize * 2;
        taskExecutor.setCorePoolSize(taskExecutorCorePoolSize);
    }

    private void executeTask(Runnable runnable) {
        if (taskExecutor.getActiveCount() == taskExecutorCorePoolSize) {
            resizePool();
        }
        taskExecutor.execute(runnable);
    }
}
