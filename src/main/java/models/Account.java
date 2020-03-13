package models;

import java.util.List;
import java.util.UUID;

public class Account {

    public AccountId accountId;
    public List<User> users;

    public Account(AccountId accountId, List<User> users) {
        this.accountId = accountId;
        this.users = users;
    }
}
