package pl.edu.agh.gethere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.model.Poi;

import java.util.List;

/**
 * Created by Dominik on 21.08.2016.
 */

public class PoiListAdapter extends ArrayAdapter<Poi> {

    private final Context context;
    private List<Poi> poiList;

    public PoiListAdapter(Context context, List<Poi> poiList) {
        super(context, R.layout.poi, poiList);
        this.context = context;
        this.poiList = poiList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.poi, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.PoiNameTextView);
        TextView address = (TextView) rowView.findViewById(R.id.PoiAddressTextView);
        name.setText(poiList.get(position).getName());
        address.setText(poiList.get(position).getCity() + ", " +
                poiList.get(position).getStreet() + " " + poiList.get(position).getNumber());
        return rowView;
    }
}