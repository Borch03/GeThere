package pl.edu.agh.gethere.controller;

import android.app.DialogFragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import org.json.JSONObject;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.utils.NegativeMessageWindow;
import pl.edu.agh.gethere.utils.PositiveMessageWindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AddPoiActivity extends AppCompatActivity {

    public final static String EMULATOR_HOST = "http://10.0.2.2:9000/triples";
    public final static String HOST = "http://localhost:9000/triples";
    public final static String GETHERE_URL = "http://gethere.agh.edu.pl/#";
    public final static String TYPE_IRI = GETHERE_URL+ "type";
    public final static String VALUE_IRI = GETHERE_URL+ "value";

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

        final EditText xCoordinateField = (EditText) findViewById(R.id.EditTextXCoordinate);
        final EditText yCoordinateField = (EditText) findViewById(R.id.EditTextYCoordinate);

        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location location = null;

        try {
            for (int i = providers.size() - 1; i >= 0; i--) {
                location = lm.getLastKnownLocation(providers.get(i));
                if (location != null)
                    break;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        xCoordinateField.setText(Double.toString(location.getLatitude()));
        yCoordinateField.setText(Double.toString(location.getLongitude()));

    }

    public void addPoiToRepository(View button) {

        final EditText poiNameField = (EditText) findViewById(R.id.EditTextPoiName);
        final EditText cityField = (EditText) findViewById(R.id.EditTextCity);
        final EditText streetField = (EditText) findViewById(R.id.EditTextStreet);
        final EditText numberField = (EditText) findViewById(R.id.EditTextNumber);
        final Spinner poiSpinner = (Spinner) findViewById(R.id.SpinnerPoiType);
        final EditText xCoordinateField = (EditText) findViewById(R.id.EditTextXCoordinate);
        final EditText yCoordinateField = (EditText) findViewById(R.id.EditTextYCoordinate);

        String poiName = poiNameField.getText().toString();
        String city = cityField.getText().toString();
        String street = streetField.getText().toString();
        String number = numberField.getText().toString();
        String poiType = poiSpinner.getSelectedItem().toString();
        String xCoordinate = xCoordinateField.getText().toString();
        String yCoordinate = yCoordinateField.getText().toString();

        String poiIRI = GETHERE_URL +
                ("POI-" + poiName + "-" + city + "-" + street + number).replaceAll(" ", "_");
        String poiTypeIRI = GETHERE_URL + poiType.replaceAll(" ", "_");
        String coordinatesIRI = GETHERE_URL + xCoordinate + ";" + yCoordinate;

        new JsonSender(poiIRI, TYPE_IRI, poiTypeIRI).execute();
        new JsonSender(poiIRI, VALUE_IRI, coordinatesIRI).execute();

        poiNameField.getText().clear();
        cityField.getText().clear();
        streetField.getText().clear();
        numberField.getText().clear();
        xCoordinateField.getText().clear();
        yCoordinateField.getText().clear();
    }

    private class JsonSender extends AsyncTask <String, Void, String> {

        private String subject;
        private String predicate;
        private String object;

        JsonSender(String subject, String predicate, String object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                URL url = new URL(EMULATOR_HOST);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestMethod("POST");

                JSONObject triple = new JSONObject();
                triple.put("subject", subject);
                triple.put("predicate", predicate);
                triple.put("object", object);

                OutputStream os = connection.getOutputStream();
                os.write(triple.toString().getBytes("UTF-8"));
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
