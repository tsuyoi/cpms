package io.cresco.cpms.cresco;

public class CPMSHeartbeatBuilder {
    private String region;
    private String agent;
    private String plugin;
    private String runID;
    private String pipelineID;
    private String jobID;
    private String taskID;

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

    public CPMSHeartbeatBuilder withRunID(String runID) {
        this.runID = runID;
        return this;
    }

    public CPMSHeartbeatBuilder withPipelineID(String pipelineID) {
        this.pipelineID = pipelineID;
        return this;
    }

    public CPMSHeartbeatBuilder withJobID(String jobID) {
        this.jobID = jobID;
        return this;
    }

    public CPMSHeartbeatBuilder withTaskID(String taskID) {
        this.taskID = taskID;
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

    public String getRunID() {
        return runID;
    }

    public String getPipelineID() {
        return pipelineID;
    }

    public String getJobID() {
        return jobID;
    }

    public String getTaskID() {
        return taskID;
    }
}
