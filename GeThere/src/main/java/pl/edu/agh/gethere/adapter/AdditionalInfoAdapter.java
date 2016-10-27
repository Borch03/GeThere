package pl.edu.agh.gethere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import pl.edu.agh.gethere.R;

import java.util.List;

/**
 * Created by Dominik on 23.10.2016.
 */
public class AdditionalInfoAdapter extends ArrayAdapter<String> {

    private final Context context;
    private List<String> additionalInfoList;

    public AdditionalInfoAdapter(Context context, List<String> additionalInfoList) {
        super(context, R.layout.additional_info_item, additionalInfoList);
        this.context = context;
        this.additionalInfoList = additionalInfoList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.additional_info_item, parent, false);
        EditText additionalInfo = (EditText) rowView.findViewById(R.id.AdditionalInfoEditText);
        ImageButton xButton = (ImageButton) rowView.findViewById(R.id.XButton);
        additionalInfo.setHint(additionalInfoList.get(position));
        xButton.setContentDescription(additionalInfoList.get(position));
        return rowView;
    }

    public List<String> getAdditionalInfoList() {
        return additionalInfoList;
    }
}
