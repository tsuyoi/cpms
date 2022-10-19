package io.cresco.cpms.scripting;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptedJob {
    private final String id;
    private final String name;
    private final List<ScriptedTask> tasks = new ArrayList<>();
    private final String scriptedJobJSON;

    public ScriptedJob(String scriptedJobJSON) throws ScriptException {
        this.scriptedJobJSON = scriptedJobJSON;
        ScriptedJobScript scriptedJobScript = ScriptedJobScript.getInstance(scriptedJobJSON);
        if (StringUtils.isBlank(scriptedJobScript.id))
            throw new ScriptException(
                    "Job is missing required parameter [id]"
            );
        this.id = scriptedJobScript.id;
        if (StringUtils.isBlank(scriptedJobScript.name))
            throw new ScriptException(
                    "Job is missing required parameter [name]"
            );
        this.name = scriptedJobScript.name;
        if (scriptedJobScript.tasks.size() < 1)
            throw new ScriptException(
                    "Job is missing required parameter [tasks]"
            );
        Gson gson = new Gson();
        for (LinkedTreeMap<String, Object> task : scriptedJobScript.tasks) {
            String taskJSON = gson.toJson(task);
            ScriptedTaskScript scriptedTaskScript = ScriptedTaskScript.getInstance(taskJSON);
            switch (scriptedTaskScript.type) {
                case "storage":
                    tasks.add(new StorageTask(taskJSON));
                    break;
                case "docker":
                    tasks.add(new DockerTask(taskJSON));
                    break;
                default:
                    throw new ScriptException("Invalid task type [" + scriptedTaskScript.type + "] supplied in job: " +
                            scriptedJobJSON);
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ScriptedTask> getTasks() {
        return tasks;
    }

    public String getScriptedJobJSON() {
        return scriptedJobJSON;
    }

    @Override
    public String toString() {
        return "ID: " + getId() + "\nName: " + getName() + "\n" + getTasks().stream().map(ScriptedTask::toString).collect(Collectors.joining("\n"));
    }
}
