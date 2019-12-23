package pdm.networkservicesmonitor.agent.connection;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pdm.networkservicesmonitor.agent.configuration.AgentConfiguration;
import pdm.networkservicesmonitor.agent.payloads.AgentToMonitorBaseRequest;
import pdm.networkservicesmonitor.agent.payloads.MonitorToAgentBaseResponse;
import pdm.networkservicesmonitor.agent.payloads.RegistrationStatusResponseToAgent;
import pdm.networkservicesmonitor.agent.payloads.UpdatesAvailabilityMonitorResponse;
import pdm.networkservicesmonitor.agent.payloads.data.DataPacket;
import pdm.networkservicesmonitor.agent.payloads.proxy.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;
import java.util.UUID;

@Service
@Slf4j
public class MonitorWebClient {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Value("${agent.ssl.validation.enabled}")
    private boolean validateSSL;
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
    public void init() throws SSLException {
        String monitorURL = String.format("https://%s:%s/%s/%s", monitorAddress, monitorPort, apiURI, webserviceEndpoint);
        log.trace(String.format("Monitor Agent Service URL: %s", monitorURL));
        log.trace(String.format("Agent Id: %s", agentId.toString()));
        log.trace(String.format("Agent Encryption Key: %s", encryptionKey.toString()));

        this.monitorWebClient = !validateSSL ? createMonitorWebClientWithDisabledSSL(monitorURL) : WebClient
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
                .uri("/checkRegistrationStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentToMonitorBaseRequest))
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(RegistrationStatusResponseToAgent.class)
                .block();
    }

    public RegistrationStatusResponseToAgent getRegistrationStatusByProxy(AgentRequest agentRequest) {
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/checkRegistrationStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentRequest))
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

    public ApiBaseResponse registerAgentByProxy(AgentRequest agentRequest) {
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentRequest))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(ApiBaseResponse.class)
                .block();
    }

    public AgentConfiguration downloadAgentConfiguration() {
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/getAgentConfiguration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(new AgentToMonitorBaseRequest(agentId)))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(AgentConfiguration.class)
                .block();
    }

    public AgentConfigurationResponse downloadAgentConfigurationByProxy(AgentRequest agentRequest) {
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/getAgentConfiguration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentRequest))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(AgentConfigurationResponse.class)
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

    public AgentDataPacketResponse sendPacketByProxy(DataPacket dataPacket) {
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/agentGateway")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(dataPacket))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(AgentDataPacketResponse.class)
                .block();
    }

    public String testMonitorConnection() {
        return monitorWebClient
                .method(HttpMethod.GET)
                .uri("/health")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public UpdatesAvailabilityMonitorResponse checkConfigurationUpdates() {
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/checkAgentConfigurationUpdates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(new AgentToMonitorBaseRequest(agentId)))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(UpdatesAvailabilityMonitorResponse.class)
                .block();
    }

    public AgentConfigurationUpdatesAvailabilityResponse checkConfigurationUpdatesByProxy(AgentRequest agentRequest) {
        return monitorWebClient
                .method(HttpMethod.POST)
                .uri("/checkAgentConfigurationUpdates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(agentRequest))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .bodyToMono(AgentConfigurationUpdatesAvailabilityResponse.class)
                .block();
    }

    public boolean validateTokenAndRequestIp(String token, String ip) {
        monitorWebClient
                .method(HttpMethod.POST)
                .uri("/validateTokenAndRequestIp")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(new AgentAuthCheckRequest(token, ip)))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwtTokenProvider.createAuthToken()))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> {
                    System.out.println("4xx eror");
                    return Mono.error(new RuntimeException("Proxy -> Agent validation failed."));
                });
        return true;
    }

    private WebClient createMonitorWebClientWithDisabledSSL(String monitorURL) throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        return WebClient
                .builder()
                .baseUrl(monitorURL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "NetworkServicesMonitor Agent")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
