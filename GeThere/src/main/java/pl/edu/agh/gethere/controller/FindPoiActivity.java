package pl.edu.agh.gethere.controller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.connection.HttpConnectionProvider;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.model.ListOfPois;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.utils.SingleAlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FindPoiActivity extends AppCompatActivity {

    public final static String EMULATOR_HOST = "http://10.0.2.2:9000/";
    public final static String HOST = "http://localhost:9000/";
    public final static String KEYWORD_HOST = EMULATOR_HOST + "keyword";

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_poi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_poi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void searchKeyword(View button) {

        final EditText keywordField = (EditText) findViewById(R.id.EditTextKeyword);
        String keyword = keywordField.getText().toString();

        try {
            String response = new KeywordRequestTask(keyword).execute().get();
            JSONArray jsonPoiList = new JSONArray(response);
            if (jsonPoiList.length() == 0) {
                String title = "Not found";
                String message = "No POI found.";
                new SingleAlertDialog(title, message).displayAlertMessage(context);
                return;
            }
            List<Poi> poiList = new ArrayList<>();
            int n = jsonPoiList.length();
            for (int i = 0; i < n; i++) {
                JSONObject jsonPoi = jsonPoiList.getJSONObject(i);
                Poi poi = createPoiFromJson(jsonPoi);
                poiList.add(poi);
            }
            ListOfPois listOfPois = new ListOfPois(poiList);

            Intent intent = new Intent(this, ListOfPoisActivity.class);
            intent.putExtra("listOfPois", listOfPois);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Poi createPoiFromJson(JSONObject jsonPoi) throws JSONException {
        String id = jsonPoi.getString("id");
        String name = jsonPoi.getString("name");
        String type = jsonPoi.getString("type");
        String coordinates = jsonPoi.getString("coordinates");
        double latitude =  Double.valueOf(coordinates.substring(0, coordinates.indexOf(";")));
        double longitude =  Double.valueOf(coordinates.substring(coordinates.indexOf(";")+1, coordinates.length()));
        HashMap<String, String> attributes = new HashMap<>();
        Iterator<?> keys = jsonPoi.getJSONObject("attributes").keys();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            String value = jsonPoi.getJSONObject("attributes").getString(key);
            attributes.put(key, value);
        }

        return new Poi(id, name, type, new Coordinates(latitude, longitude), attributes);
    }

    class KeywordRequestTask extends AsyncTask<String, String, String> {

        private String keyword;

        public KeywordRequestTask(String keyword) {
            this.keyword = keyword;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider(KEYWORD_HOST);
                httpConnectionProvider.getConnection().setDoOutput(true);
                httpConnectionProvider.getConnection().setDoInput(true);
                httpConnectionProvider.getConnection().setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                httpConnectionProvider.getConnection().setRequestMethod("POST");

                return httpConnectionProvider.sendPostHttpRequest(keyword.getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if ((result == null) || result.equals("Not Found")) {
                String errorTitle = "Error";
                String errorMessage = "Sorry, something went wrong.";
                new SingleAlertDialog(errorTitle, errorMessage).displayAlertMessage(context);
            }
            super.onPostExecute(result);
        }

    }

}
