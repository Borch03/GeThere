package pl.edu.agh.gethere.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.*;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.model.ListOfPois;
import pl.edu.agh.gethere.model.Poi;

import java.util.List;

/**
 * Created by Dominik on 19.06.2016.
 */
public class ListOfPoisActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_pois);

        ListView poiListView = (ListView) findViewById(R.id.PoiListView);

        Intent intent = getIntent();
        final ListOfPois listOfPois = (ListOfPois) intent.getSerializableExtra("listOfPois");

        poiListView.setAdapter(new PoiListAdapter(this, R.layout.poi, listOfPois.getPoiList()));
        poiListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, Poi.class);
        intent.putExtra("poi", (Poi) adapterView.getSelectedItem());
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

    private class PoiListAdapter extends ArrayAdapter<Poi> {
        private int layout;
        private PoiListAdapter(Context context, int resource, List<Poi> poiList) {
            super(context, resource, poiList);
            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewHolder = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.PoiNameTextView);
                viewHolder.address = (TextView) convertView.findViewById(R.id.poi_address);
                convertView.setTag(viewHolder);
            } else {
                mainViewHolder = (ViewHolder) convertView.getTag();
                mainViewHolder.name.setText(getItem(position).getName());
                mainViewHolder.address.setText(getItem(position).getCity() + ", " +
                        getItem(position).getStreet() + " " + getItem(position).getNumber());
            }
            return convertView;
        }
    }

    public class ViewHolder {

        TextView name;
        TextView address;
    }
}
