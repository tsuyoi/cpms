package io.cresco.cpms.logging;

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

    void cpmsInfo(Map<String, String> customParams);
    void cpmsInfo(String message);
    void cpmsInfo(Map<String, String> customParams, String message);
    void cpmsInfo(String message, Object... objects);
    void cpmsInfo(Map<String, String> customParams, String message, Object... objects);

    void cpmsError(Map<String, String> customParams);
    void cpmsError(String message);
    void cpmsError(Map<String, String> customParams, String message);
    void cpmsError(String message, Object... objects);
    void cpmsError(Map<String, String> customParams, String message, Object... objects);

    void cpmsFailure(String message);
    void cpmsFailure(String message, Object... objects);

    CPMSLogger cloneLogger(Class clazz);

    String getPipelineRunId();
    void setPipelineRunId(String pipelineRunId);

    String getTaskRunId();
    void setTaskRunId(String taskRunId);

    String getTaskCommand();
    void setTaskCommand(String taskCommand);
}
