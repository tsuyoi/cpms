package io.cresco.cpms.logging;

import java.util.Date;

public class CPMSLogMessageBuilder {
    private final String id;
    private final Long ts;
    private String pipelineID;
    private String jobID;
    private String taskID;
    private String runID;
    private CPMSLogMessageType type;
    private String message;

    public CPMSLogMessageBuilder() {
        this.id = java.util.UUID.randomUUID().toString();
        this.ts = new Date().getTime();
    }

    public CPMSLogMessageBuilder withPipelineID(String pipelineID) {
        this.pipelineID = pipelineID;
        return this;
    }

    public CPMSLogMessageBuilder withJobID(String jobID) {
        this.jobID = jobID;
        return this;
    }

    public CPMSLogMessageBuilder withTaskID(String taskID) {
        this.taskID = taskID;
        return this;
    }

    public CPMSLogMessageBuilder withRunID(String runID) {
        this.runID = runID;
        return this;
    }

    public CPMSLogMessageBuilder withType(CPMSLogMessageType type) {
        this.type = type;
        return this;
    }

    public CPMSLogMessageBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public CPMSLogMessage build() {
        CPMSLogMessage cpmsLogMessage = new CPMSLogMessage(this);
        validateCPMSLogMessageObject(cpmsLogMessage);
        return cpmsLogMessage;
    }

    private void validateCPMSLogMessageObject(CPMSLogMessage cpmsLogMessage) {
        //Todo: Add some validation here
    }

    public String getId() {
        return id;
    }

    public Long getTs() {
        return ts;
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

    public String getRunID() {
        return runID;
    }

    public CPMSLogMessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
