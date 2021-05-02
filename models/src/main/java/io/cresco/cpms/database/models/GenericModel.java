package io.cresco.cpms.database.models;

import com.google.gson.Gson;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@SuppressWarnings("unused")
@MappedSuperclass
public class GenericModel {
    @Id
    @Column( name = "id", updatable = false, nullable = false )
    private String id;

    public GenericModel() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String json() {
        return new Gson().toJson(this);
    }
}
