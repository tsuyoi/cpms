package io.cresco.cpms.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@SuppressWarnings("unused")
public class BasicCPMSLogger implements CPMSLogger {
    private static final Logger msgEventLogger = LoggerFactory.getLogger("MSGEVENT");

    private final Logger logger;
    private String pipelineRunId;
    private String taskRunId;
    private String taskCommand;

    public BasicCPMSLogger() {
        this(BasicCPMSLogger.class);
    }

    public BasicCPMSLogger(String pipelineRunId) {
        this(BasicCPMSLogger.class, pipelineRunId);
    }

    public BasicCPMSLogger(String pipelineRunId, String taskRunId) {
        this(BasicCPMSLogger.class, pipelineRunId, taskRunId);
    }

    public BasicCPMSLogger(String pipelineRunId, String taskRunId, String taskCommand) {
        this(BasicCPMSLogger.class, pipelineRunId, taskRunId, taskCommand);
    }

    public BasicCPMSLogger(Class logClass) {
        this(logClass, null, null, null);
    }

    public BasicCPMSLogger(Class logClass, String pipelineRunId) {
        this(logClass, pipelineRunId, null, null);
    }

    public BasicCPMSLogger(Class logClass, String pipelineRunId, String taskRunId) {
        this(logClass, pipelineRunId, taskRunId, null);
    }

    public BasicCPMSLogger(Class logClass, String pipelineRunId, String taskRunId, String taskCommand) {
        this.logger = LoggerFactory.getLogger(logClass);
        setPipelineRunId(pipelineRunId);
        setTaskRunId(taskRunId);
        setTaskCommand(taskCommand);
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
        if (getTaskCommand() == null)
            msgEventLogger.info(String.format("[%s][%s]\n%s", getPipelineRunId(), getTaskRunId(), customParams));
        else if (getTaskRunId() == null)
            msgEventLogger.info(String.format("[%s]\n%s", getPipelineRunId(), customParams));
        else
            msgEventLogger.info(String.format("[%s][%s][%s]\n%s", getPipelineRunId(), getTaskRunId(), getTaskCommand(), customParams));
    }
    public void cpmsInfo(String message) {
        if (getTaskCommand() == null)
            msgEventLogger.info(String.format("[%s][%s] %s", getPipelineRunId(), getTaskRunId(), message));
        else if (getTaskRunId() == null)
            msgEventLogger.info(String.format("[%s] %s", getPipelineRunId(), message));
        else
            msgEventLogger.info(String.format("[%s][%s][%s] %s", getPipelineRunId(), getTaskRunId(), getTaskCommand(), message));
    }
    public void cpmsInfo(Map<String, String> customParams, String message) {
        if (getTaskCommand() == null)
            msgEventLogger.info(String.format("[%s][%s] %s\n%s", getPipelineRunId(), getTaskRunId(), message, customParams));
        else if (getTaskRunId() == null)
            msgEventLogger.info(String.format("[%s] %s\n%s", getPipelineRunId(), message, customParams));
        else
            msgEventLogger.info(String.format("[%s][%s][%s] %s\n%s", getPipelineRunId(), getTaskRunId(), getTaskCommand(), message, customParams));
    }
    public void cpmsInfo(String message, Object... objects) {
        cpmsInfo(replaceBrackets(message, objects));
    }
    public void cpmsInfo(Map<String, String> customParams, String message, Object... objects) {
        cpmsInfo(replaceBrackets(message, objects), customParams);
    }

    public void cpmsError(Map<String, String> customParams) {
        if (getTaskCommand() == null)
            msgEventLogger.error(String.format("[%s][%s]\n%s", getPipelineRunId(), getTaskRunId(), customParams));
        else if (getTaskRunId() == null)
            msgEventLogger.error(String.format("[%s]\n%s", getPipelineRunId(), customParams));
        else
            msgEventLogger.error(String.format("[%s][%s][%s]\n%s", getPipelineRunId(), getTaskRunId(), getTaskCommand(), customParams));
    }
    public void cpmsError(String message) {
        if (getTaskCommand() == null)
            msgEventLogger.error(String.format("[%s][%s] %s", getPipelineRunId(), getTaskRunId(), message));
        else if (getTaskRunId() == null)
            msgEventLogger.error(String.format("[%s] %s", getPipelineRunId(), message));
        else
            msgEventLogger.error(String.format("[%s][%s][%s] %s", getPipelineRunId(), getTaskRunId(), getTaskCommand(), message));
    }
    public void cpmsError(Map<String, String> customParams, String message) {
        if (getTaskCommand() == null)
            msgEventLogger.error(String.format("[%s][%s] %s\n%s", getPipelineRunId(), getTaskRunId(), message, customParams));
        else if (getTaskRunId() == null)
            msgEventLogger.error(String.format("[%s] %s\n%s", getPipelineRunId(), message, customParams));
        else
            msgEventLogger.error(String.format("[%s][%s][%s] %s\n%s", getPipelineRunId(), getTaskRunId(), getTaskCommand(), message, customParams));
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

    public CPMSLogger cloneLogger(Class clazz) {
        return new BasicCPMSLogger(clazz, getPipelineRunId(), getTaskRunId(), getTaskCommand());
    }

    @Override
    public String getPipelineRunId() {
        return pipelineRunId;
    }
    @Override
    public void setPipelineRunId(String pipelineRunId) {
        this.pipelineRunId = pipelineRunId;
    }

    @Override
    public String getTaskRunId() {
        return taskRunId;
    }
    @Override
    public void setTaskRunId(String taskRunId) {
        this.taskRunId = taskRunId;
    }

    @Override
    public String getTaskCommand() {
        return taskCommand;
    }
    @Override
    public void setTaskCommand(String taskCommand) {
        this.taskCommand = taskCommand;
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
