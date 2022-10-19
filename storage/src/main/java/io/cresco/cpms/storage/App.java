package io.cresco.cpms.storage;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.gson.Gson;
import io.cresco.cpms.processing.StorageEngine;
import io.cresco.cpms.scripting.ScriptException;
import io.cresco.cpms.scripting.StorageTask;
import io.cresco.cpms.storage.encapsulation.Archiver;
import io.cresco.cpms.storage.transfer.ObjectStorage;
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
import software.amazon.ion.NullValueException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final Logger simpleLogger = LoggerFactory.getLogger("message-only");

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("ORIEN Pipeline Management System - Data Manipulator").build()
                .defaultHelp(true)
                .description("Downloads and restores genomics pipeline data to the local filesystem.");
        parser.addArgument("-v", "--verbose").action(Arguments.storeTrue());
        parser.addArgument("-C", "--credentials")
                .setDefault("config.properties")
                .help("Configuration file holding credential information.");
        parser.addArgument("-A", "--accesskey");
        parser.addArgument("-S", "--secretkey");
        parser.addArgument("-E", "--endpoint");
        parser.addArgument("-R", "--region");
        parser.addArgument("name").nargs("?");
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
        if (verbose) {
            LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger tmpLogger = loggerContext.getLogger("root");
            tmpLogger.setLevel(Level.toLevel("TRACE"));
        }
        String name = ns.getString("name"), command = ns.getString("command"),
                credentials = ns.getString("credentials"),
                accessKey = ns.getString("accesskey"), secretKey = ns.getString("secretkey"),
                endpoint = ns.getString("endpoint"), region = ns.getString("region")
                        ;
        List<String> parameters = ns.getList("parameters");

        logger.trace("Checking for a command");
        if (command == null) {
            simpleLogger.info("Commands:");
            simpleLogger.info("\t- upload <local_path_or_file> <bucket> [object_prefix]");
            simpleLogger.info("\t- download <bucket> <object_prefix_or_key> <local_path>");
            simpleLogger.info("\t- delete <bucket> <object_prefix_or_key>");
            return;
        }

        logger.trace("Checking for config.properties file");
        File configFile = new File(credentials);
        if (configFile.exists()) {
            Configurations configs = new Configurations();
            try {
                Configuration config = configs.properties(configFile);
                if (config.containsKey("log.level")) {
                    LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
                    ch.qos.logback.classic.Logger tmpLogger = loggerContext.getLogger("root");
                    tmpLogger.setLevel(Level.toLevel(config.getString("log.level")));
                }
                if (config.containsKey("access.key")) {
                    logger.trace("Config properties: " + config.getString("access.key"));
                    accessKey = config.getString("access.key");
                }
                if (config.containsKey("secret.key")) {
                    secretKey = config.getString("secret.key");
                }
                if (config.containsKey("endpoint")) {
                    endpoint = config.getString("endpoint");
                }
                if (config.containsKey("region")) {
                    region = config.getString("region");
                }
            } catch (ConfigurationException e) {
                logger.error("Error with your config.properties file.");
            }
        }
        ObjectStorage objectStorage;
        try {
            objectStorage = new ObjectStorage(accessKey, secretKey, endpoint, region);
        } catch (NullValueException | IllegalArgumentException e) {
            logger.error("Error building objectStorage : {}", e.getMessage());
            return;
        }
        Archiver archiver;
        try {
            archiver = new Archiver();
        } catch (NullValueException | IllegalArgumentException e) {
            logger.error("Error building archiver : {}", e.getMessage());
            return;
        }
        StorageEngine storageEngine;
        try {
            storageEngine = new StorageEngine(objectStorage, archiver);
        } catch (NullValueException | IllegalArgumentException e) {
            logger.error("Error building storage engine : {}", e.getMessage());
            return;
        }
        Gson gson = new Gson();
        Map<String, String> storageTaskJSON = new HashMap<>();
        storageTaskJSON.put("name", name);
        storageTaskJSON.put("type", "storage");
        storageTaskJSON.put("s3_access_key", accessKey);
        storageTaskJSON.put("s3_secret_key", secretKey);
        storageTaskJSON.put("s3_endpoint", endpoint);
        storageTaskJSON.put("s3_region", region);
        storageTaskJSON.put("action", command);
        StorageTask storageTask = null;
        logger.trace("JSON: {}", storageTaskJSON);
        logger.trace("Parameters: {}", parameters);
        try {
            switch (command) {
                case "upload":
                    break;
                case "download":
                    if (parameters.size() != 3) {
                        simpleLogger.info("download usage: <bucket> <object_prefix_or_key> <local_path>");
                        return;
                    }
                    storageTaskJSON.put("s3_bucket", parameters.get(0));
                    storageTaskJSON.put("s3_path", parameters.get(1));
                    storageTaskJSON.put("local_path", parameters.get(2));
                    String storageTaskJSONStr = gson.toJson(storageTaskJSON);
                    System.out.println(storageTaskJSONStr);
                    storageTask = new StorageTask(storageTaskJSONStr);
                    System.out.println(storageTask);
                    break;
                case "delete":
                    break;
                default:
                    break;
            }
            /*if (storageEngine.runStorageJob(storageTask))
                simpleLogger.info("Successfully completed storage job");
            else
                simpleLogger.error("Failed to complete storage job. Please rerun using -v for more information.");*/
        } catch (ScriptException e) {
            System.err.println(e.getMessage());
        }
    }
}
