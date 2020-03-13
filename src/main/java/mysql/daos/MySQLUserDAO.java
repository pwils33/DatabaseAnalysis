package mysql.daos;

import doas.UserDAO;
import models.AccountId;
import models.Document;
import models.User;
import models.UserId;

import java.sql.*;
import java.util.*;

public class MySQLUserDAO implements UserDAO {

    private MySQLDocumentDAO documentDAO;
    private ConnectionWrapper connectionWrapper;

    public MySQLUserDAO(ConnectionWrapper connectionWrapper, MySQLDocumentDAO documentDAO) {
        this.connectionWrapper = connectionWrapper;
        this.documentDAO = documentDAO;
    }

    public void createUserStorage() {
        final String createUserTableQuery =
                "CREATE TABLE users (" +
                        "user_id TEXT NOT NULL," +
                        "account_id TEXT NOT NULL," +
                        "name VARCHAR(30) NOT NULL" +
                        ");";
        try {
            connectionWrapper.getStatement(createUserTableQuery).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
    }

    public void deleteUserStorage() {
        final String dropUserTableQuery = "DROP TABLE IF EXISTS users;";
        try {
            connectionWrapper.getStatement(dropUserTableQuery).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
    }

    public User getUserByUserId(UserId userId) {
        final String selectQuery = "SELECT account_id, name FROM users WHERE user_id=?";
        try {
            connectionWrapper.getStatement(selectQuery).setString(1, userId.id.toString());
            ResultSet rs = connectionWrapper.updateAndGetResultSet();
            rs.next();
            String name = rs.getString("name");
            AccountId accountId = new AccountId(UUID.fromString(rs.getString("account_id")));
            return new User(name, userId, accountId, documentDAO.getDocumentsByUserId(userId));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
        return null;
    }

    public List<User> getUsersByUserIds(List<UserId> userIds) {
        ArrayList<User> users = new ArrayList<>();
        for (UserId userId: userIds) {
            users.add(getUserByUserId(userId));
        }
        return users;
    }

    public List<User> getUsersByAccountId(AccountId accountId) {
        final String selectQuery = "SELECT user_id, name FROM users WHERE account_id=?";
        Map<UserId, String> userIdNameMap = new HashMap<>();
        try {
            connectionWrapper.getStatement(selectQuery).setString(1, accountId.id.toString());
            ResultSet rs = connectionWrapper.updateAndGetResultSet();
//            ArrayList<User> users = new ArrayList<>();
            while (rs.next()) {
                String name = rs.getString("name");
                UserId userId = new UserId(UUID.fromString(rs.getString("user_id")));
//                users.add(new User(name, userId, accountId, documentDAO.getDocumentsByUserId(userId)));
                userIdNameMap.put(userId, name);
            }
//            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
        List<User> users = new ArrayList<>();
        for (UserId userId: userIdNameMap.keySet()) {
            users.add(new User(userIdNameMap.get(userId), userId, accountId, documentDAO.getDocumentsByUserId(userId)));
        }
        return users;
    }

    public List<User> getUsersByAccountIds(List<AccountId> accountIds) {
        ArrayList<User> users = new ArrayList<>();
        for (AccountId accountId: accountIds) {
            users.addAll(getUsersByAccountId(accountId));
        }
        return users;
    }

    public boolean insertUser(User user) {
        Map<UserId, List<Document>> docs = new HashMap<>();
        docs.put(user.userId, user.documents);
        documentDAO.insertDocuments(docs);
        final String insertUserQuery = "INSERT INTO users VALUES (?,?,?)";
        try {
            PreparedStatement stmt = connectionWrapper.getStatement(insertUserQuery);
            stmt.setString(1, user.userId.id.toString());
            stmt.setString(2, user.accountId.id.toString());
            stmt.setString(3, user.name);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
        return false;
    }

    public boolean insertUsers(List<User> users) {
        Map<UserId, List<Document>> docs = new HashMap<>();
        for (User user: users) {
            docs.put(user.userId, user.documents);
        }
        documentDAO.insertDocuments(docs);
        final String insertUserQuery = "INSERT INTO users VALUES (?,?,?)";
        try {
            PreparedStatement stmt = connectionWrapper.getStatement(insertUserQuery);
            for (User user: users) {
                stmt.setString(1, user.userId.id.toString());
                stmt.setString(2, user.accountId.id.toString());
                stmt.setString(3, user.name);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionWrapper.close();
        }
        return false;
    }
}
