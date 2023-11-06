package io.cresco.cpms.identity;

public class CPMSWorkIdentity {
    private final String pipelineID;
    private final String jobID;
    private final String taskID;
    private final String runID;

    public CPMSWorkIdentity(String pipelineID, String jobID, String taskID, String runID) {
        this.pipelineID = pipelineID;
        this.jobID = jobID;
        this.taskID = taskID;
        this.runID = runID;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Work (");
        if (getPipelineID() != null)
            sb.append(String.format("P:%s", getPipelineID()));
        if (getJobID() != null)
            sb.append(String.format(",J:%s", getJobID()));
        if (getTaskID() != null)
            sb.append(String.format(",T:%s", getTaskID()));
        if (getRunID() != null)
            sb.append(String.format(",R:%s", getRunID()));
        sb.append(")");
        return sb.toString();
    }
}
