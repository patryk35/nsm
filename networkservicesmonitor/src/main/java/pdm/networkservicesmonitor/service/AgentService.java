package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.exceptions.AppException;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;
import pdm.networkservicesmonitor.payload.client.*;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddLogsConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddMonitoredParameterConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceCreateRequest;
import pdm.networkservicesmonitor.repository.*;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.AgentServicesUtil.convertOriginsToList;
import static pdm.networkservicesmonitor.service.AgentServicesUtil.convertOriginsToString;

@Service
@Slf4j
public class AgentService {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private LogsCollectingConfigurationRepository logsCollectingConfigurationRepository;
    @Autowired
    private MonitoredParameterTypeRepository monitoredParameterTypeRepository;
    @Autowired
    private MonitoredParameterConfigurationRepository monitoredParameterConfigurationRepository;

    @Autowired
    private CollectedLogsRepository collectedLogsRepository;

    @Autowired
    private MonitoredParametersValuesRepository monitoredParametersValuesRepository;
    @Autowired
    private EntityManager entityManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public MonitorAgent createAgent(AgentCreateRequest agentCreateRequest) {
        MonitorAgent agent = new MonitorAgent(
                agentCreateRequest.getName(),
                agentCreateRequest.getDescription(),
                convertOriginsToList(agentCreateRequest.getAllowedOrigins())
        );

        return agentRepository.save(agent);
    }

    public pdm.networkservicesmonitor.model.agent.service.Service createService(ServiceCreateRequest serviceCreateRequest) {
        MonitorAgent agent = agentRepository.findById(serviceCreateRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException("Agent not found. Agent id is not valid"));
        pdm.networkservicesmonitor.model.agent.service.Service service =
                new pdm.networkservicesmonitor.model.agent.service.Service(serviceCreateRequest.getName(), serviceCreateRequest.getDescription(), agent);
        agent.addService(service);
        if (agentRepository.save(agent) != null) {
            return serviceRepository.save(service);
        }
        return null;
    }

    public LogsCollectingConfiguration addLogsCollectionConfiguration(ServiceAddLogsConfigurationRequest configurationRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        LogsCollectingConfiguration logsCollectingConfiguration = new LogsCollectingConfiguration(configurationRequest.getPath(), configurationRequest.getMonitoredFilesMasks(), configurationRequest.getUnmonitoredFileMasks(), service);
        return logsCollectingConfigurationRepository.save(logsCollectingConfiguration);
    }

    public MonitoredParameterConfiguration addMonitoredParameterConfiguration(ServiceAddMonitoredParameterConfigurationRequest configurationRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository.findById(configurationRequest.getParameterTypeId()).orElseThrow(() ->
                new NotFoundException(("Parameter type not found. Parameter type is not valid")));
        MonitoredParameterConfiguration monitoredParameterConfiguration = new MonitoredParameterConfiguration(monitoredParameterType, service, configurationRequest.getDescription(), configurationRequest.getMonitoringInterval());
        return monitoredParameterConfigurationRepository.save(monitoredParameterConfiguration);
    }

    public PagedResponse<AgentResponse> getAllAgents(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<MonitorAgent> agents = agentRepository.findAll(pageable);
        if (agents.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), agents.getNumber(),
                    agents.getSize(), agents.getTotalElements(), agents.getTotalPages(), agents.isLast());
        }
        List<AgentResponse> list = agents.getContent().stream()
                .map(e -> new AgentResponse(e.getId(), e.getName(), e.getDescription(), convertOriginsToString(e.getAllowedOrigins()), e.isRegistered()))
                .collect(Collectors.toList());

        return new PagedResponse<>(list, agents.getNumber(),
                agents.getSize(), agents.getTotalElements(), agents.getTotalPages(), agents.isLast());
    }

    public AgentResponse getAgentById(UUID agentId) {
        MonitorAgent agent = agentRepository.findById(agentId).orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Id", "id", agentId));
        return new AgentResponse(agent.getId(), agent.getName(), agent.getDescription(), convertOriginsToString(agent.getAllowedOrigins()), agent.isRegistered());
    }


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

    private void validatePageNumberAndSize(int page, int size, int maxPageSize) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > maxPageSize) {
            throw new BadRequestException("Page size cannot be greater than " + maxPageSize);
        }
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

    private Timestamp getTimestampFromRequestDateFiled(String datetime) {
        try {
            return new Timestamp(dateFormat.parse(datetime).getTime());
        } catch (ParseException e) {
            throw new BadRequestException("Datetime format is not correct");
        }
    }

    // TODO: Additional parameter for aproximation (if more than x records, count x avgs and return only avgs[time,value])
    public List<MonitoredParameterValuesResponse> getMonitoringByQuery(MonitoredParameterRequest monitoredParameterRequest) {
        Matcher serviceNameMatcher = Pattern.compile(".*service=\"(.*?)\".*").matcher(monitoredParameterRequest.getQuery());
        Matcher agentNameMatcher = Pattern.compile(".*agent=\"(.*?)\".*").matcher(monitoredParameterRequest.getQuery());
        Matcher parameterNameMatcher = Pattern.compile(".*parameter=\"(.*?)\".*").matcher(monitoredParameterRequest.getQuery());

        MonitorAgent agent;

        if (agentNameMatcher.matches()) {
            String monitorName = agentNameMatcher.group(1);
            agent = agentRepository.findByName(monitorName).orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Name", "name", monitorName));
        } else {
            throw new ResourceNotFoundException("Not found. Verify Agent Name in query", "query", monitoredParameterRequest.getQuery());
        }

        String serviceName = null;
        if (serviceNameMatcher.matches()) {
            serviceName = serviceNameMatcher.group(1);
        }


        List<UUID> servicesIds = new ArrayList<>();
        List<pdm.networkservicesmonitor.model.agent.service.Service> services = new ArrayList<>();

        if (serviceName == null) {
            agent.getServices().stream().parallel().forEachOrdered(service -> {
                servicesIds.add(service.getId());
                services.add(service);
            });
        } else {
            servicesIds.addAll(agent.getServices().parallelStream()
                    .filter(service -> service.getName().equals(serviceNameMatcher))
                    .map(pdm.networkservicesmonitor.model.agent.service.Service::getId)
                    .collect(Collectors.toList()));
        }

        List<MonitoredParameterValuesResponse> monitoredParameterValuesResponses = new ArrayList<>();


        if (servicesIds.size() != 0) {
            String parameterName = null;
            if (parameterNameMatcher.matches()) {
                parameterName = parameterNameMatcher.group(1);
            }

            List<UUID> parametersIds = new ArrayList<>();
            if (parameterName == null) {
                services.stream()
                        .map(service -> service.getMonitoredParametersConfigurations())
                        .forEach(monitoredParameterConfigurations -> monitoredParameterConfigurations
                                .forEach(monitoredParameterConfiguration -> parametersIds.add(monitoredParameterConfiguration.getParameterType().getId())));
            } else {
                final String paramName = parameterName.trim();
                services.stream()
                        .map(service -> service.getMonitoredParametersConfigurations())
                        .forEach(monitoredParameterConfigurations -> monitoredParameterConfigurations
                                .forEach(monitoredParameterConfiguration -> {
                                    if (monitoredParameterConfiguration.getParameterType().getName().equals(paramName)) {
                                        parametersIds.add(monitoredParameterConfiguration.getParameterType().getId());
                                    }
                                }));
            }

            parametersIds.forEach(id -> {
                List<MonitoredParameterValue> monitoredParameterValues;
                if (monitoredParameterRequest.getDatetimeFrom() == null && monitoredParameterRequest.getDatetimeTo() == null) {
                    servicesIds.forEach(s -> log.debug("s: " + s.toString()));
                    parametersIds.forEach(p -> log.debug("p: " + p.toString()));

                    monitoredParameterValues = monitoredParametersValuesRepository.findByServiceIdsAAndParameterTypeId(servicesIds, id);
                } else if (monitoredParameterRequest.getDatetimeFrom() != null && monitoredParameterRequest.getDatetimeTo() == null) {
                    Timestamp timestamp = getTimestampFromRequestDateFiled(monitoredParameterRequest.getDatetimeFrom());
                    monitoredParameterValues = monitoredParametersValuesRepository.findByServiceIdsAAndParameterTypeIdWithFromTimestamp(servicesIds, id, timestamp);
                } else if (monitoredParameterRequest.getDatetimeFrom() == null && monitoredParameterRequest.getDatetimeTo() != null) {
                    Timestamp timestamp = getTimestampFromRequestDateFiled(monitoredParameterRequest.getDatetimeTo());
                    monitoredParameterValues = monitoredParametersValuesRepository.findByServiceIdsAAndParameterTypeIdWithToTimestamp(servicesIds, id, timestamp);
                } else {
                    Timestamp timestampFrom = getTimestampFromRequestDateFiled(monitoredParameterRequest.getDatetimeFrom());
                    Timestamp timestampTo = getTimestampFromRequestDateFiled(monitoredParameterRequest.getDatetimeTo());
                    monitoredParameterValues = monitoredParametersValuesRepository.findByServiceIdsAAndParameterTypeIdWithTimestamp(servicesIds, id, timestampFrom, timestampTo);
                }
                MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository.findById(id).orElseThrow(() -> new AppException("Wrong parameter Id"));
                monitoredParameterValues = monitoredParameterValues.parallelStream().sorted(Comparator.comparing(MonitoredParameterValue::getTimestamp)).collect(Collectors.toList());
                monitoredParameterValuesResponses.add(new MonitoredParameterValuesResponse(
                        String.format("%s (parameter=%s)", monitoredParameterType.getDescription(), monitoredParameterType.getName()),
                        monitoredParameterValues));
            });
        }
        return monitoredParameterValuesResponses;
    }
}
