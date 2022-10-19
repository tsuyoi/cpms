package io.cresco.cpms.logging;

import io.cresco.cpms.scripting.ScriptedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@SuppressWarnings("unused")
public class BasicCPMSLogger implements CPMSLogger {
    private static final Logger msgEventLogger = LoggerFactory.getLogger("MSGEVENT");

    private final Logger logger;
    private String pipelineID;
    private String pipelineName;
    private String jobID;
    private String jobName;
    private String taskID;
    private String taskName;
    private String runID;
    private String runName;

    public BasicCPMSLogger(BasicCPMSLoggerBuilder builder) {
        this.logger = LoggerFactory.getLogger(builder.getCls());
        this.pipelineID = builder.getPipelineID();
        this.pipelineName = builder.getPipelineName();
        this.jobID = builder.getJobID();
        this.jobName = builder.getJobName();
        this.taskID = builder.getTaskID();
        this.taskName = builder.getTaskName();
        this.runID = builder.getRunID();
        this.runName = builder.getRunName();
    }

    public void trace(String message) {
        logger.trace(message);
    }
    public void trace(String message, Object... objects) {
        logger.trace(message, objects);
    }

    public void debug(String message) {
        logger.debug(message);
    }
    public void debug(String message, Object... objects) {
        logger.debug(message, objects);
    }

    public void info(String message) {
        logger.info(message);
    }
    public void info(String message, Object... objects) {
        logger.info(message, objects);
    }

    public void warn(String message) {
        logger.warn(message);
    }
    public void warn(String message, Object... objects) {
        logger.warn(message, objects);
    }

    public void error(String message) {
        logger.error(message);
    }
    public void error(String message, Object... objects) {
        logger.error(message, objects);
    }

    public void cpmsInfo(Map<String, String> customParams) {
        msgEventLogger.info(formalizeLogMessage(String.format("\n%s", customParams)));
    }
    public void cpmsInfo(String message) {
        msgEventLogger.info(formalizeLogMessage(message));
    }
    public void cpmsInfo(Map<String, String> customParams, String message) {
        msgEventLogger.info(formalizeLogMessage(String.format("%s\n%s", message, customParams)));
    }
    public void cpmsInfo(String message, Object... objects) {
        cpmsInfo(replaceBrackets(message, objects));
    }
    public void cpmsInfo(Map<String, String> customParams, String message, Object... objects) {
        cpmsInfo(replaceBrackets(message, objects), customParams);
    }

    public void cpmsWarn(String message) {
        msgEventLogger.warn(formalizeLogMessage(message));
    }
    public void cpmsWarn(String message, Object... objects) {
        cpmsWarn(replaceBrackets(message, objects));
    }

    public void cpmsError(Map<String, String> customParams) {
        msgEventLogger.error(formalizeLogMessage(String.format("\n%s", customParams)));
    }
    public void cpmsError(String message) {
        msgEventLogger.error(formalizeLogMessage(message));
    }
    public void cpmsError(Map<String, String> customParams, String message) {
        msgEventLogger.error(formalizeLogMessage(String.format("%s\n%s", message, customParams)));
    }
    public void cpmsError(String message, Object... objects) {
        cpmsError(replaceBrackets(message, objects));
    }
    public void cpmsError(Map<String, String> customParams, String message, Object... objects) {
        cpmsError(replaceBrackets(message, objects), customParams);
    }

    public void cpmsFailure(String message) {
        cpmsError(message);
    }
    public void cpmsFailure(String message, Object... objects) {
        cpmsError(message, objects);
    }

    public void cpmsTaskOutput(ScriptedTask scriptedTask, String output) {
        msgEventLogger.info(formalizeLogMessage("TASK:\n" + scriptedTask.toJson() + "\nOUTPUT:\n" + output));
    }

    private String formalizeLogMessage(String logMessage) {
        StringBuilder sb = new StringBuilder();
        if (getPipelineID() != null)
            sb.append(String.format("[P:%s:%s]", getPipelineID(), getPipelineName()));
        if (getJobID() != null)
            sb.append(String.format("[J:%s:%s]", getJobID(), getJobName()));
        if (getTaskID() != null)
            sb.append(String.format("[T:%s:%s]", getTaskID(), getTaskName()));
        if (getRunID() != null)
            sb.append(String.format("[R:%s:%s]", getRunName(), getRunName()));
        if (getPipelineID() != null || getJobID() != null || getTaskID() != null || getRunID() != null)
            sb.append(" ");
        sb.append(logMessage);
        return sb.toString();
    }

    public CPMSLogger cloneLogger(Class cls) {
        return new BasicCPMSLoggerBuilder()
                .withClass(cls)
                .withPipelineID(getPipelineID())
                .withPipelineName(getPipelineName())
                .withJobID(getJobID())
                .withJobName(getJobName())
                .withRunID(getRunID())
                .withRunName(getRunName())
                .withTaskID(getTaskID())
                .withTaskName(getTaskName())
                .build();
    }

    public String getPipelineID() {
        return pipelineID;
    }
    public void setPipelineID(String pipelineID) {
        this.pipelineID = pipelineID;
    }

    @Override
    public String getPipelineName() {
        return pipelineName;
    }
    @Override
    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getJobID() {
        return jobID;
    }
    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    @Override
    public String getJobName() {
        return jobName;
    }
    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTaskID() {
        return taskID;
    }
    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }
    @Override
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getRunID() {
        return runID;
    }
    public void setRunID(String runID) {
        this.runID = runID;
    }

    @Override
    public String getRunName() {
        return runName;
    }
    @Override
    public void setRunName(String runName) {
        this.runName = runName;
    }

    private String replaceBrackets(String logMessage, Object... params) {
        int replaced = 0;
        while (logMessage.contains("{}") && replaced < params.length) {
            logMessage = logMessage.replaceFirst("\\{}", String.valueOf(params[replaced]).replace("\\", "\\\\"));
            replaced++;
        }
        return logMessage;
    }
}
