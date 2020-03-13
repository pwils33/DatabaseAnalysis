package redis;

import doas.UserDAO;
import models.AccountId;
import models.Document;
import models.User;
import models.UserId;
import redis.clients.jedis.Jedis;

import java.util.*;

public class RedisUserDAO implements UserDAO {

    private JedisWrapper jedisWrapper;
    private RedisDocumentDAO documentDAO;

    public RedisUserDAO(JedisWrapper jedisWrapper, RedisDocumentDAO documentDAO) {
        this.jedisWrapper = jedisWrapper;
        this.documentDAO = documentDAO;
    }

    public void createUserStorage() {}

    public void deleteUserStorage() {
        Jedis jedis = jedisWrapper.getJedis();
        String documentPattern = "user*";
        Set<String> keys = jedis.keys(documentPattern);
        for (String key: keys) {
            jedis.del(key);
        }
    }

    public User getUserByUserId(UserId userId) {
        Jedis jedis = jedisWrapper.getJedis();
        String name = jedis.hget("user#" + userId.id.toString(), "name");
        AccountId accountId = new AccountId(UUID.fromString(jedis.hget("user#" + userId.id.toString(), "account_id")));
        List<Document> documents = documentDAO.getDocumentsByUserId(userId);
        return new User(name, userId, accountId, documents);
    }

    public List<User> getUsersByUserIds(List<UserId> userIds) {
        List<User> users = new ArrayList<>();
        for (UserId userId: userIds) {
            users.add(getUserByUserId(userId));
        }
        return users;
    }

    public List<User> getUsersByAccountId(AccountId accountId) {
        Jedis jedis = jedisWrapper.getJedis();
        String key = "user_account_id#" + accountId.id.toString();
        Set<String> userIds = jedis.smembers(key);
        List<User> users = new ArrayList<>();
        for (String userId: userIds) {
            users.add(getUserByUserId(new UserId(UUID.fromString(userId))));
        }
        return users;
    }

    public List<User> getUsersByAccountIds(List<AccountId> accountIds) {
        List<User> users = new ArrayList<>();
        for (AccountId accountId: accountIds) {
            users.addAll(getUsersByAccountId(accountId));
        }
        return users;
    }

    public boolean insertUser(User user) {
        Jedis jedis = jedisWrapper.getJedis();
        String key = "user#" + user.userId.id.toString();
        jedis.hset(key, "name", user.name);
        jedis.hset(key, "account_id", user.accountId.id.toString());
        jedis.sadd("user_account_id#" + user.accountId.id.toString(), user.userId.id.toString());
        Map<UserId, List<Document>> documents = new HashMap<>();
        documents.put(user.userId, user.documents);
        documentDAO.insertDocuments(documents);
        return true;
    }

    public boolean insertUsers(List<User> users) {
        for (User user: users) {
            insertUser(user);
        }
        return true;
    }
}
