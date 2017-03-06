package pl.edu.agh.gethere.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONObject;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.adapter.PoiDetailsAdapter;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.model.PoiDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dominik on 19.06.2016.
 */
public class PoiActivity extends AppCompatActivity {

    public final static String POI_TYPE_DESCRIPTION = "Type: ";
    public final static String OPENING_HOURS_DESCRIPTION = "Opening Hours: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);

        Intent intent = getIntent();
        final Poi poi = (Poi) intent.getSerializableExtra("poi");

        TextView poiNameField = (TextView) findViewById(R.id.PoiNameTextView);

        TextView poiLatitudeField = (TextView) findViewById(R.id.LatitudeTextView);
        TextView poiLongitudeField = (TextView) findViewById(R.id.LongitudeTextView);

        poiNameField.setText(poi.getName());

        ListView poiListView = (ListView) findViewById(R.id.PoiDataList);
        poiListView.setAdapter(new PoiDetailsAdapter(this, createPoiDataList(poi)));

        poiLatitudeField.setText(String.valueOf(poi.getCoordinates().getLatitude()));
        poiLongitudeField.setText(String.valueOf(poi.getCoordinates().getLongitude()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_poi, menu);
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

    private List<PoiDetails> createPoiDataList(Poi poi) {
        List<PoiDetails> poiDataList = new ArrayList<>();
        poiDataList.add(new PoiDetails(POI_TYPE_DESCRIPTION, poi.getType()));
        if (poi.getOpeningHours() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
            String openingHour = dateFormat.format(poi.getOpeningHours().getOpeningHour());
            String closingHour = dateFormat.format(poi.getOpeningHours().getClosingHour());
            poiDataList.add(new PoiDetails(OPENING_HOURS_DESCRIPTION, openingHour + " - " + closingHour));
        }

        for (Map.Entry<String, String> entry : poi.getAttributes().entrySet()) {
            poiDataList.add(new PoiDetails(entry.getKey().replaceAll("(^has)|(Info$)", "").concat(":"), entry.getValue()));
        }

        return poiDataList;
    }

    public void navigate(View button) {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }
}
