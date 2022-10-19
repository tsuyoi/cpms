package io.cresco.cpms.scripting;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerTaskScript extends ScriptedTaskScript {
    private static final Type dockerTaskScriptType = new TypeToken<DockerTaskScript>() {}.getType();

    public static DockerTaskScript getInstance(String dockerTaskJSON) throws ScriptException {
        try {
            Gson gson = new Gson();
            return gson.fromJson(dockerTaskJSON, dockerTaskScriptType);
        } catch (JsonSyntaxException e) {
            throw new ScriptException("Invalid Docker task script supplied: " + dockerTaskJSON);
        }
    }

    @SerializedName("image")
    public String image;

    @SerializedName("command")
    public String command;

    @SerializedName("user")
    public String user;

    @SerializedName("binds")
    public List<String> binds = new ArrayList<>();

    @SerializedName("envs")
    public List<String> envs = new ArrayList<>();

    @Override
    public String toString() {
        Map<String, Object> toPrint = new HashMap<>();
        toPrint.put("id", this.id);
        toPrint.put("name", this.name);
        toPrint.put("type", this.type);
        toPrint.put("image", this.image);
        toPrint.put("command", this.command);
        toPrint.put("user", this.user);
        toPrint.put("binds", this.binds);
        toPrint.put("envs", this.envs);
        return toPrint.toString();
    }
}
