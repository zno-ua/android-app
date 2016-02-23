package net.zno_ua.app.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author vojkovladimir.
 */
@JsonIgnoreProperties({"meta"})
public class Objects<T> {
    public List<T> objects;
}
