package io.cresco.cpms.cresco;

import io.cresco.library.messaging.MsgEvent;

public class CrescoCPMSStatics {
    // Logging
    public static final String CPMS_LOGGING_DATA_PLANE_KEY = "cpmsLogging";
    public static final String CPMS_LOG_MESSAGES_DATA_PLANE_VALUE = "cpmsLogMessages";
    public static final String CPMS_HEARTBEAT_MESSAGES_DATA_PLANE_VALUE = "cpmsHeartbeatMessages";
    public static final String CPMS_TASK_OUTPUT_DATA_PLANE_VALUE = "cpmsTaskOutputMessages";

    // Miscellaneous
    public static final String DEFAULT_INSTANCE_ID_URL = "http://169.254.169.254/latest/meta-data/instance-id";
}
