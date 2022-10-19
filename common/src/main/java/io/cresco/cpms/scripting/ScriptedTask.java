package io.cresco.cpms.scripting;

public interface ScriptedTask {
    public String getId();
    public String getName();
    public String getType();
    public String toJson();
}
