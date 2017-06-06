package pl.edu.agh.gethere.controller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import org.json.JSONArray;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.connection.HttpConnectionProvider;
import pl.edu.agh.gethere.model.ListOfPois;
import pl.edu.agh.gethere.model.Poi;
import pl.edu.agh.gethere.utils.JsonPoiParser;
import pl.edu.agh.gethere.utils.SingleAlertDialog;

import java.util.List;

public class FindPoiActivity extends AppCompatActivity {

    public final static String EMULATOR_HOST = "http://10.0.2.2:9000/android/";
    public final static String HOST = "http://192.168.43.5:9000/android/";
    public final static String KEYWORD_HOST = HOST + "keyword";

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_poi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_poi, menu);
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

    public void searchKeyword(View button) {

        final EditText keywordField = (EditText) findViewById(R.id.EditTextKeyword);
        String keyword = keywordField.getText().toString();

        try {
            String response = new KeywordRequestTask(keyword).execute().get();
            JSONArray jsonPoiList = new JSONArray(response);
            JsonPoiParser jsonPoiParser = new JsonPoiParser(jsonPoiList);
            List<Poi> poiList = jsonPoiParser.parseJsonPoiList();
            if (poiList == null) {
                String title = "Not found";
                String message = "No POI found.";
                new SingleAlertDialog(title, message).displayAlertMessage(context);
            }
            ListOfPois listOfPois = new ListOfPois(poiList);
            Intent intent = new Intent(this, ListOfPoisActivity.class);
            intent.putExtra("listOfPois", listOfPois);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class KeywordRequestTask extends AsyncTask<String, String, String> {

        private String keyword;

        public KeywordRequestTask(String keyword) {
            this.keyword = keyword;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider(KEYWORD_HOST);
                httpConnectionProvider.getConnection().setDoOutput(true);
                httpConnectionProvider.getConnection().setDoInput(true);
                httpConnectionProvider.getConnection().setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                httpConnectionProvider.getConnection().setRequestMethod("POST");

                return httpConnectionProvider.sendPostHttpRequest(keyword.getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if ((result == null) || result.equals("Not Found")) {
                String errorTitle = "Error";
                String errorMessage = "Sorry, something went wrong.";
                new SingleAlertDialog(errorTitle, errorMessage).displayAlertMessage(context);
            }
            super.onPostExecute(result);
        }

    }

}
