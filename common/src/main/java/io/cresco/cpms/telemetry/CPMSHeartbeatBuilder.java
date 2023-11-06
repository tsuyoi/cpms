package io.cresco.cpms.telemetry;

import io.cresco.cpms.identity.CPMSWorkerIdentity;

public class CPMSHeartbeatBuilder {
    private String region;
    private String agent;
    private String plugin;

    public CPMSHeartbeatBuilder() { }

    public CPMSHeartbeatBuilder withRegion(String region) {
        this.region = region;
        return this;
    }

    public CPMSHeartbeatBuilder withAgent(String agent) {
        this.agent = agent;
        return this;
    }

    public CPMSHeartbeatBuilder withPlugin(String plugin) {
        this.plugin = plugin;
        return this;
    }

    public CPMSHeartbeat build() {
        CPMSHeartbeat heartbeat = new CPMSHeartbeat(this);
        validateCPMSHeartbeatObject(heartbeat);
        return heartbeat;
    }

    public void validateCPMSHeartbeatObject(CPMSHeartbeat heartbeat) {
        //Todo: Add some validation here
    }

    public String getRegion() {
        return region;
    }

    public String getAgent() {
        return agent;
    }

    public String getPlugin() {
        return plugin;
    }

    public CPMSWorkerIdentity getWorkerIdentity() {
        return new CPMSWorkerIdentity(getRegion(), getAgent(), getPlugin());
    }
}
