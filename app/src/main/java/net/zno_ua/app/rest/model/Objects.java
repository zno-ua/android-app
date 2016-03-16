package net.zno_ua.app.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 * @author vojkovladimir.
 */
@JsonIgnoreProperties({"meta"})
public class Objects<T> {
    private static final String OBJECTS = "objects";

    private List<T> mObjects;

    public List<T> get() {
        return mObjects;
    }

    @JsonSetter(OBJECTS)
    public void set(List<T> objects) {
        mObjects = objects;
    }
}
