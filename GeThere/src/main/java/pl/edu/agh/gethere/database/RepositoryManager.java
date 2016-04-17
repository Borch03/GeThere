package pl.edu.agh.gethere.database;

import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

import java.net.URL;

/**
 * Created by Dominik on 16.04.2016.
 */
public class RepositoryManager {

    private Repository repository;
    private RepositoryConfigurator repositoryConfigurator;

    public RepositoryManager() {
        repositoryConfigurator = new RepositoryConfigurator();
        repository = new HTTPRepository(repositoryConfigurator.getSesameServer(), repositoryConfigurator.getRepositoryID());
        repository.initialize();
    }

    public void addStatement(IRI subject, IRI predicate, IRI object) {
        try {
            RepositoryConnection connection = repository.getConnection();
            try {
                connection.add(subject, predicate, object);
            }
            finally {
                connection.close();
            }
        }
        catch (OpenRDFException e) {
            e.printStackTrace();
        }
    }
}
