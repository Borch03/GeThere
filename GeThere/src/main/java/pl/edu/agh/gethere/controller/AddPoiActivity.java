package pl.edu.agh.gethere.controller;

import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.utils.NegativeMessageWindow;
import pl.edu.agh.gethere.utils.PositiveMessageWindow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class AddPoiActivity extends AppCompatActivity {

    public final static String EMULATOR_HOST = "http://10.0.2.2:9000/";
    public final static String HOST = "http://localhost:9000/";
    public final static String TRIPLE_HOST = EMULATOR_HOST + "triples";
    public final static String GETHERE_URL = "http://gethere.agh.edu.pl/#";

    public final static String TYPE_IRI = GETHERE_URL+ "isTypeOf";
    public final static String NAME_IRI = GETHERE_URL+ "hasName";
    public final static String CITY_IRI = GETHERE_URL+ "isInCity";
    public final static String STREET_IRI = GETHERE_URL+ "isOnStreet";
    public final static String NUMBER_IRI = GETHERE_URL+ "hasNumber";
    public final static String COORDINATES_IRI = GETHERE_URL+ "hasCoordinates";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_poi, menu);
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

    public void getYourLocation(View button, Context ctx) {

        final EditText xCoordinateField = (EditText) findViewById(R.id.EditTextLatitude);
        final EditText yCoordinateField = (EditText) findViewById(R.id.EditTextLongitude);

        //TODO

    }

    public void addPoiToRepository(View button) {

        final EditText poiNameField = (EditText) findViewById(R.id.EditTextPoiName);
        final EditText cityField = (EditText) findViewById(R.id.EditTextCity);
        final EditText streetField = (EditText) findViewById(R.id.EditTextStreet);
        final EditText numberField = (EditText) findViewById(R.id.EditTextNumber);
        final Spinner poiSpinner = (Spinner) findViewById(R.id.SpinnerPoiType);
        final EditText latitudeField = (EditText) findViewById(R.id.EditTextLatitude);
        final EditText longitudeField = (EditText) findViewById(R.id.EditTextLongitude);

        String name = poiNameField.getText().toString();
        String city = cityField.getText().toString();
        String street = streetField.getText().toString();
        String number = numberField.getText().toString();
        String type = poiSpinner.getSelectedItem().toString();
        double latitude = Double.valueOf(latitudeField.getText().toString());
        double longitude = Double.valueOf(longitudeField.getText().toString());

        String id = UUID.randomUUID().toString();
        Poi poi = new Poi(id, name, type, city, street, number, new Coordinates(latitude, longitude));

        new JsonSender(poi).execute();

        poiNameField.getText().clear();
        cityField.getText().clear();
        streetField.getText().clear();
        numberField.getText().clear();
        latitudeField.getText().clear();
        longitudeField.getText().clear();
    }


    private class JsonSender extends AsyncTask <String, Void, String> {

        private Poi poi;

        JsonSender(Poi poi) {
            this.poi = poi;
        }

        private JSONArray createJsonArray() throws JSONException {

            JSONArray triples = new JSONArray();

            String iriId = GETHERE_URL + poi.getId();
            String iriType = GETHERE_URL + poi.getType();

            JSONObject typeTriple = createTripleJsonObject(iriId, TYPE_IRI, iriType);
            JSONObject nameTriple = createTripleJsonObject(iriId, NAME_IRI, poi.getName());
            JSONObject cityTriple = createTripleJsonObject(iriId, CITY_IRI, poi.getCity());
            JSONObject streetTriple = createTripleJsonObject(iriId, STREET_IRI, poi.getStreet());
            JSONObject numberTriple = createTripleJsonObject(iriId, NUMBER_IRI, poi.getNumber());
            JSONObject coordinatesTriple = createTripleJsonObject(iriId, COORDINATES_IRI,
                    String.valueOf(poi.getCoordinates().getLatitude()) + ";" +
                            String.valueOf(poi.getCoordinates().getLongitude()));

            triples.put(typeTriple);
            triples.put(nameTriple);
            triples.put(cityTriple);
            triples.put(streetTriple);
            triples.put(numberTriple);
            triples.put(coordinatesTriple);

            return triples;
        }

        private JSONObject createTripleJsonObject(String subject, String predicate, String object) throws JSONException {
            JSONObject triple = new JSONObject();
            triple.put("subject", subject);
            triple.put("predicate", predicate);
            triple.put("object", object);

            return triple;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                URL url = new URL(TRIPLE_HOST);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestMethod("POST");

                JSONArray triples = createJsonArray();

                OutputStream os = connection.getOutputStream();
                os.write(triples.toString().getBytes("UTF-8"));
                os.close();

                String message;

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
                    message = "" + sb.toString();
                } else {
                    message = connection.getResponseMessage();
                }
                return message;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String message) {
            if (message.equals("No Content")) {
                DialogFragment dialog = new PositiveMessageWindow();
                dialog.show(getFragmentManager(), "PositiveMessageTag");
            } else {
                DialogFragment dialog = new NegativeMessageWindow();
                dialog.show(getFragmentManager(), "NegativeMessageTag");
            }
        }
    }
}
