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
import pdm.networkservicesmonitor.agent.configuration.AgentConfiguration;
import pdm.networkservicesmonitor.agent.payloads.AgentToMonitorBaseRequest;
import pdm.networkservicesmonitor.agent.payloads.MonitorToAgentBaseResponse;
import pdm.networkservicesmonitor.agent.payloads.RegistrationStatusResponseToAgent;
import pdm.networkservicesmonitor.agent.payloads.data.DataPacket;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
@Slf4j
public class MonitorWebClient {
    @Autowired
    public JwtTokenProvider jwtTokenProvider;
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
    @Value("${agent.monitor.api.webserviceednpoint}")
    private String webserviceEndpoint;
    private WebClient monitorWebClient;

    @PostConstruct
    public void init() {
        String monitorURL = String.format("http://%s:%s/%s/%s", monitorAddress, monitorPort, apiURI, webserviceEndpoint);
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


    public RegistrationStatusResponseToAgent getRegistrationStatus() {
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

    public AgentConfiguration downloadAgentConfiguration() {
        AgentToMonitorBaseRequest agentToMonitorBaseRequest = new AgentToMonitorBaseRequest(agentId);
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/getAgentConfiguration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentToMonitorBaseRequest))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(AgentConfiguration.class)
                .block();
    }

    public MonitorToAgentBaseResponse sendPacket(DataPacket dataPacket) {
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
