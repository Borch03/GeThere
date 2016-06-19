package pl.edu.agh.gethere.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Dominik on 19.06.2016.
 */

@SuppressWarnings("serial")
public class ListOfPois implements Serializable {

    private List<Poi> poiList;

    public ListOfPois(List<Poi> poiList) {
        this.poiList = poiList;
    }

    public List<Poi> getPoiList() {
        return poiList;
    }

    public void setPoiList(List<Poi> poiList) {
        this.poiList = poiList;
    }
}
