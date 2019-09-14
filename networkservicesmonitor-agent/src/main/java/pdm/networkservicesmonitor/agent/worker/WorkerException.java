package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerException extends RuntimeException {

    public WorkerException(String message) {
        super(message);
    }

}
