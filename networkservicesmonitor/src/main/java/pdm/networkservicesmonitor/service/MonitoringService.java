package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pdm.networkservicesmonitor.exceptions.AppException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
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

    // TODO: Additional parameter for aproximation (if more than x records, count x avgs and return only avgs[time,value])
    public List<MonitoredParameterValuesResponse> getMonitoringByQuery(MonitoredParameterRequest monitoredParameterRequest) {
        Matcher serviceNameMatcher = Pattern
                .compile(".*service=\"(.*?)\".*")
                .matcher(monitoredParameterRequest.getQuery());
        Matcher agentNameMatcher = Pattern
                .compile(".*agent=\"(.*?)\".*")
                .matcher(monitoredParameterRequest.getQuery());
        Matcher parameterNameMatcher = Pattern
                .compile(".*parameter=\"(.*?)\".*")
                .matcher(monitoredParameterRequest.getQuery());

        MonitorAgent agent;

        if (agentNameMatcher.matches()) {
            String monitorName = agentNameMatcher.group(1);
            agent = agentRepository.findByName(monitorName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Not found. Verify Agent Name",
                            "name",
                            monitorName)
                    );
        } else {
            throw new ResourceNotFoundException(
                    "Not found. Verify Agent Name in query",
                    "query",
                    monitoredParameterRequest.getQuery()
            );
        }

        String serviceName = null;
        if (serviceNameMatcher.matches()) {
            serviceName = serviceNameMatcher.group(1);
        }


        List<UUID> servicesIds = new ArrayList<>();
        List<Service> services = new ArrayList<>();

        if (serviceName == null) {
            agent.getServices().stream().parallel().forEachOrdered(service -> {
                servicesIds.add(service.getId());
                services.add(service);
            });
        } else {
            servicesIds.addAll(agent.getServices().parallelStream()
                    .filter(service -> service.getName().equals(serviceNameMatcher))
                    .map(Service::getId)
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
                                .forEach(monitoredParameterConfiguration -> parametersIds.add(
                                        monitoredParameterConfiguration.getParameterType().getId())
                                )
                        );
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
                        monitoredParameterValues));
            });
        }
        return monitoredParameterValuesResponses;
    }

}
