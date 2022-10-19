package io.cresco.cpms.database.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.cresco.cpms.logging.BasicCPMSLogger;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.statics.TestStatics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPipeline {
    static CPMSLogger logger = new BasicCPMSLoggerBuilder()
            .withClass(TestPipeline.class)
            .withPipelineID("Maven-Test-Environment")
            .withJobID("Model-Testing")
            .withTaskID("Pipeline-Model-Test")
            .withRunID("1")
            .build();

    @Test
    void testMethods() {
        logger.info("Testing Pipeline class methods");
        Pipeline toTest = new Pipeline(TestStatics.PIPELINE_NAME, TestStatics.PIPELINE_SCRIPT);
        Assertions.assertNotNull(toTest);
        Assertions.assertNotNull(toTest.getId());
        Assertions.assertEquals(TestStatics.PIPELINE_NAME, toTest.getName());
        Assertions.assertEquals(TestStatics.PIPELINE_SCRIPT, toTest.getScript());
        logger.info(
                "toTest.getScript():\n{}",
                toTest.getScript()
                        .replace("$input", "\"/mnt/data/input\"")
                        .replace("$output", "\"/mnt/data/output\""));
        logger.info("toTest.json():\n{}", toTest.json());
    }
}
