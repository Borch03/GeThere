package pl.edu.agh.gethere.controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import pl.edu.agh.gethere.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void goToEnterTargetActivity(View view) {
        Intent intent = new Intent(this, EnterTargetActivity.class);
        startActivity(intent);
    }

    public void goToFindPoiActivity(View view) {
        Intent intent = new Intent(this, FindPoiActivity.class);
        startActivity(intent);
    }

    public void goToAddPoiActivity(View view) {
        Intent intent = new Intent(this, AddPoiActivity.class);
        startActivity(intent);
    }

    public void goToViewMapActivity(View view) {
        Intent intent = new Intent(this, ViewMapActivity.class);
        startActivity(intent);
    }

}
