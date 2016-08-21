package pl.edu.agh.gethere.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.ListView;
import android.widget.TextView;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.adapter.PoiDetailsAdapter;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.model.PoiDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 19.06.2016.
 */
public class PoiActivity extends AppCompatActivity {

    public final static String POI_TYPE_DESCRIPTION = "Type: ";
    public final static String POI_CITY_DESCRIPTION = "City: ";
    public final static String POI_ADDRESS_DESCRIPTION = "Address: ";

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

    private List<PoiDetail> createPoiDataList(Poi poi) {
        List<PoiDetail> poiDataList = new ArrayList<>();
        poiDataList.add(new PoiDetail(POI_TYPE_DESCRIPTION, poi.getType().replace(AddPoiActivity.GETHERE_URL, "")));
        poiDataList.add(new PoiDetail(POI_CITY_DESCRIPTION, poi.getCity()));
        poiDataList.add(new PoiDetail(POI_ADDRESS_DESCRIPTION, poi.getStreet().concat(" ").concat(poi.getNumber())));

        return poiDataList;
    }

    public void navigate(View button) {

    }
}
