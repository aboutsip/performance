package com.aboutsip.performance.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class ScenarioConfig {

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private Map<String, SIPpInstanceConfig> clients;

    @JsonProperty
    private List<String> actions;

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Map<String, SIPpInstanceConfig> getClients() {
        return clients;
    }

    public void setClients(final Map<String, SIPpInstanceConfig> clients) {
        this.clients = clients;
    }

    @JsonIgnore
    public Optional<SIPpInstanceConfig> getClient(final String name) {
        return Optional.ofNullable(clients.get(name));
    }

    public void setActions(final List<String> actions) {
        this.actions = actions;
    }

    public List<String> getActions() {
        return actions;
    }
}
