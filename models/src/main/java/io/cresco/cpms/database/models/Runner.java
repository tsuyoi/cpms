package io.cresco.cpms.database.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@SuppressWarnings({"unused", "WeakerAccess"})
@Entity
@Table( name = "runner" )
public class Runner extends GenericModel {
    @NotNull
    @Size( max = 255 )
    @Column( name = "region", nullable = false )
    private String region;

    @NotNull
    @Size( max = 255 )
    @Column( name = "agent", nullable = false )
    private String agent;

    @NotNull
    @Size( max = 255 )
    @Column( name = "plugin", nullable = false )
    private String plugin;

    @NotNull
    @Size( max = 255 )
    @Column( name = "identifier", unique = true, updatable = false, nullable = false )
    private String identifier;

    public Runner() {
        super();
    }

    public Runner(String region, String agent, String plugin, String identifier) {
        this();
        this.region = region;
        this.agent = agent;
        this.plugin = plugin;
        this.identifier = identifier;
    }

    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }

    public String getAgent() {
        return agent;
    }
    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getPlugin() {
        return plugin;
    }
    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}

