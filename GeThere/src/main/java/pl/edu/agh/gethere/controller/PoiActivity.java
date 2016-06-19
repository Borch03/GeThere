package pl.edu.agh.gethere.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.model.Poi;

/**
 * Created by Dominik on 19.06.2016.
 */
public class PoiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_pois);

        Intent intent = getIntent();
        Poi poi = (Poi) intent.getSerializableExtra("poi");

        TextView poiNameField = (TextView) findViewById(R.id.PoiNameTextView);
        TextView poiTypeField = (TextView) findViewById(R.id.PoiTypeTextView);
        TextView poiCityField = (TextView) findViewById(R.id.PoiCityTextView);
        TextView poiStreetField = (TextView) findViewById(R.id.PoiStreetTextView);
        TextView poiLatitudeField = (TextView) findViewById(R.id.LatitudeTextView);
        TextView poiLongitudeField = (TextView) findViewById(R.id.LongitudeTextView);

        poiNameField.setText(poi.getName());
        poiTypeField.setText(poi.getType());
        poiCityField.setText(poi.getCity());
        poiStreetField.setText(poi.getStreet() + " " + poi.getNumber());
        poiLatitudeField.setText(String.valueOf(poi.getCoordinates().getLatitude()));
        poiLongitudeField.setText(String.valueOf(poi.getCoordinates().getLongitude()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_of_pois, menu);
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

    public void navigate(View button) {

    }
}
