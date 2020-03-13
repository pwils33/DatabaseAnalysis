package doas;

import models.Document;
import models.DocumentId;
import models.UserId;

import java.util.List;
import java.util.Map;

public interface DocumentDAO {

    void createDocumentStorage();

    void deleteDocumentStorage();

    Document getDocumentByDocumentId(DocumentId documentId);

    List<Document> getDocumentsByDocumentIds(List<DocumentId> documentIds);

    List<Document> getDocumentsByUserId(UserId userId);

    boolean insertDocument(UserId userId, Document document);

    boolean insertDocuments(Map<UserId, List<Document>> documents);
}
