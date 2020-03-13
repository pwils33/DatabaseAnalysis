package doas;

import models.AccountId;
import models.User;
import models.UserId;

import java.util.List;

public interface UserDAO {

    void createUserStorage();

    void deleteUserStorage();

    User getUserByUserId(UserId userId);

    List<User> getUsersByUserIds(List<UserId> userIds);

    List<User> getUsersByAccountId(AccountId accountId);

    List<User> getUsersByAccountIds(List<AccountId> accountIds);

    boolean insertUser(User user);

    boolean insertUsers(List<User> users);

}
