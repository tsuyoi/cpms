package io.cresco.cpms.processing;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.scripting.ScriptException;
import io.cresco.cpms.scripting.ScriptedJob;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    //private static final Logger logger = LoggerFactory.getLogger(io.cresco.cpms.storage.App.class);
    private static final CPMSLogger logger = new BasicCPMSLoggerBuilder().withClass(App.class).withPipelineID("test_pipeline_uuid").withPipelineName("Test-Pipeline").build();

    public static void main(String[] args) throws IOException, ScriptException {
        ArgumentParser parser = ArgumentParsers.newFor("Cresco Pipeline Management System - Process Executor")
                .build()
                .defaultHelp(true)
                .description("Executes a job from an input .json file.");
        parser.addArgument("script");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        String script = ns.getString("script");
        String examplePipeline = new String(Files.readAllBytes(Paths.get(script)));
        executeJob(examplePipeline);
    }

    private static void executeJob(String job) throws ScriptException{
        ScriptedJob scriptedJob = new ScriptedJob(job);
        logger.cpmsInfo(String.valueOf(scriptedJob));
        ProcessingEngine processingEngine = new ProcessingEngine(logger);
        logger.cpmsInfo("Script run result: {}", processingEngine.runScriptedJob(scriptedJob));
    }

    /*public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("ORIEN Pipeline Management System - Manual Processing").build()
                .defaultHelp(true)
                .description("Manually processes.");
        parser.addArgument("-v", "--verbose").action(Arguments.storeTrue());
        parser.addArgument("-C", "--credentials")
                .setDefault("config.properties")
                .help("Configuration file holding credential information.");
        parser.addArgument("-A", "--accesskey");
        parser.addArgument("-S", "--secretkey");
        parser.addArgument("-E", "--endpoint");
        parser.addArgument("-R", "--region");
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
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger tmpLogger = loggerContext.getLogger("root");
            tmpLogger.setLevel(Level.toLevel("TRACE"));
        }
        String command = ns.getString("command"), credentials = ns.getString("credentials"),
                accessKey = ns.getString("accesskey"), secretKey = ns.getString("secretkey"),
                endpoint = ns.getString("endpoint"), region = ns.getString("region");
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
                    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                    ch.qos.logback.classic.Logger tmpLogger = loggerContext.getLogger("root");
                    tmpLogger.setLevel(Level.toLevel(config.getString("log.level")));
                }
                if (config.containsKey("access.key"))
                    accessKey = config.getString("access.key");
                if (config.containsKey("secret.key"))
                    secretKey = config.getString("secret.key");
                if (config.containsKey("endpoint"))
                    endpoint = config.getString("endpoint");
                if (config.containsKey("region"))
                    region = config.getString("region");
            } catch (ConfigurationException e) {
                logger.error("Error with your config.properties file.");
            }
        }
    }*/
}
