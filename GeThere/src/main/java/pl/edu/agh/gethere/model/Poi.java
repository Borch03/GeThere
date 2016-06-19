package pl.edu.agh.gethere.model;

import java.io.Serializable;

/**
 * Created by Dominik on 18.06.2016.
 */

@SuppressWarnings("serial")
public class Poi implements Serializable {

    private String id;
    private String name;
    private String type;
    private String city;
    private String street;
    private String number;
    private Coordinates coordinates;

    public Poi(String id, String name, String type, String city, String street, String number, Coordinates coordinates) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.city = city;
        this.street = street;
        this.number = number;
        this.coordinates = coordinates;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
}
