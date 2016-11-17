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
public class AttributeAdapter extends ArrayAdapter<String> {

    private final Context context;
    private List<String> attributeList;

    public AttributeAdapter(Context context, List<String> attributeList) {
        super(context, R.layout.attribute_item, attributeList);
        this.context = context;
        this.attributeList = attributeList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.attribute_item, parent, false);
        EditText attribute = (EditText) rowView.findViewById(R.id.AttributeEditText);
        ImageButton xButton = (ImageButton) rowView.findViewById(R.id.XButton);
        attribute.setHint(attributeList.get(position));
        xButton.setContentDescription(attributeList.get(position));
        return rowView;
    }

    public List<String> getAttributeList() {
        return attributeList;
    }
}
