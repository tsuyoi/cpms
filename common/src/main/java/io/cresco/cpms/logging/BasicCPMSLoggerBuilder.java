package io.cresco.cpms.logging;

public class BasicCPMSLoggerBuilder {
    private Class cls;
    private String pipelineID;
    private String pipelineName;
    private String jobID;
    private String jobName;
    private String taskID;
    private String taskName;
    private String runID;
    private String runName;

    public BasicCPMSLoggerBuilder() {
        this.cls = BasicCPMSLogger.class;
    }

    public BasicCPMSLoggerBuilder withClass(Class cls) {
        this.cls = cls;
        return this;
    }

    public BasicCPMSLoggerBuilder withPipelineID(String pipelineID) {
        this.pipelineID = pipelineID;
        return this;
    }

    public BasicCPMSLoggerBuilder withPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
        return this;
    }

    public BasicCPMSLoggerBuilder withJobID(String jobID) {
        this.jobID = jobID;
        return this;
    }

    public BasicCPMSLoggerBuilder withJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public BasicCPMSLoggerBuilder withTaskID(String taskID) {
        this.taskID = taskID;
        return this;
    }

    public BasicCPMSLoggerBuilder withTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    public BasicCPMSLoggerBuilder withRunID(String runID) {
        this.runID = runID;
        return this;
    }

    public BasicCPMSLoggerBuilder withRunName(String runName) {
        this.runName = runName;
        return this;
    }

    public BasicCPMSLogger build() {
        BasicCPMSLogger basicCPMSLogger = new BasicCPMSLogger(this);
        validateBasicCPMSLoggerObject(basicCPMSLogger);
        return basicCPMSLogger;
    }

    private void validateBasicCPMSLoggerObject(BasicCPMSLogger basicCPMSLogger) {
        //Todo: Add some validation here
    }

    public Class getCls() {
        return cls;
    }

    public String getPipelineID() {
        return pipelineID;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public String getJobID() {
        return jobID;
    }

    public String getJobName() {
        return jobName;
    }

    public String getTaskID() {
        return taskID;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getRunID() {
        return runID;
    }

    public String getRunName() {
        return runName;
    }
}
