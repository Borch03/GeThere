package pl.edu.agh.gethere.controller;

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
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.model.ListOfPois;
import pl.edu.agh.gethere.model.Poi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FindPoiActivity extends AppCompatActivity {

    public final static String EMULATOR_HOST = "http://10.0.2.2:9000/";
    public final static String HOST = "http://localhost:9000/";
    public final static String KEYWORD_HOST = EMULATOR_HOST + "keyword";

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
            JSONObject result = new JSONObject(response);
            JSONArray jsonPoiList = new JSONArray(result);
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
        String city = jsonPoi.getString("city");
        String street = jsonPoi.getString("street");
        String number = jsonPoi.getString("number");
        String coordinates = jsonPoi.getString("coordinates");
        double latitude =  Double.valueOf(coordinates.substring(0, coordinates.indexOf(";")));
        double longitude =  Double.valueOf(coordinates.substring(coordinates.indexOf(";")+1, coordinates.length()));

        return new Poi(id, name, type, city, street, number, new Coordinates(latitude, longitude));
    }

    class KeywordRequestTask extends AsyncTask<String, String, String> {

        private String keyword;

        public KeywordRequestTask(String keyword) {
            this.keyword = keyword;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                URL url = new URL(KEYWORD_HOST);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                connection.setRequestMethod("POST");

                OutputStream os = connection.getOutputStream();
                os.write(keyword.getBytes("UTF-8"));
                os.close();

                String response;

                StringBuilder sb = new StringBuilder();
                int HttpResult = connection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    response = "" + sb.toString();
                } else {
                    response = connection.getResponseMessage();
                }
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }

}
