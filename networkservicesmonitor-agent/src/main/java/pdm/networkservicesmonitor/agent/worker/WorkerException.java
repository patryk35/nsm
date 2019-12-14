package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;
import pdm.networkservicesmonitor.agent.AgentApplication;
import pdm.networkservicesmonitor.agent.payloads.data.AgentError;

import java.util.Date;

@Slf4j
public class WorkerException extends RuntimeException {
    public WorkerException(String message) {
        super(message);
        AgentApplication.addPacketToQueue(new AgentError((new Date()).getTime(),message));
    }

}
