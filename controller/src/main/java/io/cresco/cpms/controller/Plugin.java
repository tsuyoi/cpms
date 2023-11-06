package io.cresco.cpms.controller;

import io.cresco.cpms.database.services.PipelineService;
import io.cresco.cpms.database.utilities.CrescoSessionFactoryManager;
import io.cresco.cpms.logging.CrescoCPMSLogger;
import io.cresco.cpms.logging.CrescoCPMSLoggerBuilder;
import io.cresco.cpms.controller.listeners.HeartbeatListener;
import io.cresco.cpms.controller.listeners.LogListener;
import io.cresco.cpms.controller.listeners.TaskOutputListener;
import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import io.cresco.library.utilities.CLogger;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import java.util.Map;

@SuppressWarnings({"unused"})
@Component(
        service = { PluginService.class },
        scope= ServiceScope.PROTOTYPE,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        servicefactory = true,
        reference=@Reference(name="io.cresco.library.agent.AgentService", service= AgentService.class)
)
public class Plugin implements PluginService {
    // Cresco Elements
    public BundleContext context;
    private Map<String,Object> map;
    public static PluginBuilder pluginBuilder;
    private CLogger logger;

    // CPMS Controller Elements
    private CrescoCPMSLogger cpmsLogger;
    private HeartbeatListener heartbeatListener;
    private LogListener logListener;
    private TaskOutputListener taskOutputListener;
    private CrescoSessionFactoryManager sessionFactoryManager;

    @Override
    public boolean isStarted() {
        try {
            if (pluginBuilder == null) {
                pluginBuilder = new PluginBuilder(this.getClass().getName(), context, map);
                logger = pluginBuilder.getLogger(Plugin.class.getName(), CLogger.Level.Trace);
            }

            while (!pluginBuilder.getAgentService().getAgentState().isActive()) {
                logger.info("Plugin " + pluginBuilder.getPluginID() + " waiting on Agent Init");
                Thread.sleep(1000);
            }
            pluginBuilder.setIsActive(true);

            // BEGIN CONTROLLER PLUGIN STARTUP

            cpmsLogger = new CrescoCPMSLoggerBuilder()
                    .withPluginBuilder(pluginBuilder)
                    .withClass(Plugin.class)
                    .withPipelineID("FakePipeline")
                    .build();
            cpmsLogger.info("Test Info");

            sessionFactoryManager = new CrescoSessionFactoryManager(pluginBuilder, cpmsLogger);
            PipelineService.setSessionFactoryManager(sessionFactoryManager);

            heartbeatListener = new HeartbeatListener(pluginBuilder);
            heartbeatListener.start();

            logListener = new LogListener(pluginBuilder);
            logListener.start();

            taskOutputListener = new TaskOutputListener(pluginBuilder);
            taskOutputListener.start();

            Thread.sleep(1000);
            cpmsLogger.cpmsHeartbeat();
            cpmsLogger.cpmsInfo("Test log message");
            cpmsLogger.info("Pipeline created: {}",
                    PipelineService.create("Test-Pipeline", "{\"name\":\"Test\"}"));

            // END CONTROLLER PLUGIN STARTUP

            return true;
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isStopped() {
        // BEGIN CONTROLLER PLUGIN TEARDOWN

        if (heartbeatListener != null) {
            logger.info("Stopping heartbeat listener");
            heartbeatListener.stop();
        }

        if (logListener != null) {
            logger.info("Stopping log listener");
            logListener.stop();
        }

        if (taskOutputListener != null) {
            logger.info("Stopping task output listener");
            taskOutputListener.stop();
        }

        if (sessionFactoryManager != null) {
            logger.info("Stopping Cresco session factory manager");
            sessionFactoryManager.close();
        }

        // END CONTROLLER PLUGIN TEARDOWN
        return true;
    }

    @Activate
    void activate(BundleContext context, Map<String, Object> map) {
        this.context = context;
        this.map = map;
    }
    @Deactivate
    void deactivate(BundleContext context, Map<String,Object> map) {
        this.context = null;
        this.map = null;
    }

    @Override
    public boolean isActive() {
        return pluginBuilder.isActive();
    }
    @Override
    public void setIsActive(boolean isActive) {
        pluginBuilder.setIsActive(isActive);
    }

    @Override
    public boolean inMsg(MsgEvent incoming) {
        pluginBuilder.msgIn(incoming);
        return true;
    }
}
