package io.cresco.cpms.processing;

import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.scripting.DockerTask;
import io.cresco.cpms.scripting.ScriptedJob;
import io.cresco.cpms.scripting.ScriptedTask;

public class ProcessingEngine {
    private CPMSLogger logger;

    public ProcessingEngine() {
        setLogger(new BasicCPMSLoggerBuilder().withClass(ProcessingEngine.class).build());
    }

    public ProcessingEngine(CPMSLogger logger) {
        setLogger(logger);
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(ProcessingEngine.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger.setPipelineID(logger.getPipelineID());
        this.logger.setPipelineName(logger.getPipelineName());
        this.logger.setJobID(logger.getJobID());
        this.logger.setJobName(logger.getJobName());
        this.logger.setTaskID(logger.getTaskID());
        this.logger.setTaskName(logger.getTaskName());
        this.logger.setRunID(logger.getRunID());
        this.logger.setRunName(logger.getRunName());
    }

    public long runScriptedJob(ScriptedJob scriptedJob) {
        logger.setJobID(scriptedJob.getId());
        logger.setJobName(scriptedJob.getName());
        for (ScriptedTask scriptedTask : scriptedJob.getTasks()) {
            long taskRet = runScriptedTask(scriptedTask);
            logger.cpmsInfo("Task Return Code: {}", taskRet);
            if (taskRet != 0) {
                logger.cpmsFailure("Task execution failed, please see logs for details");
                return taskRet;
            }
        }
        return 0;
    }

    public long runScriptedTask(ScriptedTask scriptedTask) {
        long taskRetCode = -1;
        logger.setTaskID(scriptedTask.getId());
        logger.setTaskName(scriptedTask.getName());
        switch (scriptedTask.getType()) {
            case "storage":
                logger.info("Storage task");
                logger.cpmsInfo(String.valueOf(scriptedTask));
                break;
            case "docker":
                logger.info("Docker task");
                DockerEngine dockerEngine = new DockerEngine(logger);
                taskRetCode = dockerEngine.runDockerJob((DockerTask) scriptedTask);
                break;
            default:
                logger.cpmsError("Invalid task type [{}] supplied", scriptedTask.getType());
                break;
        }
        logger.setTaskID(null);
        logger.setTaskName(null);
        return taskRetCode;
    }
}
