package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pdm.networkservicesmonitor.exceptions.AppException;
import pdm.networkservicesmonitor.exceptions.QueryException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.service.Service;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;
import pdm.networkservicesmonitor.payload.client.MonitoredParameterRequest;
import pdm.networkservicesmonitor.payload.client.MonitoredParameterValuesResponse;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.repository.MonitoredParameterTypeRepository;
import pdm.networkservicesmonitor.repository.MonitoredParametersValuesRepository;
import pdm.networkservicesmonitor.repository.ServiceRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.getTimestampFromRequestDateFiled;

@org.springframework.stereotype.Service
@Slf4j
public class MonitoringService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private MonitoredParameterTypeRepository monitoredParameterTypeRepository;

    @Autowired
    private MonitoredParametersValuesRepository monitoredParametersValuesRepository;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private ServiceRepository serviceRepository;

    public List<MonitoredParameterValuesResponse> getMonitoringByQuery(MonitoredParameterRequest monitoredParameterRequest) {

        Matcher parameterNameMatcher = Pattern
                .compile(".*parameter=\"(.*?)\".*")
                .matcher(monitoredParameterRequest.getQuery());

        MonitorAgent agent = getAgentFromQuery(monitoredParameterRequest.getQuery());

        List<Service> services = new ArrayList<>();
        List<UUID> servicesIds = new ArrayList<>();


        Service service = getServiceFromQuery(monitoredParameterRequest.getQuery(), agent);

        if (service == null) {
            agent.getServices().stream().parallel().forEachOrdered(s -> {
                servicesIds.add(s.getId());
                services.add(s);
            });
        } else {
            services.addAll(agent.getServices().parallelStream()
                    .filter(s -> s.getName().equals(service.getName()))
                    .collect(Collectors.toList()));
            services.forEach(s -> servicesIds.add(s.getId()));
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
                        .map(Service::getMonitoredParametersConfigurations)
                        .forEach(monitoredParameterConfigurations -> monitoredParameterConfigurations
                                .forEach(monitoredParameterConfiguration -> parametersIds.add(
                                        monitoredParameterConfiguration.getParameterType().getId())
                                )
                        );
            } else {
                final String paramName = parameterName.trim();
                services.stream()
                        .map(Service::getMonitoredParametersConfigurations)
                        .forEach(monitoredParameterConfigurations -> monitoredParameterConfigurations
                                .forEach(monitoredParameterConfiguration -> {
                                    if (monitoredParameterConfiguration.getParameterType().getName().equals(paramName)) {
                                        parametersIds.add(monitoredParameterConfiguration.getParameterType().getId());
                                    }
                                }));
                if (parametersIds.isEmpty()) {
                    throw new QueryException(
                            "Parameter Name",
                            "query",
                            monitoredParameterRequest.getQuery(),
                            "parameter with provided name not found"
                    );
                }
            }

            parametersIds.forEach(id -> {
                List<MonitoredParameterValue> monitoredParameterValues;
                if (monitoredParameterRequest.getDatetimeFrom() == null && monitoredParameterRequest.getDatetimeTo() == null) {
                    servicesIds.forEach(s -> log.debug("s: " + s.toString()));
                    parametersIds.forEach(p -> log.debug("p: " + p.toString()));
                    monitoredParameterValues = monitoredParametersValuesRepository.findByServiceIdsAAndParameterTypeId(
                            servicesIds,
                            id
                    );
                } else if (monitoredParameterRequest.getDatetimeFrom() != null
                        && monitoredParameterRequest.getDatetimeTo() == null) {
                    Timestamp timestamp = getTimestampFromRequestDateFiled(monitoredParameterRequest.getDatetimeFrom());
                    monitoredParameterValues = monitoredParametersValuesRepository
                            .findByServiceIdsAAndParameterTypeIdWithFromTimestamp(
                                    servicesIds,
                                    id,
                                    timestamp
                            );
                } else if (monitoredParameterRequest.getDatetimeFrom() == null
                        && monitoredParameterRequest.getDatetimeTo() != null) {
                    Timestamp timestamp = getTimestampFromRequestDateFiled(monitoredParameterRequest.getDatetimeTo());
                    monitoredParameterValues = monitoredParametersValuesRepository
                            .findByServiceIdsAAndParameterTypeIdWithToTimestamp(
                                    servicesIds,
                                    id,
                                    timestamp
                            );
                } else {
                    Timestamp timestampFrom = getTimestampFromRequestDateFiled(
                            monitoredParameterRequest.getDatetimeFrom()
                    );
                    Timestamp timestampTo = getTimestampFromRequestDateFiled(
                            monitoredParameterRequest.getDatetimeTo()
                    );
                    monitoredParameterValues = monitoredParametersValuesRepository
                            .findByServiceIdsAAndParameterTypeIdWithTimestamp(
                                    servicesIds,
                                    id,
                                    timestampFrom,
                                    timestampTo
                            );
                }
                // TODO(low): rewrite it: serviceIds is not necessary - param id is enough
                MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException("Wrong parameter Id"));
                monitoredParameterValues = monitoredParameterValues
                        .parallelStream()
                        .sorted(Comparator.comparing(MonitoredParameterValue::getTimestamp)).collect(Collectors.toList());
                monitoredParameterValuesResponses.add(new MonitoredParameterValuesResponse(
                        String.format(
                                "%s [parameter=%s]",
                                monitoredParameterType.getDescription(),
                                monitoredParameterType.getName()
                        ),
                        convertData(monitoredParameterValues, settingsService.getAppSettings().getChartsMaxValuesCount()),
                        settingsService.getAppSettings().getChartsMaxValuesCount(),
                        monitoredParameterValues.size(),
                        monitoredParameterType.getUnit(),
                        monitoredParameterType.getMultiplier()
                ));
            });
        }

        return monitoredParameterValuesResponses;
    }

    private MonitorAgent getAgentFromQuery(String searchQuery) {
        MonitorAgent agent;

        Matcher agentNameMatcher = Pattern
                .compile(".*agent=\"(.*?)\".*")
                .matcher(searchQuery);
        Matcher agentIdMatcher = Pattern
                .compile(".*agentId=\"(.*?)\".*")
                .matcher(searchQuery);

        if (agentIdMatcher.matches() && agentNameMatcher.matches()) {
            throw new QueryException(
                    "Agent",
                    "query",
                    searchQuery,
                    "use only one from set agent or agentId"
            );
        }

        if (agentNameMatcher.matches()) {
            String agentName = agentNameMatcher.group(1);
            agent = agentRepository.findByName(agentName)
                    .orElseThrow(() -> new QueryException(
                            "Agent Name",
                            "query",
                            agentName,
                            "agent not found with provided name")
                    );
        } else if (agentIdMatcher.matches()) {
            UUID agentId = null;
            try {
                agentId = UUID.fromString(agentNameMatcher.group(1));
            } catch (IllegalStateException e) {
                throw new QueryException(
                        "Agent Id",
                        "query",
                        searchQuery,
                        "agent not found with provided id"
                );
            }
            agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new QueryException(
                            "Agent Id",
                            "query",
                            agentIdMatcher.matches(),
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

    private List<MonitoredParameterValue> convertData(List<MonitoredParameterValue> data, int sizeLimit) {
        //TODO(low): Count avg here instead eliminating a lot of elems
        int size = data.size();
        if (size <= sizeLimit) {
            return data;
        }
        double step = (double) size / (sizeLimit-1);
        List<MonitoredParameterValue> d = new ArrayList<>(sizeLimit);
        for (int i = 0; i < (sizeLimit - 1); i++) {
            d.add(data.get((int) (i * step)));
        }
        d.add(data.get(size - 1));
        return d;
    }

    private Service getServiceFromQuery(String searchQuery, MonitorAgent agent) {
        Service service = null;

        Matcher serviceNameMatcher = Pattern
                .compile(".*service=\"(.*?)\".*")
                .matcher(searchQuery);
        Matcher serviceIdMatcher = Pattern
                .compile(".*serviceId=\"(.*?)\".*")
                .matcher(searchQuery);
        if (serviceIdMatcher.matches() && serviceNameMatcher.matches()) {
            throw new QueryException(
                    "Service",
                    "query",
                    searchQuery,
                    "use only one from set service or serviceId"
            );
        }

        if (serviceNameMatcher.matches()) {
            service = serviceRepository.findByAgentIdAndName(agent.getId(), serviceNameMatcher.group(1))
                    .orElseThrow(() -> new QueryException(
                            "Service Name",
                            "query",
                            serviceNameMatcher.group(1),
                            "service not found with provided name")
                    );
        } else if (serviceIdMatcher.matches()) {
            service = serviceRepository.findById(UUID.fromString(serviceIdMatcher.group(1)))
                    .orElseThrow(() -> new QueryException(
                            "Service Id",
                            "query",
                            serviceIdMatcher.group(1),
                            "service not found with provided id")
                    );
        }
        return service;
    }
}
