package pl.edu.agh.gethere.controller;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.adapter.AdditionalInfoAdapter;
import pl.edu.agh.gethere.connection.HttpConnectionProvider;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.utils.SingleAlertDialog;

import java.util.*;

public class AddPoiActivity extends AppCompatActivity {

    public final static String EMULATOR_HOST = "http://10.0.2.2:9000/";
    public final static String HOST = "http://localhost:9000/";
    public final static String ADDITIONAL_INFO_HOST = EMULATOR_HOST + "additional_info";
    public final static String TRIPLE_HOST = EMULATOR_HOST + "triples";
    public final static String GETHERE_URL = "http://gethere.agh.edu.pl/#";

    public final static String TYPE_PREDICATE = GETHERE_URL + "isTypeOf";
    public final static String NAME_PREDICATE = GETHERE_URL + "hasName";
    public final static String COORDINATES_PREDICATE = GETHERE_URL + "hasCoordinates";

    private Context context = this;
    private List<String> additionalInfoList;
    private AdditionalInfoAdapter additionalInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        additionalInfoList = createAdditionalInfoList();
        additionalInfoAdapter = new AdditionalInfoAdapter(context, new ArrayList<String>());
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

    public void getYourLocation(View button) {

        final EditText latitudeEditText = (EditText) findViewById(R.id.EditTextLatitude);
        final EditText longitudeEditText = (EditText) findViewById(R.id.EditTextLongitude);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String title = "GPS error";
            String message = "Cannot get GPS coordinates.";
            new SingleAlertDialog(title, message).displayAlertMessage(context);
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        latitudeEditText.setText(String.valueOf(latitude));
        longitudeEditText.setText(String.valueOf(longitude));
    }

    public void addAdditionalInfo(View button) {
        final String[] additionalInfoArray = additionalInfoList.toArray(new String[additionalInfoList.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose kind of information:");
        builder.setItems(additionalInfoArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                ListView additionalInfoListView = (ListView) findViewById(R.id.PoiDataList);
                additionalInfoAdapter.add(additionalInfoArray[item]);
                additionalInfoListView.setAdapter(additionalInfoAdapter);
            }
        });
        builder.setCancelable(false).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void addPoiToRepository(View button) {

        final EditText poiNameField = (EditText) findViewById(R.id.EditTextPoiName);
        final Spinner poiSpinner = (Spinner) findViewById(R.id.SpinnerPoiType);
        final EditText latitudeField = (EditText) findViewById(R.id.EditTextLatitude);
        final EditText longitudeField = (EditText) findViewById(R.id.EditTextLongitude);
        final ListView additionalInfoListView = (ListView) findViewById(R.id.AdditionalInfoList);

        String name = poiNameField.getText().toString();
        String type = poiSpinner.getSelectedItem().toString();
        double latitude = Double.valueOf(latitudeField.getText().toString());
        double longitude = Double.valueOf(longitudeField.getText().toString());
        HashMap<String, String> additionalInfo = new HashMap<>();
        for (int i = 0; i < additionalInfoListView.getCount(); i++) {
            View view = additionalInfoListView.getChildAt(i);
            EditText info = (EditText) view.findViewById(R.id.AdditionalInfoEditText);
            additionalInfo.put(info.getHint().toString(), info.getText().toString());
        }

        String id = UUID.randomUUID().toString();
        Poi poi = new Poi(id, name, type, new Coordinates(latitude, longitude), additionalInfo);

        new PoiSender(poi).execute();

        poiNameField.getText().clear();
        latitudeField.getText().clear();
        longitudeField.getText().clear();
        additionalInfoListView.setAdapter(new AdditionalInfoAdapter(context, new ArrayList<String>()));
    }

    private List<String> createAdditionalInfoList() {
        List<String> additionalInfoList = new ArrayList<>();
        try {
            String response = new AdditionalInfoDefinitionsReceiver().execute().get();
            JSONArray jsonAdditionalInfoList = new JSONArray(response);
            int n = jsonAdditionalInfoList.length();
            for (int i = 0; i < n; i++) {
                JSONObject jsonAdditionalInfo = jsonAdditionalInfoList.getJSONObject(i);
                additionalInfoList.add(jsonAdditionalInfo.toString());
            }
            return additionalInfoList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class AdditionalInfoDefinitionsReceiver extends AsyncTask <Void, String, String> {

        @Override
        protected String doInBackground(Void[] params) {
            try {
                HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider(ADDITIONAL_INFO_HOST);
                httpConnectionProvider.getConnection().setDoOutput(true);
                httpConnectionProvider.getConnection().setRequestMethod("GET");

                return httpConnectionProvider.sendGetHttpRequest();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class PoiSender extends AsyncTask <String, Void, String> {

        private Poi poi;

        PoiSender(Poi poi) {
            this.poi = poi;
        }

        private JSONArray createJsonArray() throws JSONException {

            JSONArray triples = new JSONArray();

            String poiIri = GETHERE_URL + poi.getId();
            String typeIri = GETHERE_URL + poi.getType();

            JSONObject typeTriple = createTripleJsonObject(poiIri, TYPE_PREDICATE, typeIri);
            JSONObject nameTriple = createTripleJsonObject(poiIri, NAME_PREDICATE, poi.getName());
            JSONObject coordinatesTriple = createTripleJsonObject(poiIri, COORDINATES_PREDICATE,
                    String.valueOf(poi.getCoordinates().getLatitude()) + ";" +
                            String.valueOf(poi.getCoordinates().getLongitude()));

            triples.put(typeTriple);
            triples.put(nameTriple);
            triples.put(coordinatesTriple);

            for (Map.Entry<String, String> entry : poi.getAdditionalInfo().entrySet()) {
                String infoPredicate = GETHERE_URL + "has" + entry.getKey() + "Info";
                JSONObject infoTriple = createTripleJsonObject(poiIri, infoPredicate, entry.getValue());
                triples.put(infoTriple);
            }

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
                HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider(TRIPLE_HOST);
                httpConnectionProvider.getConnection().setDoOutput(true);
                httpConnectionProvider.getConnection().setDoInput(true);
                httpConnectionProvider.getConnection().setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpConnectionProvider.getConnection().setRequestProperty("Accept", "application/json");
                httpConnectionProvider.getConnection().setRequestMethod("POST");

                JSONArray triples = createJsonArray();
                return httpConnectionProvider.sendPostHttpRequest(triples.toString().getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String message) {
            if (message.equals("OK")) {
                String successTitle = "Success";
                String successMessage = "The action has been executed successfully!";
                new SingleAlertDialog(successTitle, successMessage).displayAlertMessage(context);
            } else {
                String errorTitle = "Error";
                String errorMessage = "Sorry, something went wrong.";
                new SingleAlertDialog(errorTitle, errorMessage).displayAlertMessage(context);
            }
        }
    }
}
