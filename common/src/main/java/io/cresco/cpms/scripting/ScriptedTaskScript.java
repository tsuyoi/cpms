package io.cresco.cpms.scripting;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ScriptedTaskScript {
    private static final Type scriptedTaskScriptType = new TypeToken<ScriptedTaskScript>() {}.getType();

    public static ScriptedTaskScript getInstance(String scriptedTaskJSON) throws ScriptException {
        try {
            Gson gson = new Gson();
            return gson.fromJson(scriptedTaskJSON, scriptedTaskScriptType);
        } catch (JsonSyntaxException e) {
            throw new ScriptException("Invalid task script supplied: " + scriptedTaskJSON);
        }
    }

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("type")
    public String type;

    @Override
    public String toString() {
        Map<String, Object> toPrint = new HashMap<>();
        toPrint.put("id", this.id);
        toPrint.put("name", this.name);
        toPrint.put("type", this.type);
        return toPrint.toString();
    }
}
