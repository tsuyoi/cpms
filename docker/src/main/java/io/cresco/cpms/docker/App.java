package io.cresco.cpms.docker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final Logger simpleLogger = LoggerFactory.getLogger("message-only");

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Cresco Pipeline Management System - Docker Manipulator")
                .build()
                .defaultHelp(true)
                .description("Executes a Docker task from an input yaml file.");
        parser.addArgument("-v", "--verbose").action(Arguments.storeTrue());
        parser.addArgument("-b", "--binds").help("semi-colon (;) separated list of binds");
        parser.addArgument("-e", "--envs").help("semi-colon (;) separated list of envs");
        parser.addArgument("-u", "--user").help("system user uid to use to run the container");
        parser.addArgument("image");
        parser.addArgument("command").nargs("?");
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
        String image = ns.getString("image"), cmd = ns.getString("command"),
                binds = ns.getString("binds"), envs = ns.getString("envs"),
                user = ns.getString("user");

        /*DockerTask dockerTask = new DockerTask(image, cmd, user);
        if (envs != null)
            for (String env : envs.split(";"))
                dockerTask.addEnv(env);
        if (binds != null)
            for (String bind : binds.split(";"))
                dockerTask.addBind(bind);
        logger.info("Starting docker job:");
        logger.info("{}", dockerTask);
        DockerEngine dockerEngine = new DockerEngine();
        long retCode = dockerEngine.runDockerJob(dockerTask);
        if (retCode == 0)
            simpleLogger.info("Successfully completed docker job");
        else
            simpleLogger.error("Failed to complete docker job. Please rerun using -v for more information.");*/
    }
}
