package cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import doas.UserDAO;
import models.*;

import java.util.*;

public class CassandraUserDAO implements UserDAO {

    private CassandraConnector connector;
    private CassandraDocumentDAO documentDAO;

    private String connectorId = "userDAO";

    public CassandraUserDAO(CassandraConnector connector, CassandraDocumentDAO documentDAO) {
        this.connector = connector;
        this.documentDAO = documentDAO;
    }

    public void createUserStorage() {
        final String methodId = connectorId + "createUserStorage";
        final String createUserTable =
                "CREATE TABLE IF NOT EXISTS dev.selectUsersByUserId(" +
                        "user_id uuid PRIMARY KEY, " +
                        "account_id uuid, " +
                        "name text" +
                        ");";
        final String createUserIdsTable =
                "CREATE TABLE IF NOT EXISTS dev.selectUserIdsByAccountId(" +
                        "account_id uuid PRIMARY KEY, " +
                        "user_ids set<uuid>" +
                        ");";
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(createUserTable);
        session.execute(createUserIdsTable);
        connector.close(methodId);
    }

    public void deleteUserStorage() {
        final String methodId = connectorId + " deleteUserStorage";
        final String dropUserTable = "DROP TABLE IF EXISTS dev.selectUsersByUserId;";
        final String dropUserIdsTable = "DROP TABLE IF EXISTS dev.selectUserIdsByAccountId;";
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(dropUserTable);
        session.execute(dropUserIdsTable);
        connector.close(methodId);
    }

    public User getUserByUserId(UserId userId) {
        final String methodId = connectorId + " getUserByUserId";
        final String selectUserQuery = "SELECT name, account_id FROM dev.selectUsersByUserId WHERE user_id = " + userId.id;
        connector.connect(methodId);
        Row row = connector.getSession().execute(selectUserQuery).one();
        String name = row.getString("name");
        AccountId accountId = new AccountId(row.getUuid("account_id"));
        List<Document> documents = documentDAO.getDocumentsByUserId(userId);
        connector.close(methodId);
        return new User(name, userId, accountId, documents);
    }

    public List<User> getUsersByUserIds(List<UserId> userIds) {
        final String methodId = connectorId + " getUsersByUserIds";
        final String selectUsersQuery =
                "SELECT name, user_id, account_id FROM dev.selectUsersByUserId WHERE user_id IN " + makeResourceIdsString(userIds);
        ArrayList<User> users = new ArrayList<>();
        connector.connect(methodId);
        ResultSet rs = connector.getSession().execute(selectUsersQuery);
        Map<UserId, String> userIdNameMap = new HashMap<>();
        Map<UserId, AccountId> userIdAccountIdMap = new HashMap<>();
        for (Row row: rs.all()) {
            UserId userId = new UserId(row.getUuid("user_id"));
            userIdNameMap.put(userId, row.getString("name"));
            userIdAccountIdMap.put(userId, new AccountId(row.getUuid("account_id")));
        }
        Map<UserId, List<Document>> userIdDocumentsMap = documentDAO.getDocumentsByUserIds(userIds);
        for (UserId userId: userIds) {
            String name = userIdNameMap.get(userId);
            AccountId accountId = userIdAccountIdMap.get(userId);
            List<Document> documents = userIdDocumentsMap.get(userId);
            users.add(new User(name, userId, accountId, documents));
        }
        connector.close(methodId);
        return users;
    }

    public List<User> getUsersByAccountId(AccountId accountId) {
        final String methodId = connectorId + " getUsersByAccountId";
        final String selectUserIdsQuery = "SELECT user_ids FROM dev.selectUserIdsByAccountId WHERE account_id = " + accountId.id;
        connector.connect(methodId);
        Row row = connector.getSession().execute(selectUserIdsQuery).one();
        Set<UUID> userUUIDs = row.getSet("user_ids", UUID.class);
        List<UserId> userIds = new ArrayList<>();
        for (UUID userUUID: userUUIDs) {
            userIds.add(new UserId(userUUID));
        }
        List<User> result = getUsersByUserIds(userIds);
        connector.close(methodId);
        return result;
    }

    private <K extends ResourceId> String makeResourceIdsString(List<K> resourceIds) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < resourceIds.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(resourceIds.get(i).id);
        }
        sb.append(")");
        return sb.toString();
    }

    public List<User> getUsersByAccountIds(List<AccountId> accountIds) {
        final String methodId = connectorId + " getUsersByAccountIds";
        final String selectUserIdsQuery =
                "SELECT user_ids FROM dev.selectUserIdsByAccountId WHERE account_id IN " + makeResourceIdsString(accountIds);
        connector.connect(methodId);
        ResultSet rs = connector.getSession().execute(selectUserIdsQuery);
        List<UserId> userIds = new ArrayList<>();
        for (Row row: rs.all()) {
            Set<UUID> userUUIDs = row.getSet("user_ids", UUID.class);
            for (UUID userUUID: userUUIDs) {
                userIds.add(new UserId(userUUID));
            }
        }
        List<User> result = getUsersByUserIds(userIds);
        connector.close(methodId);
        return result;
    }

    public boolean insertUser(User user) {
        final String methodId = connectorId + "insertUser";
        final String insertUserIdQuery =
                "UPDATE dev.selectUserIdsByAccountId " +
                        "SET user_ids = user_ids + {" + user.userId.id + "} " +
                        "WHERE account_id = " + user.accountId.id + ";";
        final String insertUserQuery =
                "INSERT INTO dev.selectUsersByUserId(user_id, account_id, name) " +
                        "VALUES (" + user.userId.id + ", " + user.accountId.id + ", '" + user.name +"');";
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(insertUserIdQuery);
        session.execute(insertUserQuery);
        Map<UserId, List<Document>> docs = new HashMap<>();
        docs.put(user.userId, user.documents);
        documentDAO.insertDocuments(docs);
        connector.close(methodId);
        return true;
    }

    public boolean insertUsers(List<User> users) {
        for (User user: users) {
            insertUser(user);
        }
        return true;
    }

    private String getInsertUserQuery(User user) {
        return "INSERT INTO dev.selectUsersByUserId(user_id, account_id, name) " +
                "VALUES (" + user.userId.id + ", " + user.accountId.id + ", '" + user.name +"');";
    }

    public void insertUserOnly(User user) {
        final String methodId = connectorId + "insertUserOnly";
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(getInsertUserQuery(user));
        Map<UserId, List<Document>> docs = new HashMap<>();
        docs.put(user.userId, user.documents);
        documentDAO.insertDocumentsByBatch(docs);
        connector.close(methodId);
    }

    public void insertUsersOnly(List<User> users) {
        final String methodId = connectorId + " insertUsersOnly";
        StringBuilder batchQuery = new StringBuilder("BEGIN BATCH ");
        Map<UserId, List<Document>> docs = new HashMap<>();
        for (User user: users) {
            batchQuery.append(getInsertUserQuery(user));
            docs.put(user.userId, user.documents);
        }
        batchQuery.append("APPLY BATCH");
        connector.connect(methodId);
        connector.getSession().execute(batchQuery.toString());
        documentDAO.insertDocumentsByBatch(docs);
        connector.close(methodId);
    }

    private String makeUUIDString(List<UserId> userIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < userIds.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(" ").append(userIds.get(i).id);
        }
        return sb.toString();
    }

    private String getInsertUserIdsQuery(AccountId accountId, List<User> users) {
        List<UserId> userIds = new ArrayList<>(users.size());
        for (User user: users) {
            userIds.add(user.userId);
        }
        return "INSERT INTO dev.selectUserIdsByAccountId (account_id, user_ids) " +
                "VALUES (" + accountId.id + ", {" + makeUUIDString(userIds) + " });";
    }

    public void insertUsersByAccountId(AccountId accountId, List<User> users) {
        final String methodId = connectorId + " insertUsersByAccountId";
        final String insertUserIdsQuery = getInsertUserIdsQuery(accountId, users);
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(insertUserIdsQuery);
        insertUsersOnly(users);
        connector.close(methodId);
    }

    public void insertUsersByBatch(List<Account> accounts) {
        final String methodId = connectorId + " insertUsersByBatch";
        StringBuilder batchQuery = new StringBuilder("BEGIN BATCH ");
        List<User> users = new ArrayList<>();
        for (Account account: accounts) {
            batchQuery.append(getInsertUserIdsQuery(account.accountId, account.users));
            users.addAll(account.users);
        }
        batchQuery.append("APPLY BATCH");
        connector.connect(methodId);
        connector.getSession().execute(batchQuery.toString());
        insertUsersOnly(users);
        connector.close(methodId);
    }
}
