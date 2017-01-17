package pl.edu.agh.gethere.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.model.OpeningHours;
import pl.edu.agh.gethere.model.Poi;

import java.util.*;

/**
 * Created by Dominik on 15.01.2017.
 */
public class JsonPoiParser {

    private JSONArray jsonPoiList;

    public JsonPoiParser(JSONArray jsonPoiList) {
        this.jsonPoiList = jsonPoiList;
    }

    public List<Poi> parseJsonPoiList() throws JSONException {
        if (jsonPoiList.length() == 0) {
            return null;
        }
        List<Poi> poiList = new ArrayList<>();
        int n = jsonPoiList.length();
        for (int i = 0; i < n; i++) {
            JSONObject jsonPoi = jsonPoiList.getJSONObject(i);
            Poi poi = createPoiFromJson(jsonPoi);
            poiList.add(poi);
        }
        return poiList;
    }

    private Poi createPoiFromJson(JSONObject jsonPoi) throws JSONException {
        String id = jsonPoi.getString("id");
        String name = jsonPoi.getString("name");
        String type = jsonPoi.getString("type");
        String coordinates = jsonPoi.getString("coordinates");
        double latitude =  Double.valueOf(coordinates.substring(0, coordinates.indexOf(";")));
        double longitude =  Double.valueOf(coordinates.substring(coordinates.indexOf(";")+1, coordinates.length()));
        OpeningHours openingHours = null;
        if (!jsonPoi.isNull("openingHours")) {
            String openingHoursString = jsonPoi.getString("openingHours");
            Date openingHour = new Date(Long.valueOf(openingHoursString.substring(0, openingHoursString.indexOf(";"))));
            Date closingHour = new Date(Long.valueOf(openingHoursString.substring(
                    openingHoursString.indexOf(";")+1, openingHoursString.length())));
            openingHours = new OpeningHours(openingHour, closingHour);
        }
        HashMap<String, String> attributes = new HashMap<>();
        Iterator<?> keys = jsonPoi.getJSONObject("attributes").keys();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            String value = jsonPoi.getJSONObject("attributes").getString(key);
            attributes.put(key, value);
        }

        return new Poi(id, name, type, new Coordinates(latitude, longitude), openingHours, attributes);
    }
}
