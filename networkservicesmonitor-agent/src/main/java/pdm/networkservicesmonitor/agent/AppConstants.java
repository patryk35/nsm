package pdm.networkservicesmonitor.agent;

public interface AppConstants {
    Long TOKEN_MIN_TIME = 20000L;
    int MAX_PACKETS_IN_SENDING_QUEUE = 100;
    Long WAIT_WHEN_IS_LOCKED_INTERVAL = 20L;
    Long WAIT_WHEN_CHECKING_THREADS_ACTIVITY = 2000L;
}
