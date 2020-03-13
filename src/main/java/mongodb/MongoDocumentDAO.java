package mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import doas.DocumentDAO;
import models.Document;
import models.DocumentId;
import models.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MongoDocumentDAO implements DocumentDAO {

    private MongoClientWrapper clientWrapper;

    public MongoDocumentDAO(MongoClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    public void createDocumentStorage() {
        this.clientWrapper.getCollection();
    }

    public void deleteDocumentStorage() {
        clientWrapper.getCollection().remove(new BasicDBObject("document_id", new BasicDBObject("$exists", true)));
    }

    public Document getDocumentByDocumentId(DocumentId documentId) {
        DBCursor docCursor = this.clientWrapper.getCollection().find(new BasicDBObject("document_id", documentId.id.toString()));
        return DocumentDBObjectAdapter.adaptDBObjectToDocument(docCursor.one());
    }

    public List<Document> getDocumentsByDocumentIds(List<DocumentId> documentIds) {
        List<Document> documents = new ArrayList<>();
        for (DocumentId documentId: documentIds) {
            documents.add(getDocumentByDocumentId(documentId));
        }
        return documents;
    }

    public List<Document> getDocumentsByUserId(UserId userId) {
        DBObject query = new BasicDBObject("user_id", userId.id.toString());
        query.put("document_id", new BasicDBObject("$exists", true));
        DBCursor docCursor = clientWrapper.getCollection().find(query);
        List<Document> documents = new ArrayList<>();
        while (docCursor.hasNext()) {
            documents.add(DocumentDBObjectAdapter.adaptDBObjectToDocument(docCursor.next()));
        }
        return documents;
    }

    public boolean insertDocument(UserId userId, Document document) {
        clientWrapper.getCollection().insert(DocumentDBObjectAdapter.adaptDocumentToDBOjbect(document, userId));
        return true;
    }

    public boolean insertDocuments(Map<UserId, List<Document>> documents) {
        for (UserId userId: documents.keySet()) {
            for (Document document: documents.get(userId)) {
                insertDocument(userId, document);
            }
        }
        return true;
    }
}
