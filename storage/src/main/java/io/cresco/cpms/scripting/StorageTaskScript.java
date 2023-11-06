package io.cresco.cpms.scripting;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class StorageTaskScript extends ScriptedTaskScript {
    private static final Type storageTaskScriptType = new TypeToken<StorageTaskScript>() {}.getType();

    public static StorageTaskScript getInstance(String storageTaskJSON) throws ScriptException {
        try {
            Gson gson = new Gson();
            return gson.fromJson(storageTaskJSON, storageTaskScriptType);
        } catch (JsonSyntaxException e) {
            throw new ScriptException("Invalid storage task script JSON supplied: " + storageTaskJSON);
        }
    }

    public static StorageTaskScript getInstance(Map<String, String> storageTaskMap) throws ScriptException {
        try {
            Gson gson = new Gson();
            JsonElement storageTaskJSON = gson.toJsonTree(storageTaskMap);
            return gson.fromJson(storageTaskJSON, storageTaskScriptType);
        } catch (JsonSyntaxException e) {
            throw new ScriptException("Invalid storage task script map supplied: " + storageTaskMap);
        }
    }

    @SerializedName("action")
    public String action;

    @SerializedName("remote_path")
    public String remotePath;

    @SerializedName("local_path")
    public String localPath;

    @Override
    public String toString() {
        Map<String, Object> toPrint = new HashMap<>();
        toPrint.put("id", this.id);
        toPrint.put("name", this.name);
        toPrint.put("type", this.type);
        toPrint.put("action", this.action);
        toPrint.put("remote_path", this.remotePath);
        toPrint.put("local_path", this.localPath);
        return toPrint.toString();
    }
}
