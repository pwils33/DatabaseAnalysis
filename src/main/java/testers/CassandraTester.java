package testers;

import cassandra.CassandraAccountDAO;
import cassandra.CassandraConnector;
import cassandra.CassandraDocumentDAO;
import cassandra.CassandraUserDAO;
import doas.AccountDAO;
import doas.DocumentDAO;
import doas.UserDAO;

public class CassandraTester extends AbstractTester{

    private CassandraAccountDAO accountDAO;
    private CassandraUserDAO userDAO;
    private CassandraDocumentDAO documentDAO;

    public CassandraTester() {
        CassandraConnector connector = new CassandraConnector();
        this.documentDAO = new CassandraDocumentDAO(connector);
        this.userDAO = new CassandraUserDAO(connector, documentDAO);
        this.accountDAO = new CassandraAccountDAO(userDAO);
    }

    protected AccountDAO getAccountDAO() {
        return accountDAO;
    }

    protected UserDAO getUserDAO() {
        return userDAO;
    }

    protected DocumentDAO getDocumentDAO() {
        return documentDAO;
    }
}
