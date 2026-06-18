package io.cresco.cpms.storage;

import ch.qos.logback.classic.Level;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.processing.ExecutionException;
import io.cresco.cpms.processing.StorageEngine;
import io.cresco.cpms.processing.StorageTaskResult;
import io.cresco.cpms.scripting.ScriptException;
import io.cresco.cpms.scripting.StorageTask;
import io.cresco.cpms.statics.ArchiveCompression;
import io.cresco.cpms.statics.BagItType;
import io.cresco.cpms.storage.encapsulation.Archiver;
import io.cresco.cpms.storage.encapsulation.ArchiverBuilder;
import io.cresco.cpms.storage.transfer.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.impl.type.ReflectArgumentType;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    private static final CPMSLogger logger = new BasicCPMSLoggerBuilder().withClass(App.class).build();
    private static final Logger simpleLogger = LoggerFactory.getLogger("message-only");

    static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Cresco Pipeline Management System - Storage").build()
                .defaultHelp(true)
                .description("Command line storage management utility");
        parser.addArgument("-v", "--verbose").action(Arguments.storeTrue());
        parser.addArgument("-a", "--archive").type(new ReflectArgumentType<>(BagItType.class))
                .choices(BagItType.values()).setDefault(BagItType.None);
        parser.addArgument("-c", "--compress").type(new ReflectArgumentType<>(ArchiveCompression.class))
                .choices(ArchiveCompression.values()).setDefault(ArchiveCompression.NONE);
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
        BagItType bagItType = ns.get("archive");
        ArchiveCompression archiveCompression = ns.get("compress");
        String command = ns.getString("command");
        List<String> parameters = ns.getList("parameters");

        final ch.qos.logback.classic.Logger meLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("MSGEVENT");
        meLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger appLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(App.class);
        appLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger seLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(StorageEngine.class);
        seLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger arcLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Archiver.class);
        arcLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger arcbLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ArchiverBuilder.class);
        arcbLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger azbbLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AzureBlobStorage.class);
        azbbLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger azbLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AzureBlobStorageBuilder.class);
        azbLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger osLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(S3ObjectStorage.class);
        osLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger osbLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(S3ObjectStorageBuilder.class);
        osbLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger fsLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FileSystemStorage.class);
        fsLogger.setLevel(Level.toLevel("INFO"));
        final ch.qos.logback.classic.Logger fsbLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FileSystemStorageBuilder.class);
        fsbLogger.setLevel(Level.toLevel("INFO"));

        if (verbose) {
            meLogger.setLevel(Level.toLevel("TRACE"));
            appLogger.setLevel(Level.toLevel("TRACE"));
            seLogger.setLevel(Level.toLevel("TRACE"));
            arcLogger.setLevel(Level.toLevel("TRACE"));
            arcbLogger.setLevel(Level.toLevel("TRACE"));
            azbbLogger.setLevel(Level.toLevel("TRACE"));
            azbLogger.setLevel(Level.toLevel("TRACE"));
            osLogger.setLevel(Level.toLevel("TRACE"));
            osbLogger.setLevel(Level.toLevel("TRACE"));
            fsLogger.setLevel(Level.toLevel("TRACE"));
            fsbLogger.setLevel(Level.toLevel("TRACE"));
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
                if (parameters.size() < 2 || parameters.size() > 3) {
                    simpleLogger.info("download usage: <remote_path_or_object> <local_path>");
                    return;
                }
                break;
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
        Map<String, String> storageTaskJSON = new HashMap<>();
        storageTaskJSON.put("id", "cli-task-id");
        storageTaskJSON.put("name", "cli-task-name");
        storageTaskJSON.put("type", "storage");
        storageTaskJSON.put("action", command);
        logger.trace("JSON: {}", storageTaskJSON);
        try {
            switch (command) {
                case "list":
                    storageTaskJSON.put("source_path", parameters.getFirst());
                    break;
                case "upload":
                    storageTaskJSON.put("source_path", parameters.get(0));
                    storageTaskJSON.put("destination_path", parameters.get(1));
                    storageTaskJSON.put("destination_archiving", bagItType.name());
                    storageTaskJSON.put("destination_compression", archiveCompression.name());
                    break;
                case "download":
                case "copy":
                    storageTaskJSON.put("source_path", parameters.get(0));
                    storageTaskJSON.put("destination_path", parameters.get(1));
                    break;
                default:
                    break;
            }
            StorageTaskResult storageTaskResult = storageEngine.runStorageJob(new StorageTask(storageTaskJSON));
            if (storageTaskResult.getSuccess())
                simpleLogger.info("\nSuccessfully completed storage job");
            else
                simpleLogger.error(String.format("\nFailed to complete storage job.%s", (verbose) ? "" : " Please rerun using -v for more information."));
        } catch (ExecutionException | ScriptException e) {
            simpleLogger.error(e.getMessage());
        }
    }
}
