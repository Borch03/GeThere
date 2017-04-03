package pl.edu.agh.gethere.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.adapter.AttributeAdapter;
import pl.edu.agh.gethere.connection.HttpConnectionProvider;
import pl.edu.agh.gethere.connection.LocationProvider;
import pl.edu.agh.gethere.connection.RepositoryDefinitionsReceiver;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.model.OpeningHours;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.utils.SingleAlertDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddPoiActivity extends AppCompatActivity {

    public final static String EMULATOR_HOST = "http://10.0.2.2:9000/android/";
    public final static String HOST = "http://localhost:9000/android/";
    public final static String ATTRIBUTE_HOST = EMULATOR_HOST + "attribute";
    public final static String TYPE_HOST = EMULATOR_HOST + "type";
    public final static String TRIPLE_HOST = EMULATOR_HOST + "triple";
    public final static String GETHERE_URL = "http://gethere.agh.edu.pl/#";

    public final static String TYPE_PREDICATE = GETHERE_URL + "isTypeOf";
    public final static String NAME_PREDICATE = GETHERE_URL + "hasName";
    public final static String COORDINATES_PREDICATE = GETHERE_URL + "hasCoordinates";
    public final static String OPENING_HOURS_PREDICATE = GETHERE_URL + "hasOpeningHours";

    private Context context = this;
    private LocationProvider locationProvider = new LocationProvider();
    private List<String> attributeList;
    private AttributeAdapter attributeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        RepositoryDefinitionsReceiver definitionsReceiver = new RepositoryDefinitionsReceiver();

        Spinner typeSpinner = (Spinner) findViewById(R.id.PoiTypeSpinner);
        List<String> typesOfPoi = definitionsReceiver.createDefinitionList(TYPE_HOST);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, typesOfPoi);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        typeSpinner.setAdapter(spinnerAdapter);

        final CheckBox openingHoursCheckBox = (CheckBox) findViewById(R.id.OpeningHoursCheckBox);
        openingHoursCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText openingHourEditText = (EditText) findViewById(R.id.OpeningHourEditText);
                EditText closingHourEditText = (EditText) findViewById(R.id.ClosingHourEditText);
                if (((CheckBox)v).isChecked()) {
                    openingHourEditText.setVisibility(View.VISIBLE);
                    closingHourEditText.setVisibility(View.VISIBLE);
                } else {
                    openingHourEditText.setVisibility(View.GONE);
                    closingHourEditText.setVisibility(View.GONE);
                }
            }
        });
        attributeList = definitionsReceiver.createDefinitionList(ATTRIBUTE_HOST);
        attributeAdapter = new AttributeAdapter(context, new ArrayList<String>());
        ListView attributeListView = (ListView) findViewById(R.id.AttributeList);
        attributeListView.setAdapter(attributeAdapter);
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
        final EditText latitudeEditText = (EditText) findViewById(R.id.LatitudeEditText);
        final EditText longitudeEditText = (EditText) findViewById(R.id.LongitudeEditText);
        Coordinates coordinates = locationProvider.getLocation(context);
        latitudeEditText.setText(String.valueOf(coordinates.getLatitude()));
        longitudeEditText.setText(String.valueOf(coordinates.getLongitude()));
    }

    public void addAttribute(View button) {
        final String[] attributeArray = attributeList.toArray(new String[attributeList.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose the kind of attribute:");
        builder.setItems(attributeArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                attributeAdapter.add(attributeArray[item]);
                attributeList.remove(item);
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

    public void removeAttribute(View button) {
        attributeAdapter.remove(button.getContentDescription().toString());
        attributeList.add(button.getContentDescription().toString());
        Collections.sort(attributeList.subList(1, attributeList.size()));
    }

    public void addPoiToRepository(View button) {

        final EditText poiNameField = (EditText) findViewById(R.id.PoiNameEditText);
        final Spinner typeSpinner = (Spinner) findViewById(R.id.PoiTypeSpinner);
        final EditText latitudeField = (EditText) findViewById(R.id.LatitudeEditText);
        final EditText longitudeField = (EditText) findViewById(R.id.LongitudeEditText);
        final EditText openingHourEditText = (EditText) findViewById(R.id.OpeningHourEditText);
        final EditText closingHourEditText = (EditText) findViewById(R.id.ClosingHourEditText);
        final ListView attributeListView = (ListView) findViewById(R.id.AttributeList);
        final CheckBox openingHoursCheckBox = (CheckBox) findViewById(R.id.OpeningHoursCheckBox);

        String name = poiNameField.getText().toString();
        String type = typeSpinner.getSelectedItem().toString();
        double latitude = Double.valueOf(latitudeField.getText().toString());
        double longitude = Double.valueOf(longitudeField.getText().toString());
        OpeningHours openingHours = null;
        if (openingHoursCheckBox.isChecked()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
            String openingHourString = openingHourEditText.getText().toString();
            String closingHourString = closingHourEditText.getText().toString();
            try {
                Date openingHour = dateFormat.parse(openingHourString);
                Date closingHour = dateFormat.parse(closingHourString);
                if (openingHour.after(closingHour)) {
                    closingHour = new Date(closingHour.getTime() + 24*3600*1000);
                }
                openingHours = new OpeningHours(openingHour, closingHour);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        HashMap<String, String> attributes = new HashMap<>();
        for (int i = 0; i < attributeListView.getCount(); i++) {
            View view = attributeListView.getChildAt(i);
            EditText info = (EditText) view.findViewById(R.id.AttributeEditText);
            attributes.put(info.getHint().toString(), info.getText().toString());
        }

        String id = UUID.randomUUID().toString();
        Poi poi = new Poi(id, name, type, new Coordinates(latitude, longitude), openingHours, attributes);

        new PoiSender(poi).execute();

        clearFields();
    }

    private void clearFields() {
        final EditText poiNameField = (EditText) findViewById(R.id.PoiNameEditText);
        final EditText latitudeField = (EditText) findViewById(R.id.LatitudeEditText);
        final EditText longitudeField = (EditText) findViewById(R.id.LongitudeEditText);
        final EditText openingHourEditText = (EditText) findViewById(R.id.OpeningHourEditText);
        final EditText closingHourEditText = (EditText) findViewById(R.id.ClosingHourEditText);
        final CheckBox openingHoursCheckBox = (CheckBox) findViewById(R.id.OpeningHoursCheckBox);

        poiNameField.getText().clear();
        latitudeField.getText().clear();
        longitudeField.getText().clear();
        openingHoursCheckBox.setChecked(false);
        openingHourEditText.getText().clear();
        closingHourEditText.getText().clear();
        openingHourEditText.setVisibility(View.GONE);
        closingHourEditText.setVisibility(View.GONE);
        attributeList.addAll(attributeAdapter.getAttributeList());
        Collections.sort(attributeList.subList(1, attributeList.size()));
        attributeAdapter.clear();
    }

    private class PoiSender extends AsyncTask <String, Void, String> {

        private Poi poi;

        PoiSender(Poi poi) {
            this.poi = poi;
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
            if (message.equals("")) {
                String successTitle = "Success";
                String successMessage = "The action has been executed successfully!";
                new SingleAlertDialog(successTitle, successMessage).displayAlertMessage(context);
            } else {
                String errorTitle = "Error";
                String errorMessage = "Sorry, something went wrong.";
                new SingleAlertDialog(errorTitle, errorMessage).displayAlertMessage(context);
            }
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
            JSONObject openingHoursTriple = null;
            if (poi.getOpeningHours() != null) {
                openingHoursTriple = createTripleJsonObject(poiIri, OPENING_HOURS_PREDICATE,
                        String.valueOf(poi.getOpeningHours().getOpeningHour().getTime()) + ";" +
                                String.valueOf(poi.getOpeningHours().getClosingHour().getTime()));
            }
            triples.put(typeTriple);
            triples.put(nameTriple);
            triples.put(coordinatesTriple);
            if (poi.getOpeningHours() != null) {
                triples.put(openingHoursTriple);
            }
            for (Map.Entry<String, String> entry : poi.getAttributes().entrySet()) {
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
    }
}
