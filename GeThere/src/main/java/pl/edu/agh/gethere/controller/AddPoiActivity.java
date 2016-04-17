package pl.edu.agh.gethere.controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.database.RepositoryManager;

public class AddPoiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_poi, menu);
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

    public void addPoiToRepository(View button) {

        final EditText poiNameField = (EditText) findViewById(R.id.EditTextPoiName);
        final EditText cityField = (EditText) findViewById(R.id.EditTextCity);
        final EditText streetField = (EditText) findViewById(R.id.EditTextStreet);
        final EditText numberField = (EditText) findViewById(R.id.EditTextNumber);
        final Spinner poiSpinner = (Spinner) findViewById(R.id.SpinnerPoiType);
        final EditText xCoordinateField = (EditText) findViewById(R.id.EditTextXCoordinate);
        final EditText yCoordinateField = (EditText) findViewById(R.id.EditTextYCoordinate);

        String poiName = poiNameField.getText().toString();
        String city = cityField.getText().toString();
        String street = streetField.getText().toString();
        String number = numberField.getText().toString();
        String poiType = poiSpinner.getSelectedItem().toString();
        String xCoordinate = xCoordinateField.getText().toString();
        String yCoordinate = yCoordinateField.getText().toString();

        String poiIRI = "POI-" + poiName + "-" + city + "-" + street + number;
        String poiTypeIRI = "http://gethere.agh.edu.pl/#" + poiType.replaceAll(" ", "-").toLowerCase();
        String coordinates = xCoordinate + ";" + yCoordinate;

        RepositoryManager repositoryManager = new RepositoryManager();
        try {
//            ValueFactory valueFactory = repositoryManager.getRepository().getValueFactory();
//            repositoryManager.addStatement(valueFactory.createIRI(poiIRI), RDF.VALUE ,valueFactory.createIRI(coordinates));
//            repositoryManager.addStatement(valueFactory.createIRI(poiIRI), RDF.TYPE ,valueFactory.createIRI(poiTypeIRI));
        } catch (RepositoryException e) {
            e.printStackTrace();
        } finally {
            repositoryManager.tearDown();
        }

    }
}
