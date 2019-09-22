package pdm.networkservicesmonitor;

public interface AppConstants {
    String DEFAULT_PAGE_NUMBER = "0";
    String DEFAULT_PAGE_SIZE = "10";
    int MAX_PAGE_SIZE = 50;

    int DEFAULT_LOGS_PAGE_NUMBER = 100;
    int DEFAULT_LOGS_PAGE_SIZE = 100;
    int MAX_LOGS_PAGE_SIZE = 1000;

    Long CORS_MAX_AGE_SECS = 3000L;
    Long AGENT_DATA_SENDING_INTERVAL = 1000L;
}
