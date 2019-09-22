package pdm.networkservicesmonitor.agent.worker.specializedWorkers;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

public abstract class SpecializedWorker {
    @Getter
    protected UUID serviceId;
    @Getter
    protected UUID configurationId;

    public SpecializedWorker(UUID serviceId, UUID configurationId) {
        this.serviceId = serviceId;
        this.configurationId = configurationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecializedWorker)) return false;
        SpecializedWorker that = (SpecializedWorker) o;
        return getServiceId().equals(that.getServiceId()) &&
                getConfigurationId().equals(that.getConfigurationId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceId(), getConfigurationId());
    }

    public abstract void disable();
}
