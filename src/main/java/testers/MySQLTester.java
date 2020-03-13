package testers;

import doas.AccountDAO;
import doas.DocumentDAO;
import doas.UserDAO;
import mysql.daos.ConnectionWrapper;
import mysql.daos.MySQLAccountDAO;
import mysql.daos.MySQLDocumentDAO;
import mysql.daos.MySQLUserDAO;

public class MySQLTester extends AbstractTester {

    private ConnectionWrapper connectionWrapper;
    private MySQLAccountDAO accountDAO;
    private MySQLUserDAO userDAO;
    private MySQLDocumentDAO documentDAO;

    public MySQLTester() {
        connectionWrapper = new ConnectionWrapper();
        try {
            connectionWrapper.init();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        documentDAO = new MySQLDocumentDAO(connectionWrapper);
        userDAO = new MySQLUserDAO(connectionWrapper, documentDAO);
        accountDAO = new MySQLAccountDAO(userDAO);
    }

    @Override
    protected AccountDAO getAccountDAO() {
        return accountDAO;
    }

    @Override
    protected UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    protected DocumentDAO getDocumentDAO() {
        return documentDAO;
    }
}
