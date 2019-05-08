package pdm.networkservicesmonitor.agent.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pdm.networkservicesmonitor.agent.AppConstants;
import pdm.networkservicesmonitor.agent.payloads.*;
import pdm.networkservicesmonitor.agent.settings.JwtTokenProvider;
import pdm.networkservicesmonitor.agent.model.Settings;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
@Slf4j
public class MonitorWebClient {
    @Value("${agent.id}")
    private UUID agentId;

    @Value("${agent.encryptionKey}")
    private UUID encryptionKey;

    @Value("${agent.monitor.address}")
    private String monitorAddress;

    @Value("${agent.monitor.port}")
    private String monitorPort;

    @Value("${agent.monitor.api.uri}")
    private String apiURI;


    @Autowired
    public JwtTokenProvider jwtTokenProvider;

    private WebClient monitorWebClient;

    @PostConstruct
    public void init(){
        String monitorURL = String.format("http://%s:%s/%s/%s", monitorAddress, monitorPort, apiURI, AppConstants.AGENT_SERVICE_UTI);
        log.trace(String.format("Monitor Agent Service URL: %s", monitorURL));
        log.trace(String.format("Agent Id: %s", agentId.toString()));
        log.trace(String.format("Agent Encryption Key: %s", encryptionKey.toString()));
        this.monitorWebClient = WebClient
                .builder()
                .baseUrl(monitorURL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "NetworkServicesMonitor Agent")
                .build();
    }


    public RegistrationStatusResponseToAgent getRegistrationStatus(){
        AgentToMonitorBaseRequest agentToMonitorBaseRequest = new AgentToMonitorBaseRequest(agentId);
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/checkRegistrationStatus/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentToMonitorBaseRequest))
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(RegistrationStatusResponseToAgent.class)
                .block();
    }

    public MonitorToAgentBaseResponse registerAgent() {
        AgentToMonitorBaseRequest agentToMonitorBaseRequest = new AgentToMonitorBaseRequest(agentId);
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentToMonitorBaseRequest))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(MonitorToAgentBaseResponse.class)
                .block();
    }

    public Settings downloadSettings() {
        AgentToMonitorBaseRequest agentToMonitorBaseRequest = new AgentToMonitorBaseRequest(agentId);
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/getAgentSettings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentToMonitorBaseRequest))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(Settings.class)
                .block();
    }

    public MonitorToAgentBaseResponse sendPacket(DataPacket dataPacket) {
        log.trace("Packet sent");
        dataPacket.getLogs().stream().forEach(log::error);

        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/agentGateway")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(dataPacket))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(MonitorToAgentBaseResponse.class)
                .block();
    }
}
