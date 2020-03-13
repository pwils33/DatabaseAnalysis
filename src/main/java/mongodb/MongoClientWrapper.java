package mongodb;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;

public class MongoClientWrapper {

    private MongoClient client;

    public MongoClientWrapper() {
        client = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
    }

    protected DBCollection getCollection() {
        return client.getDB("Product").getCollection("mainCollection");
    }

}
