package pl.edu.agh.gethere.connection;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dominik on 15.01.2017.
 */
public class RepositoryDefinitionsReceiver {

    private HttpResponseReceiver httpResponseReceiver;

    public List<String> createDefinitionList(String host) {
        List<String> definitionList = new ArrayList<>();
        try {
            String response = new HttpResponseReceiver(host).execute().get();
            JSONArray jsonDefinitionList = new JSONArray(response);
            int n = jsonDefinitionList.length();
            for (int i = 0; i < n; i++) {
                String jsonDefinition = jsonDefinitionList.getString(i);
                definitionList.add(jsonDefinition);
            }
            Collections.sort(definitionList.subList(1, definitionList.size()));
            return definitionList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
