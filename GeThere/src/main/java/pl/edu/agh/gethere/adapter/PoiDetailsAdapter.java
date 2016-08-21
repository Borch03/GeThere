package pl.edu.agh.gethere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.model.PoiDetail;

import java.util.List;

/**
 * Created by Dominik on 21.08.2016.
 */
public class PoiDetailsAdapter extends ArrayAdapter<PoiDetail> {
    private final Context context;
    private List<PoiDetail> poiData;

    public PoiDetailsAdapter(Context context, List<PoiDetail> poiData) {
        super(context, R.layout.poi_detail, poiData);
        this.context = context;
        this.poiData = poiData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.poi_detail, parent, false);
        TextView description = (TextView) rowView.findViewById(R.id.PoiDescriptionTextView);
        TextView value = (TextView) rowView.findViewById(R.id.PoiValueTextView);
        description.setText(poiData.get(position).getDescription());
        value.setText(poiData.get(position).getValue());
        return rowView;
    }
}