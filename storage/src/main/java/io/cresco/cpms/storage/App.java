package io.cresco.cpms.storage;

import ch.qos.logback.classic.Level;
import com.google.gson.Gson;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.processing.StorageEngine;
import io.cresco.cpms.scripting.ScriptException;
import io.cresco.cpms.scripting.StorageTask;
import io.cresco.cpms.storage.encapsulation.Archiver;
import io.cresco.cpms.storage.transfer.ObjectStorageV2;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    private static final CPMSLogger logger = new BasicCPMSLoggerBuilder().withClass(App.class).build();
    private static final Logger simpleLogger = LoggerFactory.getLogger("message-only");

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Cresco Pipeline Management System - Storage").build()
                .defaultHelp(true)
                .description("Interacts with storage");
        parser.addArgument("-v", "--verbose").action(Arguments.storeTrue());
        parser.addArgument("-C", "--credentials")
                .setDefault("config.properties")
                .help("Configuration file holding credential information.");
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
                credentials = ns.getString("credentials")
                        ;
        List<String> parameters = ns.getList("parameters");

        if (verbose) {
            final ch.qos.logback.classic.Logger meLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("MSGEVENT");
            meLogger.setLevel(Level.toLevel("TRACE"));
            final ch.qos.logback.classic.Logger appLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(App.class);
            appLogger.setLevel(Level.toLevel("TRACE"));
            final ch.qos.logback.classic.Logger osLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ObjectStorageV2.class);
            osLogger.setLevel(Level.toLevel("TRACE"));
            final ch.qos.logback.classic.Logger arcLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Archiver.class);
            arcLogger.setLevel(Level.toLevel("TRACE"));
            final ch.qos.logback.classic.Logger seLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(StorageEngine.class);
            seLogger.setLevel(Level.toLevel("TRACE"));
        }

        logger.trace("Checking for a command");
        if (command == null) {
            simpleLogger.info("Commands:");
            simpleLogger.info("\t- list <path>");
            simpleLogger.info("\t- upload <local_path_or_file> <remote_path>");
            simpleLogger.info("\t- download <remote_path_or_object> <local_path>");
            simpleLogger.info("\t- copy <source_path_or_object> <destination_path>");
            return;
        }
        logger.debug("Command: {}", command);
        logger.debug("Parameters: {}", parameters);
        switch (command) {
            case "list":
                if (parameters.size() != 1) {
                    simpleLogger.info("list usage: <path>");
                    return;
                }
                break;
            case "upload":
                if (parameters.size() < 2 || parameters.size() > 3) {
                    simpleLogger.info("upload usage: <local_path_or_file> <remote_path>");
                    return;
                }
                break;
            case "download":
                if (parameters.size() != 3) {
                    simpleLogger.info("download usage: <remote_path_or_object> <local_path>");
                    return;
                }
                break;
        }


        logger.trace("Checking for config.properties file");
        File configFile = new File(credentials);
        if (configFile.exists()) {
            Configurations configs = new Configurations();
            try {
                Configuration config = configs.properties(configFile);
                if (config.containsKey("log.level") && !verbose) {
                    final ch.qos.logback.classic.Logger meLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("MSGEVENT");
                    meLogger.setLevel(Level.toLevel(config.getString("log.level")));
                    final ch.qos.logback.classic.Logger appLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(App.class);
                    appLogger.setLevel(Level.toLevel(config.getString("log.level")));
                    final ch.qos.logback.classic.Logger osLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ObjectStorageV2.class);
                    osLogger.setLevel(Level.toLevel(config.getString("log.level")));
                    final ch.qos.logback.classic.Logger arcLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Archiver.class);
                    arcLogger.setLevel(Level.toLevel(config.getString("log.level")));
                    final ch.qos.logback.classic.Logger seLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(StorageEngine.class);
                    seLogger.setLevel(Level.toLevel(config.getString("log.level")));
                }
            } catch (ConfigurationException e) {
                logger.error("Error with your config.properties file.");
            }
        }
        logger.trace("Building StorageEngine instance");
        StorageEngine storageEngine;
        try {
            storageEngine = new StorageEngine(logger);
        } catch (IllegalArgumentException e) {
            logger.error("Error building storage engine : {}", e.getMessage());
            return;
        }
        logger.trace("Building storage task");
        Gson gson = new Gson();
        Map<String, String> storageTaskJSON = new HashMap<>();
        storageTaskJSON.put("id", "cli-task-id");
        storageTaskJSON.put("name", "cli-task-name");
        storageTaskJSON.put("type", "storage");
        storageTaskJSON.put("action", command);
        StorageTask storageTask;
        logger.trace("JSON: {}", storageTaskJSON);
        boolean success = false;
        try {
            switch (command) {
                case "list":
                    storageTaskJSON.put("remote_path", parameters.get(0));
                    success = storageEngine.runStorageJob(new StorageTask(storageTaskJSON));
                    break;
                case "upload":
                    storageTaskJSON.put("local_path", parameters.get(0));
                    storageTaskJSON.put("remote_path", parameters.get(1));
                    Path localFile = Paths.get(parameters.get(1));
                    if (parameters.size() == 3)
                        storageTaskJSON.put("s3_path", String.format("%s/%s", parameters.get(2), localFile.getFileName()));
                    else
                        storageTaskJSON.put("s3_path", localFile.getFileName().toString());
                    success = storageEngine.runStorageJob(new StorageTask(storageTaskJSON));
                    break;
                case "download":
                    storageTaskJSON.put("s3_bucket", parameters.get(0));
                    storageTaskJSON.put("s3_path", parameters.get(1));
                    storageTaskJSON.put("local_path", parameters.get(2));
                    String storageTaskJSONStr = gson.toJson(storageTaskJSON);
                    System.out.println(storageTaskJSONStr);
                    storageTask = new StorageTask(storageTaskJSONStr);
                    System.out.println(storageTask);
                    success = storageEngine.runStorageJob(storageTask);
                    break;
                default:
                    break;
            }
            if (success)
                simpleLogger.info("\nSuccessfully completed storage job");
            else
                simpleLogger.error("\nFailed to complete storage job. Please rerun using -v for more information.");
        } catch (ScriptException e) {
            System.err.println(e.getMessage());
        }
    }
}
