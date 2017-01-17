package pl.edu.agh.gethere.connection;

import android.os.AsyncTask;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dominik on 15.01.2017.
 */
public class RepositoryDefinitionsReceiver {

    public List<String> createDefinitionList(String host) {
        List<String> definitionList = new ArrayList<>();
        try {
            String response = new DefinitionReceiver(host).execute().get();
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

    private class DefinitionReceiver extends AsyncTask<String, Void, String> {

        private String host;

        public DefinitionReceiver(String host) {
            this.host = host;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider(host);
                httpConnectionProvider.getConnection().setRequestMethod("GET");
                return httpConnectionProvider.sendGetHttpRequest();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }
}
