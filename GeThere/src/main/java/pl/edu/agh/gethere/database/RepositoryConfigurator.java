package pl.edu.agh.gethere.database;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Dominik on 16.04.2016.
 */
public class RepositoryConfigurator {

    private String sesameServer;
    private String repositoryID;

    public RepositoryConfigurator() {
        Properties prop = new Properties();
        try {
            prop.load(new FileReader("src/release/res/sesame/sesame.properties"));
            sesameServer = prop.getProperty("sesameServer");
            repositoryID = prop.getProperty("repositoryID");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSesameServer() {
        return sesameServer;
    }

    public String getRepositoryID() {
        return repositoryID;
    }
}
