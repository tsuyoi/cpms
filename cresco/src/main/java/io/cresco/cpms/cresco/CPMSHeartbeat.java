package io.cresco.cpms.cresco;

import java.util.Date;

public class CPMSHeartbeat {
    private Date ts;
    private String region;
    private String agent;
    private String plugin;
    private String runID;
    private String pipelineID;
    private String jobID;
    private String taskID;

    public CPMSHeartbeat(String region, String agent, String plugin,
                         String runID, String pipelineID, String jobID, String taskID) {
        this.region = region;
        this.agent = agent;
        this.plugin = plugin;
        this.runID = runID;
        this.pipelineID = pipelineID;
        this.jobID = jobID;
        this.taskID = taskID;
    }

    public CPMSHeartbeat(CPMSHeartbeatBuilder builder) {
        this.region = builder.getRegion();
        this.agent = builder.getAgent();
        this.plugin = builder.getPlugin();
        this.runID = builder.getRunID();
        this.pipelineID = builder.getPipelineID();
        this.jobID = builder.getJobID();
        this.taskID = builder.getTaskID();
    }

    public Date getTs() {
        return ts;
    }
    public void setTs(Date ts) {
        this.ts = ts;
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
