package pdm.networkservicesmonitor.agent.controller.exceptions;

public class ProxyDisabledException extends RuntimeException {
    public ProxyDisabledException(String message) {
        super(message);
    }
}
