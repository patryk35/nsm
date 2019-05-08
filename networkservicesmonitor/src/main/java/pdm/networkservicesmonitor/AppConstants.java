package pdm.networkservicesmonitor;

public interface AppConstants {
    // TODO(low) Move it to application properties ???
    String DEFAULT_PAGE_NUMBER = "0";
    String DEFAULT_PAGE_SIZE = "30";

    int MAX_PAGE_SIZE = 50;
    Long CORS_MAX_AGE_SECS = 3000L;
    Long AGENT_DATA_SENDING_INTERVAL = 1000L;
}
