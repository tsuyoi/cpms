package io.cresco.cpms.database.utilities;

import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.library.plugin.PluginBuilder;

@SuppressWarnings("unused")
public class CrescoSessionFactoryManager extends BaseSessionFactoryManager {

    public CrescoSessionFactoryManager(PluginBuilder pluginBuilder, CPMSLogger logger) {
        super(pluginBuilder.getConfig().getConfigMap(), logger);
    }
}
