package io.cresco.cpms.cresco;

import com.google.gson.Gson;
import io.cresco.cpms.logging.*;
import io.cresco.cpms.scripting.ScriptedTask;
import io.cresco.cpms.statics.CPMSStatics;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.jms.JMSException;
import javax.jms.TextMessage;

public class CrescoCPMSLogger implements CPMSLogger {
    private static Gson gson = new Gson();
    private CLogger logger;
    private PluginBuilder pluginBuilder;
    private String pipelineID;
    private String pipelineName;
    private String jobID;
    private String jobName;
    private String taskID;
    private String taskName;
    private String runID;
    private String runName;
    private CLogger.Level level;

    /*
    // Pluginbuilder-only Logger

    public CrescoCPMSLogger(PluginBuilder pluginBuilder) {
        this(CrescoCPMSLogger.class, pluginBuilder, null, null, null, null, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, CLogger.Level level) {
        this(CrescoCPMSLogger.class, pluginBuilder, null, null, null, null, level);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder) {
        this(clazz, pluginBuilder, null, null, null, null, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, CLogger.Level level) {
        this(clazz, pluginBuilder, null, null, null, null, level);
    }

    // Pipeline Tagged Logger

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, String pipelineID) {
        this(CrescoCPMSLogger.class, pluginBuilder, pipelineID, null, null, null, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, String pipelineID, CLogger.Level level) {
        this(CrescoCPMSLogger.class, pluginBuilder, pipelineID, null, null, null, level);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, String pipelineID) {
        this(clazz, pluginBuilder, pipelineID, null, null, null, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, String pipelineID, CLogger.Level level) {
        this(clazz, pluginBuilder, pipelineID, null, null, null, level);
    }

    // Pipeline/Job Tagged Logger

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, String pipelineID, String jobID) {
        this(CrescoCPMSLogger.class, pluginBuilder, pipelineID, jobID, null, null, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, String pipelineID, String jobID, CLogger.Level level) {
        this(CrescoCPMSLogger.class, pluginBuilder, pipelineID, jobID, null, null, level);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, String pipelineID, String jobID) {
        this(clazz, pluginBuilder, pipelineID, jobID, null, null, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, String pipelineID, String jobID, CLogger.Level level) {
        this(clazz, pluginBuilder, pipelineID, jobID, null, null, level);
    }

    // Pipeline/Job/Task Tagged Logger

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, String pipelineID, String jobID, String taskID) {
        this(CrescoCPMSLogger.class, pluginBuilder, pipelineID, jobID, taskID, null, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, String pipelineID, String jobID, String taskID, CLogger.Level level) {
        this(CrescoCPMSLogger.class, pluginBuilder, pipelineID, jobID, taskID, null, level);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, String pipelineID, String jobID, String taskID) {
        this(clazz, pluginBuilder, pipelineID, jobID, taskID, null, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, String pipelineID, String jobID, String taskID, CLogger.Level level) {
        this(clazz, pluginBuilder, pipelineID, jobID, taskID, null, level);
    }

    // Pipeline/Job/Task/Run Tagged Logger

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, String pipelineID, String jobID, String taskID, String runID) {
        this(CrescoCPMSLogger.class, pluginBuilder, pipelineID, jobID, taskID, runID, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(PluginBuilder pluginBuilder, String pipelineID, String jobID, String taskID, String runID, CLogger.Level level) {
        this(CrescoCPMSLogger.class, pluginBuilder, pipelineID, jobID, taskID, runID, level);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, String pipelineID, String jobID, String taskID, String runID) {
        this(clazz, pluginBuilder, pipelineID, jobID, taskID, runID, CLogger.Level.Info);
    }

    public CrescoCPMSLogger(Class clazz, PluginBuilder pluginBuilder, String pipelineID, String jobID, String taskID, String runID, CLogger.Level level) {
        this.logger = pluginBuilder.getLogger(clazz.getName(), level);
        setPluginBuilder(pluginBuilder);
        setPipelineID(pipelineID);
        setJobID(jobID);
        setTaskID(taskID);
        setRunID(runID);
        setLevel(level);
    }*/

    public CrescoCPMSLogger(CrescoCPMSLoggerBuilder builder) {
        this.pluginBuilder = builder.getPluginBuilder();
        this.logger = pluginBuilder.getLogger(builder.getCls().getName(), builder.getLogLevel());
        this.pipelineID = builder.getPipelineID();
        this.pipelineName = builder.getPipelineName();
        this.jobID = builder.getJobID();
        this.jobName = builder.getJobName();
        this.taskID = builder.getTaskID();
        this.taskName = builder.getTaskName();
        this.runID = builder.getRunID();
        this.runName = builder.getRunName();
        this.level = builder.getLogLevel();
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

    /*public void cpmsInfo(Map<String, String> customParams) {
        CPMSLogMessage infoMsg = genCPMSLogMessage(CPMSLogMessageType.INFO);
        infoMsg.getParams().putAll(customParams);
        sendCPMSLogMessage(infoMsg);
    }*/

    public void cpmsHeartbeat() {
        sendCPMSHeartbeat();
    }

    public void cpmsInfo(String message) {
        logger.info(message);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.INFO, message));
    }

    /*public void cpmsInfo(Map<String, String> customParams, String message) {
        logger.info(message);
        CPMSLogMessage infoMsg = genCPMSLogMessage(CPMSLogMessageType.INFO, message);
        infoMsg.getParams().putAll(customParams);
        sendCPMSLogMessage(infoMsg);
    }*/

    public void cpmsInfo(String message, Object... objects) {
        logger.info(message, objects);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.INFO, replaceBrackets(message, objects)));
    }

    /*public void cpmsInfo(Map<String, String> customParams, String message, Object... objects) {
        logger.info(message, objects);
        CPMSLogMessage infoMsg = genCPMSLogMessage(CPMSLogMessageType.INFO, replaceBrackets(message, objects));
        infoMsg.getParams().putAll(customParams);
        sendCPMSLogMessage(infoMsg);
    }*/

    /*public void cpmsError(Map<String, String> customParams) {
        CPMSLogMessage errMsg = genCPMSLogMessage(CPMSLogMessageType.ERROR);
        errMsg.getParams().putAll(customParams);
        sendCPMSLogMessage(errMsg);
    }*/

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

    /*public void cpmsError(Map<String, String> customParams, String message) {
        logger.error(message);
        CPMSLogMessage errMsg = genCPMSLogMessage(CPMSLogMessageType.ERROR, message);
        errMsg.getParams().putAll(customParams);
        sendCPMSLogMessage(errMsg);
    }*/

    public void cpmsError(String message, Object... objects) {
        logger.error(message, objects);
        sendCPMSLogMessage(genCPMSLogMessage(CPMSLogMessageType.ERROR, replaceBrackets(message, objects)));
    }

    /*public void cpmsError(Map<String, String> customParams, String message, Object... objects) {
        logger.error(message, objects);
        CPMSLogMessage errMsg = genCPMSLogMessage(CPMSLogMessageType.ERROR, replaceBrackets(message, objects));
        errMsg.getParams().putAll(customParams);
        sendCPMSLogMessage(errMsg);
    }*/

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
        Gson gson = new Gson();
        CPMSHeartbeat heartbeat = new CPMSHeartbeatBuilder()
                .withRegion(pluginBuilder.getRegion())
                .withAgent(pluginBuilder.getAgent())
                .withPlugin(pluginBuilder.getPluginID())
                .withRunID(getRunID())
                .withPipelineID(getPipelineID())
                .withJobID(getJobID())
                .withTaskID(getTaskID())
                .build();
        try {
            TextMessage cpmsLogDataPlaneMessage = pluginBuilder.getAgentService().getDataPlaneService()
                    .createTextMessage();
            cpmsLogDataPlaneMessage.setText(gson.toJson(heartbeat));
            cpmsLogDataPlaneMessage.setStringProperty(CrescoCPMSStatics.CPMS_LOGGING_DATA_PLANE_KEY,
                    CrescoCPMSStatics.CPMS_HEARTBEAT_MESSAGES_DATA_PLANE_VALUE);
            pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, cpmsLogDataPlaneMessage);
        } catch (JMSException e) {
            logger.error("Failed to generate heartbeat message: {}, code: {}", e.getMessage(), e.getErrorCode());
            logger.error("JMSException:\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    private void sendCPMSLogMessage(CPMSLogMessage logMessage) {
        Gson gson = new Gson();
        try {
            TextMessage cpmsLogDataPlaneMessage = pluginBuilder.getAgentService().getDataPlaneService()
                    .createTextMessage();
            cpmsLogDataPlaneMessage.setText(gson.toJson(logMessage));
            cpmsLogDataPlaneMessage.setStringProperty(CrescoCPMSStatics.CPMS_LOGGING_DATA_PLANE_KEY,
                    CrescoCPMSStatics.CPMS_LOG_MESSAGES_DATA_PLANE_VALUE);
            pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, cpmsLogDataPlaneMessage);
        } catch (JMSException e) {
            logger.error("Failed to generate log message: {}, code: {}", e.getMessage(), e.getErrorCode());
            logger.error("JMSException:\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    private void sendCPMSTaskOutput(ScriptedTask scriptedTask, String output) {
        Gson gson = new Gson();
        CPMSTaskOutput taskOutput = new CPMSTaskOutput(scriptedTask.toJson(), output);
        try {
            TextMessage cpmsLogDataPlaneMessage = pluginBuilder.getAgentService().getDataPlaneService()
                    .createTextMessage();
            cpmsLogDataPlaneMessage.setText(gson.toJson(taskOutput));
            cpmsLogDataPlaneMessage.setStringProperty(CrescoCPMSStatics.CPMS_LOGGING_DATA_PLANE_KEY,
                    CrescoCPMSStatics.CPMS_TASK_OUTPUT_DATA_PLANE_VALUE);
            pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, cpmsLogDataPlaneMessage);
        } catch (JMSException e) {
            logger.error("Failed to generate task output message: {}, code: {}", e.getMessage(), e.getErrorCode());
            logger.error("JMSException:\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    /*private MsgEvent genGPMSMessage(MsgEvent.Type type) {
        return genGPMSMessage(type, "");
    }*/

    /*private MsgEvent genGPMSMessage(MsgEvent.Type type, String msgBody) {
        MsgEvent me = null;
        try {
            me = pluginBuilder.getGlobalPluginMsgEvent(
                    CrescoCPMSStatics.CONTROLLER_LOGGER_MESSAGE_TYPE,
                    pluginBuilder.getConfig().getStringParam(CrescoCPMSStatics.GENOMICS_CONTROLLER_REGION_PARAM_NAME, pluginBuilder.getRegion()),
                    pluginBuilder.getConfig().getStringParam(CrescoCPMSStatics.GENOMICS_CONTROLLER_AGENT_PARAM_NAME, pluginBuilder.getAgent()),
                    pluginBuilder.getConfig().getStringParam(CrescoCPMSStatics.GENOMICS_CONTROLLER_PLUGIN_PARAM_NAME, pluginBuilder.getPluginID())
            );
            me.setParam(CrescoCPMSStatics.GPMS_MESSAGE_TYPE_PARAM_NAME, type.name());
            me.setParam(CrescoCPMSStatics.PATHSTAGE_PARAM_NAME, Integer.toString(getStage()));
            if (getFlowcellID() != null)
                me.setParam(CrescoCPMSStatics.FLOW_CELL_ID_PARAM_NAME, getFlowcellID());
            if (getSampleID() != null)
                me.setParam(CrescoCPMSStatics.SAMPLE_ID_PARAM_NAME, getSampleID());
            if (getRequestID() != null)
                me.setParam(CrescoCPMSStatics.REQUEST_ID_PARAM_NAME, getRequestID());
            if (getSampleID() != null)
                me.setParam(CrescoCPMSStatics.SAMPLE_STEP_PARAM_NAME, Integer.toString(getStep()));
            else if (getFlowcellID() != null)
                me.setParam(CrescoCPMSStatics.FLOW_CELL_STEP_PARAM_NAME, Integer.toString(getStep()));
            else
                me.setParam(CrescoCPMSStatics.PLUGIN_STEP_PARAM_NAME, Integer.toString(getStep()));
            if (type == MsgEvent.Type.ERROR)
                me.setParam(CrescoCPMSStatics.ERROR_MESSAGE_PARAM_NAME, msgBody);
            else
                me.setParam(CrescoCPMSStatics.UPDATE_MESSAGE_PARAM_NAME, msgBody);
        } catch(Exception ex) {
            logger.error(ex.getMessage());
        }
        return me;
    }*/

    /*private void setFailed(MsgEvent failMsg, String message) {
        failMsg.getParams().put(CrescoCPMSStatics.FAILURE_MESSAGE_PARAM_NAME, message);
    }*/

    private String replaceBrackets(String logMessage, Object... params) {
        int replaced = 0;
        while (logMessage.contains("{}") && replaced < params.length) {
            logMessage = logMessage.replaceFirst("\\{}", String.valueOf(params[replaced]).replace("\\", "\\\\"));
            replaced++;
        }
        return logMessage;
    }
}
