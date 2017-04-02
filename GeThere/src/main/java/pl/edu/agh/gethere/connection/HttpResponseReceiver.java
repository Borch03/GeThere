package pl.edu.agh.gethere.connection;

import android.os.AsyncTask;

/**
 * Created by Dominik on 02.04.2017.
 */
public class HttpResponseReceiver extends AsyncTask<String, Void, String> {

    private String host;

    public HttpResponseReceiver(String host) {
        this.host = host;
    }

    @Override
    protected String doInBackground(String[] params) {
        try {
            HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider(host);
            httpConnectionProvider.getConnection().setRequestMethod("GET");
            return httpConnectionProvider.sendGetHttpRequest();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
    }
}
