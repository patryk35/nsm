package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.exceptions.ItemExists;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.AgentConfiguration;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentDetailsResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentEditRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceResponse;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.repository.ServiceRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.*;

@org.springframework.stereotype.Service
@Slf4j
public class AgentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ServiceRepository serviceRepository;


    public MonitorAgent createAgent(AgentCreateRequest agentCreateRequest) {
        if (agentRepository.findByName(agentCreateRequest.getName()).isPresent()) {
            throw new ItemExists(String.format("Agent with name `%s` exists! Aborting.", agentCreateRequest.getName()));
        }
        MonitorAgent agent = new MonitorAgent(
                agentCreateRequest.getName(),
                agentCreateRequest.getDescription(),
                convertOriginsToList(agentCreateRequest.getAllowedOrigins()),
                agentCreateRequest.isProxyAgent()
        );

        return agentRepository.save(agent);
    }

    public PagedResponse<AgentResponse> getAllAgents(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<MonitorAgent> agents = agentRepository.findByIsDeleted(false, pageable);
        if (agents.getNumberOfElements() == 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    agents.getNumber(),
                    agents.getSize(),
                    agents.getTotalElements(),
                    agents.getTotalPages(),
                    agents.isLast());
        }
        List<AgentResponse> list = agents.getContent().stream()
                .map(e -> new AgentResponse(
                        e.getId(),
                        e.getName(),
                        e.getDescription(),
                        convertOriginsToString(e.getAllowedOrigins()),
                        e.isRegistered(),
                        e.isProxyAgent()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                list,
                agents.getNumber(),
                agents.getSize(),
                agents.getTotalElements(),
                agents.getTotalPages(),
                agents.isLast()
        );
    }

    /*public AgentResponse getAgentById(UUID agentId) {
        MonitorAgent agent = agentRepository.findById(agentId).orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Id", "id", agentId));
        if(agent.isDeleted()){
            throw new NotFoundException(String.format("Agent with id %s was removed",agentId));
        }
        return new AgentResponse(agent.getId(), agent.getName(), agent.getDescription(), convertOriginsToString(agent.getAllowedOrigins()), agent.isRegistered());
    }*/

    public PagedResponse<ServiceResponse> getAllAgentServices(UUID agentId, int page, int size) {
        MonitorAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Id", "id", agentId));
        if (agent.isDeleted()) {
            throw new NotFoundException(String.format("Agent with id %s was removed", agentId));
        }
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "name");
        Page<Service> agentServices = serviceRepository.findByAgentIdAndIsDeleted(agentId, false, pageable);
        if (agentServices.getNumberOfElements() == 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    agentServices.getNumber(),
                    agentServices.getSize(),
                    agentServices.getTotalElements(),
                    agentServices.getTotalPages(),
                    agentServices.isLast()
            );
        }
        List<ServiceResponse> list = agentServices.getContent().stream()
                .map(e -> new ServiceResponse(
                        e.getId(),
                        e.getName(),
                        e.getDescription()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(list,
                agentServices.getNumber(),
                agentServices.getSize(),
                agentServices.getTotalElements(),
                agentServices.getTotalPages(),
                agentServices.isLast()
        );
    }

    public AgentDetailsResponse getAgentDetailsById(UUID agentId) {
        MonitorAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Id", "id", agentId));
        if (agent.isDeleted()) {
            throw new NotFoundException(String.format("Agent with id %s was removed", agentId));
        }
        return new AgentDetailsResponse(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                convertOriginsToString(agent.getAllowedOrigins()),
                agent.isRegistered(),
                agent.getAgentConfiguration().getSendingInterval(),
                agent.isProxyAgent()
        );
    }

    public void editAgent(AgentEditRequest agentEditRequest) {
        MonitorAgent agent = agentRepository.findById(agentEditRequest.getAgentId())
                .orElseThrow(() ->
                        new NotFoundException(String.format("Agent with id %s doesn't exist", agentEditRequest.getAgentId()))
                );
        if (agent.isDeleted()) {
            throw new NotFoundException(String.format("Agent with id %s was removed", agentEditRequest.getAgentId()));
        }
        agent.setAllowedOrigins(convertOriginsToList(agentEditRequest.getAllowedOrigins()));
        agent.setDescription(agentEditRequest.getDescription());
        AgentConfiguration agentConfiguration = agent.getAgentConfiguration();
        agentConfiguration.setSendingInterval(agentEditRequest.getSendingInterval());
        agentConfiguration.setUpdated(true);
        agentRepository.save(agent);
    }

    public void deleteAgent(UUID agentId) {
        MonitorAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException(String.format("Agent with id %s doesn't exist", agentId)));
        if (agent.isDeleted()) {
            throw new NotFoundException(String.format("Agent with id %s was already removed", agentId));
        }
        agent.setDeleted(true);
        agent.getServices().forEach(s -> s.setDeleted(true));
        agentRepository.save(agent);
    }
}
