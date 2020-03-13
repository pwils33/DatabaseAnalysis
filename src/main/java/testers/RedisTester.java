package testers;

import doas.AccountDAO;
import doas.DocumentDAO;
import doas.UserDAO;
import redis.JedisWrapper;
import redis.RedisAccountDAO;
import redis.RedisDocumentDAO;
import redis.RedisUserDAO;

public class RedisTester extends AbstractTester{

    private RedisAccountDAO accountDAO;
    private RedisUserDAO userDAO;
    private RedisDocumentDAO documentDAO;

    public RedisTester() {
        JedisWrapper jedisWrapper = new JedisWrapper();
        documentDAO = new RedisDocumentDAO(jedisWrapper);
        userDAO = new RedisUserDAO(jedisWrapper, documentDAO);
        accountDAO = new RedisAccountDAO(userDAO);
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
