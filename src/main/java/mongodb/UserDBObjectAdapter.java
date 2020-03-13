package mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import models.AccountId;
import models.Document;
import models.User;
import models.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDBObjectAdapter {

    public static DBObject adaptUserToDBObject(User user) {
        return new BasicDBObject("user_id", user.userId.id.toString())
                .append("account_id", user.accountId.id.toString())
                .append("name", user.name);
    }

    public static User adaptDBObjectToUser(DBObject userDBObject, List<Document> documents) {
        UserId userId = new UserId(UUID.fromString((String)userDBObject.get("user_id")));
        String name = (String)userDBObject.get("name");
        AccountId accountId = new AccountId(UUID.fromString((String)userDBObject.get("account_id")));
        return new User(name, userId, accountId, documents);
    }
}
