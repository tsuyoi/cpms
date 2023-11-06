package io.cresco.cpms.database.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@SuppressWarnings("unused")
@Entity
@Table( name = "task" )
public class Task extends GenericModel {
    @NotNull
    @ManyToOne
    @JoinColumn( name = "pipeline_id", nullable = false )
    private Pipeline pipeline;

    @NotNull
    @Size( max = 255 )
    @Column( name = "name", nullable = false )
    private String name;

    @NotNull
    @Column( name = "script", columnDefinition = "longtext", nullable = false )
    private String script;

    public Task() {
        super();
    }

    public Task(Pipeline pipeline, String name, String script) {
        this();
        this.pipeline = pipeline;
        this.name = name;
        this.script = script;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getScript() {
        return script;
    }
    public void setScript(String script) {
        this.script = script;
    }
}
