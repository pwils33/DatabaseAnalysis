package testers;

import doas.AccountDAO;
import doas.DocumentDAO;
import doas.UserDAO;
import mongodb.MongoAccountDAO;
import mongodb.MongoClientWrapper;
import mongodb.MongoDocumentDAO;
import mongodb.MongoUserDAO;

public class MongoTester extends AbstractTester {

    private MongoAccountDAO accountDAO;
    private MongoUserDAO userDAO;
    private MongoDocumentDAO documentDAO;

    public MongoTester() {
        MongoClientWrapper clientWrapper = new MongoClientWrapper();
        documentDAO = new MongoDocumentDAO(clientWrapper);
        userDAO = new MongoUserDAO(clientWrapper, documentDAO);
        accountDAO = new MongoAccountDAO(userDAO);
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
