package io.cresco.cpms.controller.listeners;

import com.google.gson.Gson;
import io.cresco.cpms.statics.CrescoCPMSStatics;
import io.cresco.cpms.telemetry.CPMSHeartbeat;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

public class HeartbeatListener {
    private final Gson gson = new Gson();
    private final PluginBuilder pluginBuilder;
    private final CLogger logger;

    private String listenerId = null;

    public HeartbeatListener(PluginBuilder pb) {
        pluginBuilder = pb;
        logger = pluginBuilder.getLogger(HeartbeatListener.class.getName(), CLogger.Level.Trace);
    }

    public void start() {
        try {
            if (listenerId == null) {
                MessageListener listener = (Message msg) -> {
                    try {
                        TextMessage textMessage = (TextMessage) msg;
                        CPMSHeartbeat heartbeat = gson.fromJson(textMessage.getText(), CPMSHeartbeat.class);
                        handleHeartbeat(heartbeat);
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
                                CrescoCPMSStatics.HEARTBEAT_MESSAGES_DATA_PLANE_VALUE
                        )
                );
            }
            logger.info("Listening for heartbeat messages");
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

    private void handleHeartbeat(CPMSHeartbeat heartbeat) {
        if (heartbeat == null) {
            logger.error("Failed to unmarshall heartbeat, please check generator");
        }
        logger.info("Received: {}", heartbeat);
    }
}
