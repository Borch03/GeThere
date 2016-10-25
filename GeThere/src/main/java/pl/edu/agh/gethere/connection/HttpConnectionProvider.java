package pl.edu.agh.gethere.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dominik on 20.10.2016.
 */
public class HttpConnectionProvider {

    private URL url;
    private HttpURLConnection connection;

    public HttpConnectionProvider(String url) throws IOException {
        this.url = new URL(url);
        this.connection = (HttpURLConnection) this.url.openConnection();
    }

    public String sendPostHttpRequest(byte[] buffer) throws IOException {
        OutputStream os = connection.getOutputStream();
        os.write(buffer);
        os.close();
        return getHttpResponde();
    }

    public String sendGetHttpRequest() throws IOException {
        return getHttpResponde();
    }

    private String getHttpResponde() throws IOException {
        String respond;
        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            respond = "" + sb.toString();
        } else {
            respond = connection.getResponseMessage();
        }
        return respond;
    }

    public HttpURLConnection getConnection() {
        return connection;
    }
}
