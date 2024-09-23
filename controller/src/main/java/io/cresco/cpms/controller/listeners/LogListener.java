package io.cresco.cpms.controller.listeners;

import com.google.gson.Gson;
import io.cresco.cpms.statics.CrescoCPMSStatics;
import io.cresco.cpms.logging.CPMSLogMessage;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

public class LogListener {
    private final Gson gson = new Gson();
    private final PluginBuilder pluginBuilder;
    private final CLogger logger;

    private String listenerId = null;

    public LogListener(PluginBuilder pb) {
        pluginBuilder = pb;
        logger = pluginBuilder.getLogger(LogListener.class.getName(), CLogger.Level.Trace);
    }

    public void start() {
        try {
            if (listenerId == null) {
                MessageListener listener = (Message msg) -> {
                    try {
                        TextMessage textMessage = (TextMessage) msg;
                        CPMSLogMessage log = gson.fromJson(textMessage.getText(), CPMSLogMessage.class);
                        handleLog(log);
                    } catch (JMSException e) {
                        logger.error("Failed to get JMS message from data plane: {}", e.getMessage());
                    }
                };
                listenerId = pluginBuilder.getAgentService().getDataPlaneService().addMessageListener(
                        TopicType.AGENT,
                        listener,
                        String.format(
                                "%s='%s'",
                                CrescoCPMSStatics.LOGGING_DATA_PLANE_KEY,
                                CrescoCPMSStatics.LOG_MESSAGES_DATA_PLANE_VALUE
                        )
                );
            }
            logger.info("Listening for log messages");
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage());
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (listenerId != null) {
            pluginBuilder.getAgentService().getDataPlaneService().removeMessageListener(listenerId);
            listenerId = null;
        }
    }

    private void handleLog(CPMSLogMessage log) {
        if (log == null) {
            logger.error("Failed to unmarshall log, please check generator");
        }
        logger.info("Received: {}", log);
    }
}