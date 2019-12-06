package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.CreateResponse;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.*;
import pdm.networkservicesmonitor.payload.client.auth.DataAvailability;
import pdm.networkservicesmonitor.service.AgentServicesService;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/agent/service")
public class AgentServicesController {

    @Autowired
    private AgentServicesService agentServicesService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> createService(@Valid @RequestBody ServiceCreateRequest serviceCreateRequest) {
        Service service = agentServicesService.createService(serviceCreateRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{serviceId}")
                .buildAndExpand(service.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Service Created Successfully", HttpStatus.OK, service.getId()));
    }

    @GetMapping("/getNameAvailability/{agentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public DataAvailability checkServiceNameAvailability(@RequestParam(value = "name") String name, @PathVariable UUID agentId) {
        return new DataAvailability(agentServicesService.checkServiceNameAvailability(name, agentId), true, "", HttpStatus.OK);
    }

    @PostMapping("/logConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> addLogsConfiguration(@Valid @RequestBody ServiceAddLogsConfigurationRequest serviceAddLogsConfigurationRequest) {
        LogsCollectingConfiguration configuration = agentServicesService.addLogsCollectionConfiguration(serviceAddLogsConfigurationRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{logConfigurationId}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Configuration Added Successfully", HttpStatus.OK, configuration.getId()));
    }


    @PostMapping("/parameterConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> addMonitoredParameterConfiguration(@Valid @RequestBody ServiceAddMonitoredParameterConfigurationRequest serviceAddMonitoredParameterConfigurationRequest) {
        MonitoredParameterConfiguration configuration = agentServicesService.addMonitoredParameterConfiguration(serviceAddMonitoredParameterConfigurationRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{monitoredParameterConfiguration}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Configuration Added Successfully", HttpStatus.OK, configuration.getId()));
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> deleteService(@PathVariable UUID serviceId) {
        agentServicesService.deleteService(serviceId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/").build().toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Service deleted successfully", HttpStatus.OK));
    }

    @GetMapping("/details/{serviceId}")
    public ServiceResponse getAgentDetailsById(@PathVariable UUID serviceId) {
        return agentServicesService.getServiceDetailsById(serviceId);
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> editService(@Valid @RequestBody ServiceEditRequest serviceEditRequest) {
        agentServicesService.editService(serviceEditRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{serviceId}")
                .buildAndExpand(serviceEditRequest.getServiceId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Agent edited successfully", HttpStatus.OK));
    }

    @PatchMapping("/parameterConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> editLogsConfiguration(@Valid @RequestBody ServiceEditMonitoredParameterConfigurationRequest serviceEditRequest) {
        agentServicesService.editMonitoringConfiguration(serviceEditRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{configurationId}")
                .buildAndExpand(serviceEditRequest.getConfigurationId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Configuration edited successfully", HttpStatus.OK));
    }

    @PatchMapping("/logConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> editMonitoringConfiguration(@Valid @RequestBody ServiceEditLogsConfigurationRequest serviceEditRequest) {
        agentServicesService.editLogsConfiguration(serviceEditRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{configurationId}")
                .buildAndExpand(serviceEditRequest.getConfigurationId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Configuration edited successfully", HttpStatus.OK));
    }

    @DeleteMapping("/parameterConfig/{configurationId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> deleteMonitoredParameterConfiguration(@PathVariable UUID configurationId) {
        agentServicesService.deleteMonitoredParameterConfiguration(configurationId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/").build().toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Configuration deleted successfully", HttpStatus.OK));
    }

    @GetMapping("/parameterConfigs/details/{serviceId}")
    public PagedResponse<ServiceMonitoringConfigurationResponse> getMonitoringConfigurationDetailsByServiceId(@PathVariable UUID serviceId,
                                                                                                              @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                                                                              @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return agentServicesService.getServiceMonitoringConfigurationDetailsByServiceId(serviceId, page, size);
    }

    @GetMapping("/logConfigs/details/{serviceId}")
    public PagedResponse<ServiceLogsConfigurationResponse> getLogsConfigurationDetailsByServiceId(@PathVariable UUID serviceId,
                                                                                                  @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                                                                  @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return agentServicesService.getServiceLogsConfigurationDetailsByServiceId(serviceId, page, size);
    }

    @GetMapping("/logConfig/details/{configurationId}")
    public ServiceLogsConfigurationResponse getLogsConfigurationDetailsById(@PathVariable UUID configurationId) {
        return agentServicesService.getServiceLogsConfigurationDetailsById(configurationId);
    }

    @GetMapping("/parameterConfig/details/{configurationId}")
    public ServiceMonitoringConfigurationResponse getMonitoringConfigurationDetailsById(@PathVariable UUID configurationId) {
        return agentServicesService.getServiceMonitoringConfigurationDetailsById(configurationId);
    }

    @DeleteMapping("/logConfig/{configurationId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> deleteLogsConfiguration(@PathVariable UUID configurationId) {
        agentServicesService.deleteLogsCollectingConfiguration(configurationId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/").build().toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Configuration deleted successfully", HttpStatus.OK));
    }

    @GetMapping("/parameterConfig/available/{serviceId}")
    public List<ParameterTypeResponse> getAvailableMonitoringParameters(@PathVariable UUID serviceId) {
        return agentServicesService.getAvailableParameters(serviceId);
    }

    @GetMapping("/parameterConfig/added/{serviceId}")
    public List<ParameterTypeResponse> getAddedMonitoringParameters(@PathVariable UUID serviceId) {
        return agentServicesService.getAddedParameters(serviceId);
    }
}
