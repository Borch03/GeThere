package pl.edu.agh.gethere.database;

import org.openrdf.OpenRDFException;
import org.openrdf.model.IRI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.util.Connections;

import java.net.URL;

/**
 * Created by Dominik on 16.04.2016.
 */
public class RepositoryManager {

    private Repository repository;
    private RepositoryConnection connection;
    private RepositoryConfigurator repositoryConfigurator;

    public RepositoryManager() {
        this.repositoryConfigurator = new RepositoryConfigurator();
        this.repository = new HTTPRepository(repositoryConfigurator.getSesameServer(), repositoryConfigurator.getRepositoryID());
        this.repository.initialize();
        this.connection = repository.getConnection();
    }

    public void tearDown() throws RepositoryException {
        this.connection.close();
        this.repository.shutDown();
    }

    public void addStatement(IRI subject, IRI predicate, IRI object) {
        connection.add(subject, predicate, object);
    }

    public Repository getRepository() {
        return this.repository;
    }
}
