package mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import doas.UserDAO;
import models.AccountId;
import models.Document;
import models.User;
import models.UserId;

import java.util.*;

public class MongoUserDAO implements UserDAO {

    private MongoClientWrapper clientWrapper;
    private MongoDocumentDAO documentDAO;

    public MongoUserDAO(MongoClientWrapper clientWrapper, MongoDocumentDAO documentDAO) {
        this.clientWrapper = clientWrapper;
        this.documentDAO = documentDAO;
    }

    public void createUserStorage() {
        this.clientWrapper.getCollection();
    }

    public void deleteUserStorage() {
        DBObject query = new BasicDBObject("user_id", new BasicDBObject("$exists", true));
        appendDocumentQualifier(query);
        clientWrapper.getCollection().remove(query);
    }

    private void appendDocumentQualifier(DBObject query) {
        query.put("document_id", new BasicDBObject("$exists", false));
    }

    public User getUserByUserId(UserId userId) {
        DBObject query = new BasicDBObject("user_id", userId.id.toString());
        appendDocumentQualifier(query);
        DBCursor userCursor = clientWrapper.getCollection().find(query);
        List<Document> documents = this.documentDAO.getDocumentsByUserId(userId);
        return UserDBObjectAdapter.adaptDBObjectToUser(userCursor.one(), documents);
    }

    public List<User> getUsersByUserIds(List<UserId> userIds) {
        List<User> users = new ArrayList<>();
        for (UserId userId: userIds) {
            users.add(getUserByUserId(userId));
        }
        return users;
    }

    public List<User> getUsersByAccountId(AccountId accountId) {
        List<User> users = new ArrayList<>();
        DBObject query = new BasicDBObject("account_id", accountId.id.toString());
        DBCursor userCursor = clientWrapper.getCollection().find(query);
        while (userCursor.hasNext()) {
            DBObject next = userCursor.next();
            UserId userId = new UserId(UUID.fromString((String) next.get("user_id")));
            List<Document> documents = documentDAO.getDocumentsByUserId(userId);
            users.add(UserDBObjectAdapter.adaptDBObjectToUser(next, documents));
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
        clientWrapper.getCollection().insert(UserDBObjectAdapter.adaptUserToDBObject(user));
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
