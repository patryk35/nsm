package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pdm.networkservicesmonitor.exceptions.AppException;
import pdm.networkservicesmonitor.exceptions.QueryException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;
import pdm.networkservicesmonitor.payload.client.MonitoredParameterRequest;
import pdm.networkservicesmonitor.payload.client.MonitoredParameterValuesResponse;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.repository.MonitoredParameterTypeRepository;
import pdm.networkservicesmonitor.repository.MonitoredParametersValuesRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.AppConstants.MAX_PARAMETERS_IN_RESPONSE;
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

    public List<MonitoredParameterValuesResponse> getMonitoringByQuery(MonitoredParameterRequest monitoredParameterRequest) {

        Matcher parameterNameMatcher = Pattern
                .compile(".*parameter=\"(.*?)\".*")
                .matcher(monitoredParameterRequest.getQuery());

        MonitorAgent agent = getAgentFromQuery(monitoredParameterRequest.getQuery());

        List<UUID> servicesIds = new ArrayList<>();
        List<Service> services = new ArrayList<>();

        agent.getServices().stream().parallel().forEachOrdered(service ->
        {
            servicesIds.add(service.getId());
            services.add(service);
        });

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
                if(parametersIds.isEmpty()){
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
                    Timestamp timestampTo = getTimestampFromRequestDateFiled(monitoredParameterRequest.getDatetimeTo());
                    monitoredParameterValues = monitoredParametersValuesRepository
                            .findByServiceIdsAAndParameterTypeIdWithTimestamp(
                                    servicesIds,
                                    id,
                                    timestampFrom,
                                    timestampTo
                            );
                }
                MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException("Wrong parameter Id"));
                monitoredParameterValues = monitoredParameterValues
                        .parallelStream()
                        .sorted(Comparator.comparing(MonitoredParameterValue::getTimestamp)).collect(Collectors.toList());
                monitoredParameterValuesResponses.add(new MonitoredParameterValuesResponse(
                        String.format(
                                "%s (parameter=%s)",
                                monitoredParameterType.getDescription(),
                                monitoredParameterType.getName()
                        ),
                        convertData(monitoredParameterValues, MAX_PARAMETERS_IN_RESPONSE),
                        MAX_PARAMETERS_IN_RESPONSE,
                        monitoredParameterValues.size()));
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
            } catch (IllegalStateException e){
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
        int step = size / sizeLimit;
        List<MonitoredParameterValue> d = new ArrayList<>(sizeLimit);
        for (int i = 0; i < (sizeLimit - 1); i++) {

            d.add(data.get(i * step));
        }
        d.add(data.get(size - 1));

        return d;
    }
}
