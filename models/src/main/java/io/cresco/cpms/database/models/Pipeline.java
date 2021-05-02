package io.cresco.cpms.database.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@SuppressWarnings("unused")
@Entity
@Table( name = "pipeline" )
public class Pipeline extends GenericModel {
    @NotNull
    @Size( max = 255 )
    @Column( name = "name", nullable = false, unique = true )
    private String name;

    @NotNull
    @Column( name = "script", columnDefinition = "longtext", nullable = false )
    private String script;

    public Pipeline() {
        super();
    }

    public Pipeline(String name, String script) {
        this();
        this.name = name;
        this.script = script;
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
