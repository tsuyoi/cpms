package io.cresco.cpms.database;

import ch.qos.logback.classic.Level;
import io.cresco.cpms.database.services.PipelineService;
import io.cresco.cpms.database.utilities.BaseSessionFactoryManager;
import io.cresco.cpms.database.utilities.BasicSessionFactoryManager;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationMap;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class App {
    private static final CPMSLogger logger = new BasicCPMSLoggerBuilder().withClass(App.class).build();
    private static final Logger simpleLogger = LoggerFactory.getLogger("message-only");

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Cresco Pipeline Management System - Database").build()
                .defaultHelp(true)
                .description("Interacts with CPMS database elements");
        parser.addArgument("-v", "--verbose").action(Arguments.storeTrue());
        parser.addArgument("-C", "--config")
                .setDefault("config.properties")
                .help("Configuration file holding database settings");
        parser.addArgument("command").nargs("?");
        parser.addArgument("parameters").nargs("*");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        Boolean verbose = ns.getBoolean("verbose");
        String command = ns.getString("command"),
                config = ns.getString("config")
                        ;
        List<String> parameters = ns.getList("parameters");

        if (verbose) {
            final ch.qos.logback.classic.Logger meLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("MSGEVENT");
            meLogger.setLevel(Level.toLevel("TRACE"));
            final ch.qos.logback.classic.Logger appLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(App.class);
            appLogger.setLevel(Level.toLevel("TRACE"));
            final ch.qos.logback.classic.Logger sessLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseSessionFactoryManager.class);
            sessLogger.setLevel(Level.toLevel("TRACE"));
            final ch.qos.logback.classic.Logger bsessLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BasicSessionFactoryManager.class);
            bsessLogger.setLevel(Level.toLevel("TRACE"));
        }

        logger.trace("Checking for a command");
        if (command == null) {
            simpleLogger.info("Commands:");
            simpleLogger.info("\t- connect");
            return;
        }
        logger.debug("Command: {}", command);
        logger.debug("Parameters: {}", parameters);
        switch (command) {
            case "list":
                if (!parameters.isEmpty()) {
                    simpleLogger.info("list usage:");
                    return;
                }
                break;
        }

        logger.trace("Checking for config.properties file");
        File configFile = new File(config);
        Configuration configFromFile;
        if (configFile.exists()) {
            Configurations configs = new Configurations();
            try {
                configFromFile = configs.properties(configFile);
                if (configFromFile.containsKey("log.level") && !verbose) {
                    final ch.qos.logback.classic.Logger meLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("MSGEVENT");
                    meLogger.setLevel(Level.toLevel(configFromFile.getString("log.level")));
                    final ch.qos.logback.classic.Logger appLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(App.class);
                    appLogger.setLevel(Level.toLevel(configFromFile.getString("log.level")));
                    final ch.qos.logback.classic.Logger sessLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseSessionFactoryManager.class);
                    sessLogger.setLevel(Level.toLevel(configFromFile.getString("log.level")));
                    final ch.qos.logback.classic.Logger bsessLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BasicSessionFactoryManager.class);
                    bsessLogger.setLevel(Level.toLevel(configFromFile.getString("log.level")));
                }
            } catch (ConfigurationException e) {
                logger.error("Error with your config.properties file.");
                return;
            }
        } else {
            logger.error("Config file [{}] does not exist. Exiting...", config);
            return;
        }

        Map<String, Object> dbConfig = new HashMap<>();
        Iterator<String> configKeyIterator = configFromFile.getKeys();
        while (configKeyIterator.hasNext()) {
            String key = configKeyIterator.next();
            dbConfig.put(key, configFromFile.getProperty(key));
        }

        logger.trace("Building BasicSessionFactoryManager");
        BasicSessionFactoryManager sessionFactoryManager;
        try {
            logger.debug("Configs: {}", dbConfig);
            sessionFactoryManager = new BasicSessionFactoryManager(dbConfig);
            PipelineService.setSessionFactoryManager(sessionFactoryManager);
        } catch (Exception e) {
            logger.error("Error building session factory manager: {}", e.getMessage());
        }
        try {
            logger.info("Pipeline: {}", PipelineService.create("Test", "{\"name\":\"Test\"}"));
        } catch (CPMSDatabaseException e) {
            logger.error("Database Exception: {}", e.getMessage());
        }
    }
}
