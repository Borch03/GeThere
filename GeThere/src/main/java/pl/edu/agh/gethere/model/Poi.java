package pl.edu.agh.gethere.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by Dominik on 18.06.2016.
 */

@SuppressWarnings("serial")
public class Poi implements Serializable {

    private String id;
    private String name;
    private String type;
    private Coordinates coordinates;
    private HashMap<String, String> additionalInfo;

    public Poi(String id, String name, String type, Coordinates coordinates, HashMap<String, String> additionalInfo) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.coordinates = coordinates;
        this.additionalInfo = additionalInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public HashMap<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(HashMap<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
