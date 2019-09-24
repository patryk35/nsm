package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.payload.client.LogValue;
import pdm.networkservicesmonitor.payload.client.LogsRequest;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.repository.CollectedLogsRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.getTimestampFromRequestDateFiled;
import static pdm.networkservicesmonitor.service.util.ServicesUtils.validatePageNumberAndSize;

@Service
@Slf4j
public class LogsService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private CollectedLogsRepository collectedLogsRepository;


    // TODO: Check if sql injection is possible
    public PagedResponse<LogValue> getLogsByQuery(LogsRequest logsRequest) {
        validatePageNumberAndSize(logsRequest.getPage(), logsRequest.getSize(), AppConstants.MAX_LOGS_PAGE_SIZE);
        LogsSearchQuery logsSearchQuery = createQueryObject(logsRequest.getQuery());
        if (!logsSearchQuery.valid()) {
            //TODO: return what was wrong
            throw new NotFoundException("Agent not found");
        }
        MonitorAgent agent;
        if (logsSearchQuery.getAgentId() != null) {
            agent = agentRepository.findById(logsSearchQuery.getAgentId()).orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Id", "id", logsSearchQuery.getAgentId().toString()));
        } else {
            //TODO: Base has to has only one agent with some name
            agent = agentRepository.findByName(logsSearchQuery.getAgentName()).orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Name", "name", logsSearchQuery.getAgentName()));
        }

        List<UUID> servicesIds = new ArrayList<>();
        if (logsSearchQuery.getServiceId() == null && logsSearchQuery.getServiceName() == null) {
            agent.getServices().stream().parallel().forEachOrdered(service -> servicesIds.add(service.getId()));
        } else if (logsSearchQuery.getServiceId() != null) {
            servicesIds.addAll(agent.getServices().parallelStream()
                    .filter(service -> service.getId() == logsSearchQuery.getServiceId())
                    .map(pdm.networkservicesmonitor.model.agent.service.Service::getId)
                    .collect(Collectors.toList()));
        } else {
            servicesIds.addAll(agent.getServices().parallelStream()
                    .filter(service -> service.getName().equals(logsSearchQuery.getServiceName()))
                    .map(pdm.networkservicesmonitor.model.agent.service.Service::getId)
                    .collect(Collectors.toList()));
        }

        if (servicesIds.size() != 0) {
            Pageable pageable = PageRequest.of(logsRequest.getPage(), logsRequest.getSize(), Sort.Direction.DESC, "timestamp");
            Page<CollectedLog> collectedLogs;

            // TODO: Use it to searchQuery q = em.createNativeQuery("SELECT a.firstname, a.lastname FROM Author a");
            // https://vladmihalcea.com/query-pagination-jpa-hibernate/


            if (logsRequest.getDatetimeFrom() == null && logsRequest.getDatetimeTo() == null) {
                collectedLogs = collectedLogsRepository.findByServiceIds(pageable, servicesIds, logsSearchQuery.getPath(), logsSearchQuery.getQuerySecondPart());
            } else if (logsRequest.getDatetimeFrom() != null && logsRequest.getDatetimeTo() == null) {
                Timestamp timestamp = getTimestampFromRequestDateFiled(logsRequest.getDatetimeFrom());
                collectedLogs = collectedLogsRepository.findByServiceIdsWithFromTimestamp(pageable, servicesIds, timestamp, logsSearchQuery.getPath(), logsSearchQuery.getQuerySecondPart());
            } else if (logsRequest.getDatetimeFrom() == null && logsRequest.getDatetimeTo() != null) {
                Timestamp timestamp = getTimestampFromRequestDateFiled(logsRequest.getDatetimeTo());
                collectedLogs = collectedLogsRepository.findByServiceIdsWithToTimestamp(pageable, servicesIds, timestamp, logsSearchQuery.getPath(), logsSearchQuery.getQuerySecondPart());
            } else {
                Timestamp timestampFrom = getTimestampFromRequestDateFiled(logsRequest.getDatetimeFrom());
                Timestamp timestampTo = getTimestampFromRequestDateFiled(logsRequest.getDatetimeTo());
                collectedLogs = collectedLogsRepository.findByServiceIdsWithTimestamp(pageable, servicesIds, timestampFrom, timestampTo, logsSearchQuery.getPath(), logsSearchQuery.getQuerySecondPart());
            }


            if (collectedLogs.getNumberOfElements() == 0) {
                return new PagedResponse<>(Collections.emptyList(), collectedLogs.getNumber(),
                        collectedLogs.getSize(), collectedLogs.getTotalElements(), collectedLogs.getTotalPages(), collectedLogs.isLast());
            }
            List<LogValue> list = collectedLogs.getContent().stream()
                    .map(e -> new LogValue(e.getPath(), e.getTimestamp(), e.getLog(), e.getService().getName()))
                    .collect(Collectors.toList());

            return new PagedResponse<>(list, collectedLogs.getNumber(),
                    collectedLogs.getSize(), collectedLogs.getTotalElements(), collectedLogs.getTotalPages(), collectedLogs.isLast());
        }
        return new PagedResponse<>(Collections.emptyList(), 0, 0, 0, 0, true);
    }

    // TODO: do it in smarter way
    private LogsSearchQuery createQueryObject(String searchQuery) {
        LogsSearchQuery logsSearchQuery = new LogsSearchQuery();
        String firstPart = null;
        String secondPart = null;

        try {
            Matcher queryPartsMatcher = Pattern.compile("(.*)\"(.*)").matcher(searchQuery);
            if (queryPartsMatcher.matches()) {
                firstPart = queryPartsMatcher.group(1) + "\"";
                secondPart = queryPartsMatcher.group(2);
            }
        } catch (Exception e) {
            throw new BadRequestException("Search query is not correct");
        }
        secondPart = secondPart.replace("\\s+=", "=").replace("=\\s+", "=");

        Matcher serviceNameMatcher = Pattern.compile(".*service=\"(.*?)\".*").matcher(firstPart);
        Matcher serviceIdMatcher = Pattern.compile(".*serviceId=\"(.*?)\".*").matcher(firstPart);
        Matcher agentNameMatcher = Pattern.compile(".*agent=\"(.*?)\".*").matcher(firstPart);
        Matcher agentIdMatcher = Pattern.compile(".*agentId=\"(.*?)\".*").matcher(firstPart);
        Matcher pathMatcher = Pattern.compile(".*path=\"(.*?)\".*").matcher(firstPart);

        if (agentNameMatcher.matches()) {
            logsSearchQuery.setAgentName(agentNameMatcher.group(1));
        }
        if (agentIdMatcher.matches()) {
            logsSearchQuery.setAgentId(UUID.fromString(agentIdMatcher.group(1)));
        }
        if (serviceNameMatcher.matches()) {
            logsSearchQuery.setServiceName(serviceNameMatcher.group(1));
        }
        if (serviceIdMatcher.matches()) {
            logsSearchQuery.setServiceId(UUID.fromString(serviceIdMatcher.group(1)));
        }
        if (pathMatcher.matches()) {
            logsSearchQuery.setPath(pathMatcher.group(1));
        } else {
            logsSearchQuery.setPath("");
        }

        /*StringBuilder sb =  new StringBuilder();
        List<String> secondPartSplited = Arrays.asList(secondPart.split("\\s+"));
        secondPartSplited.forEach(part -> {
            sb.append(String.format("AND like %%s% ", part));
        });

        log.debug(sb.toString());
        logsSearchQuery.setQuerySecondPart(sb.toString());*/
        // TODO: split words and search each one with like
        logsSearchQuery.setQuerySecondPart(secondPart.replaceFirst("\\s+", ""));
        return logsSearchQuery;
    }
}
