package io.cresco.cpms.scripting;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptedJobScript {
    private static final Type scriptedJobScriptType = new TypeToken<ScriptedJobScript>() {}.getType();

    public static ScriptedJobScript getInstance(String scriptedJobJSON) throws ScriptException {
        try {
            Gson gson = new Gson();
            return gson.fromJson(scriptedJobJSON, scriptedJobScriptType);
        } catch (JsonSyntaxException e) {
            throw new ScriptException("Invalid job script supplied: " + scriptedJobJSON + "\n" + e.getMessage());
        }
    }

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("tasks")
    public List<LinkedTreeMap<String, Object>> tasks;

    @Override
    public String toString() {
        Gson gson = new Gson();
        Map<String, Object> toPrint = new HashMap<>();
        toPrint.put("id", this.id);
        toPrint.put("name", this.name);
        toPrint.put("tasks", this.tasks);
        return gson.toJson(toPrint);
    }
}
