package io.cresco.cpms.scripting;

import com.google.gson.Gson;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DockerTask implements ScriptedTask {
    private final String id;
    private final String name;
    private final String type;
    private final String image;
    private final String command;
    private final String user;
    private final List<String> binds = new ArrayList<>();
    private final List<String> envs = new ArrayList<>();
    private final String dockerTaskJSON;

    public DockerTask(String dockerTaskJSON) throws ScriptException {
        this.dockerTaskJSON = dockerTaskJSON;
        DockerTaskScript dockerTaskScript = DockerTaskScript.getInstance(dockerTaskJSON);
        if (StringUtils.isBlank(dockerTaskScript.id))
            throw new ScriptException(
                    "Docker task is missing required parameter [id]"
            );
        this.id = dockerTaskScript.id;
        if (StringUtils.isBlank(dockerTaskScript.name))
            throw new ScriptException(
                    "Docker task is missing required parameter [name]"
            );
        this.name = dockerTaskScript.name;
        if (StringUtils.isBlank(dockerTaskScript.type))
            throw new ScriptException(
                    String.format("Docker task [%s] is missing required parameter [type]", getName())
            );
        this.type = dockerTaskScript.type;
        if (StringUtils.isBlank(dockerTaskScript.image))
            throw new ScriptException(
                    String.format("Docker task [%s] is missing required parameter [action]", getName())
            );
        this.image = validateImageName(dockerTaskScript.image);
        this.command = dockerTaskScript.command;
        this.user = dockerTaskScript.user;
        for (String bind : dockerTaskScript.binds)
            addBind(bind);
        for (String env : dockerTaskScript.envs)
            addEnv(env);
    }

    public void addBind(String bind) throws ScriptException {
        if (this.binds.contains(bind))
            return;
        String[] bindParts = bind.split(":");
        if (bindParts.length < 1 || bindParts.length > 3)
            throw new ScriptException("Binds should be of the format [host-src:]container-dest[:<options>]");
        this.binds.add(bind);
    }

    public void addEnv(String env) throws ScriptException {
        if (this.envs.contains(env))
            return;
        String[] envParts = env.split("=");
        if (envParts.length != 2)
            throw new ScriptException("Envs should be of the format <key>=<value>");
        this.envs.add(env);
    }

    public HostConfig getHostConfig() {
        return HostConfig.builder().binds(binds).build();
    }

    public ContainerConfig getContainerConfig() {
        if (command != null)
            return ContainerConfig.builder().hostConfig(getHostConfig()).env(envs)
                    .image(image).cmd(command).user(user).build();
        return ContainerConfig.builder().hostConfig(getHostConfig()).env(envs)
                .image(image).user(user).build();
    }

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public String getCommand() {
        return command;
    }

    public String getUser() {
        return user;
    }

    public List<String> getBinds() {
        return binds;
    }

    public List<String> getEnvs() {
        return envs;
    }

    public String getDockerTaskJSON() {
        return dockerTaskJSON;
    }

    private String validateImageName(String providedName) {
        String repoName = "";
        String imageName = providedName;
        int repoIdx = providedName.indexOf("/");
        if (repoIdx > -1) {
            repoName = providedName.substring(0, repoIdx);
            imageName = providedName.substring(repoIdx + 1);
        }
        if (!imageName.contains(":"))
            imageName = String.format("%s:latest", imageName);
        if (repoIdx > -1)
            return String.format("%s/%s", repoName, imageName);
        return imageName;
    }

    public String toJson() {
        return getDockerTaskJSON();
    }

    @Override
    public String toString() {
        String envsStr = (getEnvs().size() > 0) ? " " + getEnvs().stream()
                .map(e -> "-e " + e).collect(Collectors.joining(" ")) : "";
        String bindsStr = (getBinds().size() > 0) ? " " + getBinds().stream()
                .map(v -> "-v " + v).collect(Collectors.joining(" ")) : "";
        return String.format(
                "- Docker Task (ID: %s, Name: %s)\n\tCommand: docker run --rm -t%s%s%s %s%s",
                getId(), getName(),
                envsStr,
                bindsStr,
                (getUser() != null) ? " -u " + getUser() : "",
                getImage(),
                (getCommand() != null) ? " " + getCommand() : ""
                );
    }
}
