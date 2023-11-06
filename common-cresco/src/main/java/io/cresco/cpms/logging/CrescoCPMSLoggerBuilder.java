package io.cresco.cpms.logging;

import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class CrescoCPMSLoggerBuilder {
    private Class cls;
    private PluginBuilder pluginBuilder;
    private String pipelineID;
    private String pipelineName;
    private String jobID;
    private String jobName;
    private String taskID;
    private String taskName;
    private String runID;
    private String runName;
    private CLogger.Level logLevel;

    public CrescoCPMSLoggerBuilder() {
        this.cls = CrescoCPMSLogger.class;
    }

    public CrescoCPMSLoggerBuilder withClass(Class cls) {
        this.cls = cls;
        return this;
    }

    public CrescoCPMSLoggerBuilder withPluginBuilder(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        return this;
    }

    public CrescoCPMSLoggerBuilder withPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
        return this;
    }

    public CrescoCPMSLoggerBuilder withPipelineID(String pipelineID) {
        this.pipelineID = pipelineID;
        return this;
    }

    public CrescoCPMSLoggerBuilder withJobID(String jobID) {
        this.jobID = jobID;
        return this;
    }

    public CrescoCPMSLoggerBuilder withJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public CrescoCPMSLoggerBuilder withTaskID(String taskID) {
        this.taskID = taskID;
        return this;
    }

    public CrescoCPMSLoggerBuilder withTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    public CrescoCPMSLoggerBuilder withRunID(String runID) {
        this.runID = runID;
        return this;
    }

    public CrescoCPMSLoggerBuilder withRunName(String runName) {
        this.runName = runName;
        return this;
    }

    public CrescoCPMSLoggerBuilder withLogLevel(CLogger.Level logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public CrescoCPMSLogger build() {
        CrescoCPMSLogger crescoCPMSLogger = new CrescoCPMSLogger(this);
        validateCrescoCPMSLoggerObject(crescoCPMSLogger);
        return crescoCPMSLogger;
    }

    private void validateCrescoCPMSLoggerObject(CrescoCPMSLogger crescoCPMSLogger) {
        //Todo: Add some validation here
    }

    public Class getCls() {
        return cls;
    }

    public PluginBuilder getPluginBuilder() {
        return pluginBuilder;
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

    public CLogger.Level getLogLevel() {
        return logLevel;
    }
}
