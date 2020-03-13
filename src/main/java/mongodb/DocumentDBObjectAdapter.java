package mongodb;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import models.Document;
import models.DocumentId;
import models.UserId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DocumentDBObjectAdapter {

    public static DBObject adaptDocumentToDBOjbect(Document document, UserId userId) {
        return new BasicDBObject("document_id", document.docId.id.toString())
                .append("user_id", userId.id.toString())
                .append("pages", document.pages.toArray());
    }

    public static Document adaptDBObjectToDocument(DBObject documentDBObject) {
        String docId = (String)documentDBObject.get("document_id");
        BasicDBList dbList = (BasicDBList) documentDBObject.get("pages");
        List<String> pages = new ArrayList<>();
        for (Object el: dbList) {
            pages.add((String) el);
        }
        return new Document(new DocumentId(UUID.fromString(docId)), pages);
    }

}
