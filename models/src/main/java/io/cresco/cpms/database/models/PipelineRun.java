package io.cresco.cpms.database.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@SuppressWarnings("unused")
@Entity
@Table( name = "pipeline_run" )
public class PipelineRun extends GenericRunModel {
    @NotNull
    @ManyToOne
    @JoinColumn( name = "pipeline_id", nullable = false )
    private Pipeline pipeline;

    public PipelineRun() {
        super();
    }

    public PipelineRun(Pipeline pipeline) {
        this();
        this.pipeline = pipeline;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }
}
