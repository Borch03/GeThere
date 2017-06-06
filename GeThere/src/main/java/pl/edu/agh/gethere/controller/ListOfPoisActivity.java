package pl.edu.agh.gethere.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.adapter.PoiListAdapter;
import pl.edu.agh.gethere.model.ListOfPois;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.utils.NonScrollableListView;

/**
 * Created by Dominik on 19.06.2016.
 */
public class ListOfPoisActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_pois);

        NonScrollableListView poiListView = (NonScrollableListView) findViewById(R.id.PoiListView);

        Intent intent = getIntent();
        final ListOfPois listOfPois = (ListOfPois) intent.getSerializableExtra("listOfPois");

        poiListView.setAdapter(new PoiListAdapter(this, listOfPois.getPoiList()));
        poiListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, PoiActivity.class);
        intent.putExtra("poi", (Poi) adapterView.getItemAtPosition(position));
        startActivity(intent);
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
}
