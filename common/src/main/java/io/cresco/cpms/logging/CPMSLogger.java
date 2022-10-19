package io.cresco.cpms.logging;

import io.cresco.cpms.scripting.ScriptedTask;

import java.util.Map;

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

    //void cpmsInfo(Map<String, String> customParams);
    void cpmsInfo(String message);
    //void cpmsInfo(Map<String, String> customParams, String message);
    void cpmsInfo(String message, Object... objects);
    //void cpmsInfo(Map<String, String> customParams, String message, Object... objects);

    void cpmsWarn(String message);
    void cpmsWarn(String message, Object... objects);

    //void cpmsError(Map<String, String> customParams);
    void cpmsError(String message);
    //void cpmsError(Map<String, String> customParams, String message);
    void cpmsError(String message, Object... objects);
    //void cpmsError(Map<String, String> customParams, String message, Object... objects);

    void cpmsFailure(String message);
    void cpmsFailure(String message, Object... objects);

    void cpmsTaskOutput(ScriptedTask scriptedTask, String output);

    CPMSLogger cloneLogger(Class clazz);

    public String getPipelineID();
    public void setPipelineID(String pipelineID);

    public String getPipelineName();
    public void setPipelineName(String pipelineName);

    public String getJobID();
    public void setJobID(String jobID);

    public String getJobName();
    public void setJobName(String jobName);

    public String getRunID();
    public void setRunID(String runID);

    public String getRunName();
    public void setRunName(String runName);

    public String getTaskID();
    public void setTaskID(String taskID);

    public String getTaskName();
    public void setTaskName(String taskName);
}
