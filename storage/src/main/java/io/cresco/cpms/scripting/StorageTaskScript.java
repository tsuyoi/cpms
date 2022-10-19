package io.cresco.cpms.scripting;

import com.google.gson.Gson;
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
            throw new ScriptException("Invalid storage task script supplied: " + storageTaskJSON);
        }
    }

    @SerializedName("action")
    public String action;

    @SerializedName("s3_access_key")
    public String s3AccessKey;

    @SerializedName("s3_secret_key")
    public String s3SecretKey;

    @SerializedName("s3_endpoint")
    public String s3Endpoint;

    @SerializedName("s3_region")
    public String s3Region;

    @SerializedName("s3_bucket")
    public String s3Bucket;

    @SerializedName("s3_path")
    public String s3Path;

    @SerializedName("local_path")
    public String localPath;

    @Override
    public String toString() {
        Map<String, Object> toPrint = new HashMap<>();
        toPrint.put("id", this.id);
        toPrint.put("name", this.name);
        toPrint.put("type", this.type);
        toPrint.put("action", this.action);
        toPrint.put("s3_access_key", this.s3AccessKey);
        toPrint.put("s3_secret_key", this.s3SecretKey);
        toPrint.put("s3_endpoint", this.s3Endpoint);
        toPrint.put("s3_region", this.s3Region);
        toPrint.put("s3_bucket", this.s3Bucket);
        toPrint.put("s3_path", this.s3Path);
        toPrint.put("local_path", this.localPath);
        return toPrint.toString();
    }
}
