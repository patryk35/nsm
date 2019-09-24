package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pdm.networkservicesmonitor.payload.client.LogValue;
import pdm.networkservicesmonitor.payload.client.LogsRequest;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.service.LogsService;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/logs")
public class LogsViewerController {

    @Autowired
    private LogsService logsService;

    @PostMapping("/load")
    public PagedResponse<LogValue> getLogEntries(@Valid @RequestBody LogsRequest logsRequest) {
        return logsService.getLogsByQuery(logsRequest);
    }
}
