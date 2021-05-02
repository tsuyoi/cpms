package io.cresco.cpms.database.models;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

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
