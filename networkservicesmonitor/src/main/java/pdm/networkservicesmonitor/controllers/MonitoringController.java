package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pdm.networkservicesmonitor.payload.client.MonitoredParameterRequest;
import pdm.networkservicesmonitor.payload.client.MonitoredParameterValuesResponse;
import pdm.networkservicesmonitor.service.MonitoringService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/monitoring")
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    @PostMapping("/load")
    public List<MonitoredParameterValuesResponse> getMonitoredParameters(@Valid @RequestBody MonitoredParameterRequest monitoredParameterRequest) {
        return monitoringService.getMonitoringByQuery(monitoredParameterRequest);
    }
}
