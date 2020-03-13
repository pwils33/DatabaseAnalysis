package models;

import java.util.List;

public class User {

    public String name;
    public UserId userId;
    public AccountId accountId;
    public List<Document> documents;

    public User(String name, UserId userId, AccountId accountId, List<Document> documents) {
        this.name = name;
        this.userId = userId;
        this.accountId = accountId;
        this.documents = documents;
    }
}
