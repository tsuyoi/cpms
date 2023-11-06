package io.cresco.cpms.scripting;

public interface ScriptedTask {
    String getId();
    String getName();
    String getType();
    String toJson();
}
