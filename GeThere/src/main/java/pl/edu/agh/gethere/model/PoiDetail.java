package pl.edu.agh.gethere.model;

import java.io.Serializable;

/**
 * Created by Dominik on 08.08.2016.
 */

@SuppressWarnings("serial")
public class PoiDetail implements Serializable {

    private String description;
    private String value;

    public PoiDetail(String description, String value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }
}