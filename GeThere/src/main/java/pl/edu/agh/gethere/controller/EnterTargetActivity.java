package pl.edu.agh.gethere.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.adapter.AttributeAdapter;
import pl.edu.agh.gethere.connection.HttpConnectionProvider;
import pl.edu.agh.gethere.connection.LocationProvider;
import pl.edu.agh.gethere.connection.RepositoryDefinitionsReceiver;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.model.ListOfPois;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.utils.JsonPoiParser;
import pl.edu.agh.gethere.utils.NonScrollableListView;
import pl.edu.agh.gethere.utils.SingleAlertDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EnterTargetActivity extends AppCompatActivity {

    private final static String EMULATOR_HOST = "http://10.0.2.2:9000/android/";
    private final static String HOST = "http://192.168.0.3:9000/android/";
    private final static String TYPE_HOST = HOST + "type";
    private final static String ATTRIBUTE_HOST = HOST + "attribute";
    private final static String FILTER_HOST = HOST + "filter";

    private final static String TYPE_SPINNER_TITLE = "Choose the type of POI";

    private Context context = this;
    private LocationProvider locationProvider = new LocationProvider();
    private List<String> attributeList;
    private AttributeAdapter attributeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_target);
        RepositoryDefinitionsReceiver definitionsReceiver = new RepositoryDefinitionsReceiver();

        Spinner typeSpinner = (Spinner) findViewById(R.id.ChooseTypeSpinner);
        typeSpinner.setPrompt(TYPE_SPINNER_TITLE);
        List<String> typesOfPoi = definitionsReceiver.createDefinitionList(TYPE_HOST);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, typesOfPoi);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        typeSpinner.setAdapter(spinnerAdapter);

        attributeList = definitionsReceiver.createDefinitionList(ATTRIBUTE_HOST);
        attributeAdapter = new AttributeAdapter(context, new ArrayList<String>());
        NonScrollableListView attributeListView = (NonScrollableListView) findViewById(R.id.AttributeFilterListView);
        attributeListView.setAdapter(attributeAdapter);

        final CheckBox openAtCheckBox = (CheckBox) findViewById(R.id.OpenAtCheckBox);
        openAtCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText openAt = (EditText) findViewById(R.id.OpenAtEditText);
                if (((CheckBox)v).isChecked()) {
                    openAt.setEnabled(true);
                } else {
                    openAt.setEnabled(false);
                }
            }
        });
        final CheckBox inRadiusCheckBox = (CheckBox) findViewById(R.id.InRadiusCheckBox);
        inRadiusCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText radius = (EditText) findViewById(R.id.RadiusEditText);
                if (((CheckBox)v).isChecked()) {
                    radius.setEnabled(true);
                } else {
                    radius.setEnabled(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_enter_target, menu);
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

    public void addAttributeFilter(View button) {
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

    public void applyTarget(View button) {

        final CheckBox openNowCheckBox= (CheckBox) findViewById(R.id.OpenNowCheckBox);
        final CheckBox openAtCheckBox = (CheckBox) findViewById(R.id.OpenAtCheckBox);
        final EditText openAtEditText = (EditText) findViewById(R.id.OpenAtEditText);
        final CheckBox inRadiusCheckBox = (CheckBox) findViewById(R.id.InRadiusCheckBox);
        final EditText radiusEditBox = (EditText) findViewById(R.id.RadiusEditText);
        final Spinner typeSpinner = (Spinner) findViewById(R.id.ChooseTypeSpinner);
        final NonScrollableListView attributeListView = (NonScrollableListView) findViewById(R.id.AttributeFilterListView);
        Map<String, String> filters = new HashMap<>();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
            if (openNowCheckBox.isChecked()) {
                Calendar calendar = Calendar.getInstance();
                String openTime = String.valueOf(dateFormat.parse(dateFormat.format(calendar.getTime())).getTime());
                filters.put("openTime", openTime);
            } else if (openAtCheckBox.isChecked()) {
                String openTime = openAtEditText.getText().toString();
                filters.put("openTime", String.valueOf(dateFormat.parse(openTime).getTime()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (inRadiusCheckBox.isChecked()) {
            String radius = radiusEditBox.getText().toString();
            filters.put("radius", radius);
            Coordinates location = getYourLocation();
            if (location != null) {
                filters.put("location", String.valueOf(location.getLatitude())+";"+String.valueOf(location.getLongitude()));
            }
        }
        String type = typeSpinner.getSelectedItem().toString();
        filters.put("type", type);
        for (int i = 0; i < attributeListView.getCount(); i++) {
            View view = attributeListView.getChildAt(i);
            EditText info = (EditText) view.findViewById(R.id.AttributeEditText);
            filters.put(info.getHint().toString(), info.getText().toString());
        }

        try {
            String response = new TargetRequestTask(filters).execute().get();
            JSONArray jsonPoiList = new JSONArray(response);
            JsonPoiParser jsonPoiParser = new JsonPoiParser(jsonPoiList);
            List<Poi> poiList = jsonPoiParser.parseJsonPoiList();
            if (poiList == null) {
                String title = "Not found";
                String message = "No POI found.";
                new SingleAlertDialog(title, message).displayAlertMessage(context);
            } else {
                ListOfPois listOfPois = new ListOfPois(poiList);
                Intent intent = new Intent(this, ListOfPoisActivity.class);
                intent.putExtra("listOfPois", listOfPois);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        clearFields();
    }

    private void clearFields() {
        final CheckBox openNowCheckBox= (CheckBox) findViewById(R.id.OpenNowCheckBox);
        final CheckBox openAtCheckBox = (CheckBox) findViewById(R.id.OpenAtCheckBox);
        final EditText openAtEditText = (EditText) findViewById(R.id.OpenAtEditText);
        final CheckBox inRadiusCheckBox = (CheckBox) findViewById(R.id.InRadiusCheckBox);
        final EditText radiusEditBox = (EditText) findViewById(R.id.RadiusEditText);

        openNowCheckBox.setChecked(false);
        openAtCheckBox.setChecked(false);
        openAtEditText.getText().clear();
        inRadiusCheckBox.setChecked(false);
        radiusEditBox.getText().clear();
        attributeList.addAll(attributeAdapter.getAttributeList());
        Collections.sort(attributeList.subList(1, attributeList.size()));
        attributeAdapter.clear();
    }

    private Coordinates getYourLocation() {
        return locationProvider.getLocation(context);
    }

    class TargetRequestTask extends AsyncTask<String, String, String> {

        private Map<String, String> filters;

        public TargetRequestTask(Map<String, String> filters) {
            this.filters = filters;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider(FILTER_HOST);
                httpConnectionProvider.getConnection().setDoOutput(true);
                httpConnectionProvider.getConnection().setDoInput(true);
                httpConnectionProvider.getConnection().setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpConnectionProvider.getConnection().setRequestMethod("POST");

                JSONObject filterJsonObject = new JSONObject();;
                for (Map.Entry<String, String> entry : filters.entrySet()) {
                    filterJsonObject.put(entry.getKey(), entry.getValue());
                }
                return httpConnectionProvider.sendPostHttpRequest(filterJsonObject.toString().getBytes("UTF-8"));
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
