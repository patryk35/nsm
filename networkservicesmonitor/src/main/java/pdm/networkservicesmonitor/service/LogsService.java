package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pdm.networkservicesmonitor.config.AppConstants;
import pdm.networkservicesmonitor.exceptions.QueryException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.service.Service;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.payload.client.LogValue;
import pdm.networkservicesmonitor.payload.client.LogsRequest;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.repository.CollectedLogsRepository;
import pdm.networkservicesmonitor.repository.ServiceRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.getTimestampFromRequestDateFiled;
import static pdm.networkservicesmonitor.service.util.ServicesUtils.validatePageNumberAndSize;

@org.springframework.stereotype.Service
@Slf4j
public class LogsService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CollectedLogsRepository collectedLogsRepository;


    // TODO(high): Check if sql injection is possible
    public PagedResponse<LogValue> getLogsByQuery(LogsRequest logsRequest) {
        validatePageNumberAndSize(logsRequest.getPage(), logsRequest.getSize(), AppConstants.MAX_LOGS_PAGE_SIZE);
        LogsSearchQuery logsSearchQuery = new LogsSearchQuery(logsRequest.getQuery());
        MonitorAgent agent = getAgentFromQuery(logsSearchQuery);
        Service service = getServiceFromQuery(logsSearchQuery, agent);

        List<UUID> servicesIds = new ArrayList<>();
        if (service == null) {
            agent.getServices().stream().parallel().forEachOrdered(s -> servicesIds.add(s.getId()));
        } else {
            servicesIds.addAll(agent.getServices().parallelStream()
                    .filter(s -> s.getName().equals(service.getName()))
                    .map(Service::getId)
                    .collect(Collectors.toList()));
        }

        if (servicesIds.size() != 0) {
            Pageable pageable = PageRequest.of(
                    logsRequest.getPage(),
                    logsRequest.getSize(),
                    Sort.Direction.DESC,
                    "timestamp"
            );
            Page<CollectedLog> collectedLogs;

            // TODO(medium): Use it to searchQuery q = em.createNativeQuery("SELECT a.firstname, a.lastname FROM Author a");
            // https://vladmihalcea.com/query-pagination-jpa-hibernate/


            if (logsRequest.getDatetimeFrom() == null && logsRequest.getDatetimeTo() == null) {
                collectedLogs = collectedLogsRepository
                        .findByServiceIds(
                                pageable,
                                servicesIds,
                                logsSearchQuery.getPath(),
                                logsSearchQuery.getQuerySecondPart()
                        );
            } else if (logsRequest.getDatetimeFrom() != null && logsRequest.getDatetimeTo() == null) {
                Timestamp timestamp = getTimestampFromRequestDateFiled(logsRequest.getDatetimeFrom());
                collectedLogs = collectedLogsRepository.findByServiceIdsWithFromTimestamp(
                        pageable,
                        servicesIds,
                        timestamp,
                        logsSearchQuery.getPath(),
                        logsSearchQuery.getQuerySecondPart()
                );
            } else if (logsRequest.getDatetimeFrom() == null && logsRequest.getDatetimeTo() != null) {
                Timestamp timestamp = getTimestampFromRequestDateFiled(logsRequest.getDatetimeTo());
                collectedLogs = collectedLogsRepository.findByServiceIdsWithToTimestamp(
                        pageable,
                        servicesIds,
                        timestamp,
                        logsSearchQuery.getPath(),
                        logsSearchQuery.getQuerySecondPart()
                );
            } else {
                Timestamp timestampFrom = getTimestampFromRequestDateFiled(logsRequest.getDatetimeFrom());
                Timestamp timestampTo = getTimestampFromRequestDateFiled(logsRequest.getDatetimeTo());
                collectedLogs = collectedLogsRepository.findByServiceIdsWithTimestamp(
                        pageable,
                        servicesIds,
                        timestampFrom,
                        timestampTo,
                        logsSearchQuery.getPath(),
                        logsSearchQuery.getQuerySecondPart()
                );
            }


            if (collectedLogs.getNumberOfElements() == 0) {
                return new PagedResponse<>(
                        Collections.emptyList(),
                        collectedLogs.getNumber(),
                        collectedLogs.getSize(),
                        collectedLogs.getTotalElements(),
                        collectedLogs.getTotalPages(),
                        collectedLogs.isLast()
                );
            }
            List<LogValue> list = collectedLogs.getContent().stream()
                    .map(e -> new LogValue(
                            e.getPath(),
                            e.getTimestamp(),
                            e.getLog(),
                            e.getService().getName())
                    )
                    .collect(Collectors.toList());

            return new PagedResponse<>(list,
                    collectedLogs.getNumber(),
                    collectedLogs.getSize(),
                    collectedLogs.getTotalElements(),
                    collectedLogs.getTotalPages(),
                    collectedLogs.isLast()
            );
        }
        return new PagedResponse<>(Collections.emptyList(), 0, 0, 0, 0, true);
    }

    private MonitorAgent getAgentFromQuery(LogsSearchQuery searchQuery) {
        MonitorAgent agent;

        if (searchQuery.getAgentId() != null && searchQuery.getAgentName() != null) {
            throw new QueryException(
                    "Agent",
                    "query",
                    searchQuery,
                    "use only one from set agent or agentId"
            );
        }

        if (searchQuery.getAgentName() != null) {
            agent = agentRepository.findByName(searchQuery.getAgentName())
                    .orElseThrow(() -> new QueryException(
                            "Agent Name",
                            "query",
                            searchQuery.getAgentName(),
                            "agent not found with provided name")
                    );
        } else if (searchQuery.getAgentId() != null) {
            agent = agentRepository.findById(searchQuery.getAgentId())
                    .orElseThrow(() -> new QueryException(
                            "Agent Id",
                            "query",
                            searchQuery.getAgentId(),
                            "agent not found with provided id")
                    );
        } else {
            throw new QueryException(
                    "Agent",
                    "query",
                    searchQuery,
                    "agent and agentId missing in query"
            );
        }
        return agent;
    }

    private Service getServiceFromQuery(LogsSearchQuery searchQuery, MonitorAgent agent) {
        Service service = null;

        if (searchQuery.getServiceId() != null && searchQuery.getServiceName() != null) {
            throw new QueryException(
                    "Service",
                    "query",
                    searchQuery,
                    "use only one from set service or serviceId"
            );
        }

        if (searchQuery.getServiceName() != null) {
            service = serviceRepository.findByAgentIdAndName(agent.getId(), searchQuery.getServiceName())
                    .orElseThrow(() -> new QueryException(
                            "Service Name",
                            "query",
                            searchQuery.getServiceName(),
                            "service not found with provided name")
                    );
        } else if (searchQuery.getServiceId() != null) {
            service = serviceRepository.findById(searchQuery.getServiceId())
                    .orElseThrow(() -> new QueryException(
                            "Service Id",
                            "query",
                            searchQuery.getServiceId(),
                            "service not found with provided id")
                    );
        }
        return service;
    }

}
