package io.cresco.cpms.logging;

public class CPMSLogMessage {
    private String id;
    private Long ts;
    private String pipelineID;
    private String jobID;
    private String taskID;
    private String runID;
    private String type;
    private String message;

    public CPMSLogMessage() {

    }

    public CPMSLogMessage(CPMSLogMessageBuilder builder) {
        this.id = builder.getId();
        this.ts = builder.getTs();
        this.pipelineID = builder.getPipelineID();
        this.jobID = builder.getJobID();
        this.taskID = builder.getTaskID();
        this.runID = builder.getRunID();
        this.type = builder.getType().name();
        this.message = builder.getMessage();
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

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CPMSLogMessage (");
        if (getId() != null)
            sb.append(String.format("ID:%s", getId()));
        if (getTs() != null)
            sb.append(String.format(",TS:%s", getTs()));
        if (getPipelineID() != null)
            sb.append(String.format(",P:%s", getPipelineID()));
        if (getJobID() != null)
            sb.append(String.format(",J:%s", getJobID()));
        if (getTaskID() != null)
            sb.append(String.format(",T:%s", getTaskID()));
        if (getRunID() != null)
            sb.append(String.format(",R:%s) - ", getRunID()));
        if (getMessage() != null)
            sb.append(getMessage());
        return sb.toString();
    }
}
