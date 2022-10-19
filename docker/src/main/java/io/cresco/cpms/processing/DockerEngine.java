package io.cresco.cpms.processing;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.*;
import com.spotify.docker.client.messages.*;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.scripting.DockerTask;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DockerEngine {
    private CPMSLogger logger;

    private StringBuilder output = new StringBuilder();

    private DockerClient dockerClient = null;
    private String dockerRunningID = null;

    public DockerEngine() {
        setLogger(new BasicCPMSLoggerBuilder().withClass(DockerEngine.class).build());
    }

    public DockerEngine(CPMSLogger logger) {
        setLogger(logger);
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(DockerEngine.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger.setRunID(logger.getRunID());
        this.logger.setPipelineID(logger.getPipelineID());
        this.logger.setJobID(logger.getJobID());
        this.logger.setTaskID(logger.getTaskID());
    }

    public String getOutput() {
        return output.toString();
    }

    public boolean hasDockerImage(String imageName) {
        logger.debug("hasDockerImage({})", imageName);
        try (DockerClient tmpDockerClient = DefaultDockerClient.fromEnv().build()) {
            List<Image> dockerImages = tmpDockerClient.listImages();
            List<String> matches = dockerImages.stream().map(Image::repoTags).filter(Objects::nonNull)
                    .flatMap(List::stream).filter(t -> t.equals(imageName)).collect(Collectors.toList());
            return matches.size() > 0;
        } catch (DockerCertificateException e) {
            logger.cpmsError("Docker certificates improperly configured on this machine");
            return false;
        } catch (DockerException e) {
            logger.cpmsError("Docker exception encountered: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            logger.cpmsError("Docker command interrupted");
            return false;
        }
    }

    public boolean pullDockerImage(String imageName) {
        logger.debug("pullDockerImage({})", imageName);
        try (DockerClient tmpDockerClient = DefaultDockerClient.fromEnv().build()) {
            logger.cpmsInfo("Pulling Docker image [{}]", imageName);
            tmpDockerClient.pull(imageName);
            return true;
        } catch (DockerCertificateException e) {
            logger.cpmsError("Docker certificates improperly configured on this machine");
            return false;
        } catch(ImageNotFoundException e) {
            logger.cpmsError("Docker registry does not have the image [{}]", imageName);
            return false;
        } catch (DockerException e) {
            logger.cpmsError("Docker exception encountered: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            logger.cpmsError("Docker command interrupted");
            return false;
        }
    }

    public long runDockerJob(DockerTask dockerTask) {
        if (!hasDockerImage(dockerTask.getImage()))
            if (!pullDockerImage(dockerTask.getImage()))
                return -1;
        String dockerJobRunCommand = dockerTask.toString();
        logger.cpmsInfo("Running Docker: {}", dockerJobRunCommand);
        try {
            dockerClient = DefaultDockerClient.fromEnv().build();
            final ContainerCreation creation = dockerClient.createContainer(dockerTask.getContainerConfig());
            dockerRunningID = creation.id();
            dockerClient.startContainer(dockerRunningID);
            try (final LogStream logStream = dockerClient.logs(dockerRunningID, DockerClient.LogsParam.follow(),
                    DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr())) {
                String stagePhase = "uninit";
                output.append(dockerJobRunCommand);
                output.append("\n\n");
                output.append(String.join("", Collections.nCopies(40, "=")));
                output.append("\n");
                while (logStream.hasNext()) {
                    final LogMessage msg = logStream.next();
                    final String outputLine = StandardCharsets.UTF_8.decode(msg.content()).toString();
                    output.append(String.format("%s", outputLine));

                    String[] outputStr = outputLine.split("\\|\\|");

                    for (int i = 0; i < outputStr.length; i++) {
                        outputStr[i] = outputStr[i].trim();
                    }

                    if ((outputStr.length == 5) &&
                            ((outputLine.toLowerCase().startsWith("info")) ||
                                    (outputLine.toLowerCase().startsWith("error")))) {
                        if (outputStr[0].equalsIgnoreCase("info")) {
                            if (!stagePhase.equals(outputStr[3]))
                                logger.cpmsInfo("Pipeline is now in phase: " + outputStr[3]);
                            stagePhase = outputStr[3];
                        } else if (outputStr[0].equalsIgnoreCase("error"))
                            logger.cpmsError("Pipeline error: " + outputLine);
                    }
                    logger.info("Docker: " + outputLine.trim());
                }
            }
            logger.cpmsInfo("Container has finished, gathering information");
            ContainerState state = dockerClient.inspectContainer(dockerRunningID).state();
            logger.cpmsTaskOutput(dockerTask, output.toString());
            Thread.sleep(2000);
            output = new StringBuilder();
            logger.cpmsInfo("Pipeline has completed");
            Long retValue = state.exitCode();
            return (retValue != null) ? retValue : -1;
        } catch (ImageNotFoundException e) {
            logger.cpmsError("Docker image [{}] does not exist on this machine", dockerTask.getImage());
            return 404;
        } catch (BadParamException e) {
            logger.cpmsError("Docker parameter error: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            return 125;
        } catch(ContainerNotFoundException e) {
            logger.cpmsError("Docker container failed creation: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            return 125;
        } catch (DockerException e) {
            logger.cpmsError("Docker container failed to run: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            return 125;
        } catch (InterruptedException e) {
            logger.cpmsError("Docker container interrupted");
            return 137;
        } catch (Exception e) {
            logger.cpmsError("Run container exception encountered [{}:{}]",
                    e.getClass().getCanonicalName(), e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            return -1;
        } finally {
            try {
                if (dockerClient != null) {
                    if (dockerRunningID != null) {
                        logger.cpmsInfo("Removing container: {}", dockerRunningID);
                        dockerClient.removeContainer(dockerRunningID);
                        dockerRunningID = null;
                    }
                    dockerClient.close();
                    dockerClient = null;
                }
            } catch (DockerException e) {
                logger.cpmsError("Failed to close Docker client: " + e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
            } catch (InterruptedException e) {
                logger.cpmsError("Docker client closure was interrupted: " + e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
            } catch (Exception e) {
                logger.cpmsError("Docker client closure execption encountered [{}:{}]",
                        e.getClass().getCanonicalName(), e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public boolean isDockerRunning() {
        if (dockerClient != null) {
            if (dockerRunningID != null) {
                try {
                    ContainerState state = dockerClient.inspectContainer(dockerRunningID).state();
                    return state.running();
                } catch (DockerException | InterruptedException e) {
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    public boolean stopDockerContainer() {
        try {
            if (dockerClient != null && dockerRunningID != null) {
                dockerClient.killContainer(dockerRunningID);
                return true;
            }
        } catch (DockerException e) {
            logger.cpmsError("Failed to kill container [{}] on this machine: {}", dockerRunningID, e.getMessage());
        } catch (InterruptedException e) {
            logger.cpmsError("Task to kill container [{}] was interrupted");
        }
        return false;
    }
}
