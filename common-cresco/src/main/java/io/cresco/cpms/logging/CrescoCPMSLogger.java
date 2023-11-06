package io.cresco.cpms.logging;

import com.google.gson.Gson;
import io.cresco.cpms.statics.CrescoCPMSStatics;
import io.cresco.cpms.telemetry.*;
import io.cresco.cpms.scripting.ScriptedTask;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.jms.JMSException;
import javax.jms.TextMessage;

public class CrescoCPMSLogger implements CPMSLogger {
    private final static Gson gson = new Gson();
    private PluginBuilder pluginBuilder;
    private final CLogger logger;
    private String pipelineID;
    private String pipelineName;
    private String jobID;
    private String jobName;
    private String taskID;
    private String taskName;
    private String runID;
    private String runName;
    private CLogger.Level level;

    public CrescoCPMSLogger(CrescoCPMSLoggerBuilder builder) {
        setPluginBuilder(builder.getPluginBuilder());
        this.logger = pluginBuilder.getLogger(builder.getCls().getName(), builder.getLogLevel());
        setPipelineID(builder.getPipelineID());
        setPipelineName(builder.getPipelineName());
        setJobID(builder.getJobID());
        setJobName(builder.getJobName());
        setTaskID(builder.getTaskID());
        setTaskName(builder.getTaskName());
        setRunID(builder.getRunID());
        setRunName(builder.getRunName());
        setLevel(builder.getLogLevel());
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

    public void cpmsHeartbeat() {
        logger.trace("Sending heartbeat");
        sendCPMSHeartbeat();
    }

    public void cpmsInfo(String message) {
        logger.info(message);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.INFO, message));
    }

    public void cpmsInfo(String message, Object... objects) {
        logger.info(message, objects);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.INFO, replaceBrackets(message, objects)));
    }

    public void cpmsWarn(String message) {
        logger.warn(message);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.WARN, message));
    }

    public void cpmsWarn(String message, Object... objects) {
        logger.warn(message, objects);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.WARN, replaceBrackets(message, objects)));
    }

    public void cpmsError(String message) {
        logger.error(message);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.ERROR, message));
    }

    public void cpmsError(String message, Object... objects) {
        logger.error(message, objects);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.ERROR, replaceBrackets(message, objects)));
    }

    public void cpmsFailure(String message) {
        logger.error("FAILURE: {}", message);
        CPMSLogMessage failMsg = genCPMSLogMessage(CPMSLogMessageType.FAILURE, message);
        sendCPMSLogMessage(failMsg);
    }

    public void cpmsFailure(String message, Object... objects) {
        logger.error("FAILURE: {}", replaceBrackets(message, objects));
        CPMSLogMessage failMsg = genCPMSLogMessage(CPMSLogMessageType.FAILURE, replaceBrackets(message, objects));
        sendCPMSLogMessage(failMsg);
    }

    public void cpmsTaskOutput(ScriptedTask scriptedTask, String output) {
        logger.info("Sending output for task: {}", scriptedTask.toJson());
        sendCPMSTaskOutput(scriptedTask, output);
    }

    public CPMSLogger cloneLogger(Class cls) {
        return new CrescoCPMSLoggerBuilder()
                .withClass(cls)
                .withPluginBuilder(getPluginBuilder())
                .withPipelineID(getPipelineID())
                .withPipelineName(getPipelineName())
                .withJobID(getJobID())
                .withJobName(getJobName())
                .withTaskID(getTaskID())
                .withTaskName(getTaskName())
                .withRunID(getRunID())
                .withRunName(getRunName())
                .withLogLevel(getLevel())
                .build();
    }

    public PluginBuilder getPluginBuilder() {
        return pluginBuilder;
    }
    public void setPluginBuilder(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
    }

    public String getPipelineID() {
        return pipelineID;
    }
    public void setPipelineID(String pipelineID) {
        this.pipelineID = pipelineID;
    }

    public String getPipelineName() {
        return pipelineName;
    }
    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getJobID() {
        return jobID;
    }
    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getJobName() {
        return jobName;
    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTaskID() {
        return taskID;
    }
    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getTaskName() {
        return taskName;
    }
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getRunID() {
        return runID;
    }
    public void setRunID(String runID) {
        this.runID = runID;
    }

    public String getRunName() {
        return runName;
    }
    public void setRunName(String runName) {
        this.runName = runName;
    }

    public CLogger.Level getLevel() {
        return level;
    }

    public void setLevel(CLogger.Level level) {
        this.level = level;
    }

    private CPMSLogMessage genCPMSLogMessage(CPMSLogMessageType type) {
        return new CPMSLogMessageBuilder()
                .withPipelineID(getPipelineID())
                .withJobID(getJobID())
                .withTaskID(getTaskID())
                .withRunID(getRunID())
                .withType(type)
                .build();
    }

    private CPMSLogMessage genCPMSLogMessage(CPMSLogMessageType type, String message) {
        return new CPMSLogMessageBuilder()
                .withPipelineID(getPipelineID())
                .withJobID(getJobID())
                .withTaskID(getTaskID())
                .withRunID(getRunID())
                .withType(type)
                .withMessage(message)
                .build();
    }

    private void sendCPMSHeartbeat() {
        CPMSHeartbeat heartbeat = new CPMSHeartbeatBuilder()
                .withRegion(pluginBuilder.getRegion())
                .withAgent(pluginBuilder.getAgent())
                .withPlugin(pluginBuilder.getPluginID())
                .build();
        try {
            TextMessage cpmsLogDataPlaneMessage = pluginBuilder.getAgentService().getDataPlaneService()
                    .createTextMessage();
            cpmsLogDataPlaneMessage.setText(gson.toJson(heartbeat));
            cpmsLogDataPlaneMessage.setStringProperty(CrescoCPMSStatics.LOGGING_DATA_PLANE_KEY,
                    CrescoCPMSStatics.HEARTBEAT_MESSAGES_DATA_PLANE_VALUE);
            pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, cpmsLogDataPlaneMessage);
        } catch (JMSException e) {
            logger.error("Failed to generate heartbeat message: {}, code: {}", e.getMessage(), e.getErrorCode());
            logger.error("JMSException:\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    private void sendCPMSLogMessage(CPMSLogMessage logMessage) {
        try {
            TextMessage cpmsLogDataPlaneMessage = pluginBuilder.getAgentService().getDataPlaneService()
                    .createTextMessage();
            cpmsLogDataPlaneMessage.setText(gson.toJson(logMessage));
            cpmsLogDataPlaneMessage.setStringProperty(CrescoCPMSStatics.LOGGING_DATA_PLANE_KEY,
                    CrescoCPMSStatics.LOG_MESSAGES_DATA_PLANE_VALUE);
            pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, cpmsLogDataPlaneMessage);
        } catch (JMSException e) {
            logger.error("Failed to generate log message: {}, code: {}", e.getMessage(), e.getErrorCode());
            logger.error("JMSException:\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    private void sendCPMSTaskOutput(ScriptedTask scriptedTask, String output) {
        CPMSTaskOutput taskOutput = new CPMSTaskOutput(scriptedTask.toJson(), output);
        try {
            TextMessage cpmsLogDataPlaneMessage = pluginBuilder.getAgentService().getDataPlaneService()
                    .createTextMessage();
            cpmsLogDataPlaneMessage.setText(gson.toJson(taskOutput));
            cpmsLogDataPlaneMessage.setStringProperty(CrescoCPMSStatics.LOGGING_DATA_PLANE_KEY,
                    CrescoCPMSStatics.TASK_OUTPUT_DATA_PLANE_VALUE);
            pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, cpmsLogDataPlaneMessage);
        } catch (JMSException e) {
            logger.error("Failed to generate task output message: {}, code: {}", e.getMessage(), e.getErrorCode());
            logger.error("JMSException:\n" + ExceptionUtils.getStackTrace(e));
        }
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
