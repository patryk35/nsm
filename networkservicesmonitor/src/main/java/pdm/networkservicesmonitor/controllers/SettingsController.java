package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.model.data.SettingsObject;
import pdm.networkservicesmonitor.service.SettingsService;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public SettingsObject get() {
        return settingsService.get();
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse editAgent(@Valid @RequestBody SettingsObject settingsObject) {
        settingsService.update(settingsObject);

        return new ApiBaseResponse(true, "Settings updated successfully", HttpStatus.OK);
    }
}
