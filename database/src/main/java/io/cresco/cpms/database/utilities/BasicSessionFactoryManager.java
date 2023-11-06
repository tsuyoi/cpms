package io.cresco.cpms.database.utilities;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;

import java.util.Map;

public class BasicSessionFactoryManager extends BaseSessionFactoryManager {

    public BasicSessionFactoryManager(Map<String, Object> config) {
        super(config, new BasicCPMSLoggerBuilder().withClass(BasicSessionFactoryManager.class).build());
    }
}
