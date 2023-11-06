package io.cresco.cpms.logging;

import io.cresco.cpms.scripting.ScriptedTask;

@SuppressWarnings({"unused"})
public interface CPMSLogger {
    void trace(String message);
    void trace(String message, Object... objects);

    void debug(String message);
    void debug(String message, Object... objects);

    void info(String message);
    void info(String message, Object... objects);

    void warn(String message);
    void warn(String message, Object... objects);

    void error(String message);
    void error(String message, Object... objects);

    void cpmsInfo(String message);
    void cpmsInfo(String message, Object... objects);

    void cpmsWarn(String message);
    void cpmsWarn(String message, Object... objects);

    void cpmsError(String message);
    void cpmsError(String message, Object... objects);

    void cpmsFailure(String message);
    void cpmsFailure(String message, Object... objects);

    void cpmsTaskOutput(ScriptedTask scriptedTask, String output);

    CPMSLogger cloneLogger(Class clazz);

    String getPipelineID();
    void setPipelineID(String pipelineID);

    String getPipelineName();
    void setPipelineName(String pipelineName);

    String getJobID();
    void setJobID(String jobID);

    String getJobName();
    void setJobName(String jobName);

    String getRunID();
    void setRunID(String runID);

    String getRunName();
    void setRunName(String runName);

    String getTaskID();
    void setTaskID(String taskID);

    String getTaskName();
    void setTaskName(String taskName);
}
