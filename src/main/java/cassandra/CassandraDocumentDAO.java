package cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import doas.DocumentDAO;
import models.*;

import java.util.*;

public class CassandraDocumentDAO implements DocumentDAO {

    private CassandraConnector connector;
    final private String connectorId = "documentDAO";

    public CassandraDocumentDAO(CassandraConnector connector) {
        this.connector = connector;
    }

    public void createDocumentStorage() {
        final String methodId = connectorId + " createDocumentStorage";
        final String createDocumentsQuery =
                "CREATE TABLE IF NOT EXISTS dev.selectDocumentsByDocumentId(" +
                        "document_id uuid primary key, " +
                        "pages list<text>" +
                        ");";
        final String createDocumentIdsQuery =
                "CREATE TABLE IF NOT EXISTS dev.selectDocumentIdsByUserId(" +
                        "user_id uuid primary key, " +
                        "document_ids set<uuid>" +
                        ");";
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(createDocumentsQuery);
        session.execute(createDocumentIdsQuery);
        connector.close(methodId);
    }

    public void deleteDocumentStorage() {
        final String methodId = connectorId + " deleteDocumentStorage";
        final String dropDocumentsQuery = "DROP TABLE IF EXISTS dev.selectDocumentsByDocumentId;";
        final String dropDocumentIdsQuery = "DROP TABLE IF EXISTS dev.selectDocumentIdsByUserId;";
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(dropDocumentsQuery);
        session.execute(dropDocumentIdsQuery);
        connector.close(methodId);
    }

    public Document getDocumentByDocumentId(DocumentId documentId) {
        final String methodId = connectorId + " getDocumentsByDocumentId";
        final String selectQuery = "SELECT pages FROM dev.selectDocumentsByDocumentId WHERE document_id=" + documentId.id;
        connector.connect(methodId);
        ResultSet rs = connector.getSession().execute(selectQuery);
        List<String> pages = rs.one().getList("pages", String.class);
        connector.close(methodId);
        return new Document(documentId, pages);
    }

    public List<Document> getDocumentsByDocumentIds(List<DocumentId> documentIds) {
        final String methodId = connectorId + " getDocumentsByDocumentIds";
        final String selectQuery = "SELECT document_id, pages FROM dev.selectDocumentsByDocumentId WHERE document_id IN" + makeResourceIdsString(documentIds);
        connector.connect(methodId);
        ResultSet rs = connector.getSession().execute(selectQuery);
        List<Document> documents = new ArrayList<>();
        for (Row row: rs.all()) {
            DocumentId documentId = new DocumentId(row.getUuid("document_id"));
            List<String> pages = row.getList("pages", String.class);
            documents.add(new Document(documentId, pages));
        }
        connector.close(methodId);
        return documents;
    }

    public List<Document> getDocumentsByUserId(UserId userId) {
        final String methodId = connectorId + " getDocumentsByUserId";
        final String selectQuery = "SELECT document_ids FROM dev.selectDocumentIdsByUserId WHERE user_id=" + userId.id;
        connector.connect(methodId);
        ResultSet rs = connector.getSession().execute(selectQuery);
        Set<UUID> documentUUIDs = rs.one().getSet("document_ids", UUID.class);
        List<DocumentId> documentIds = new ArrayList<>();
        for (UUID documentUUID: documentUUIDs) {
            documentIds.add(new DocumentId(documentUUID));
        }
        List<Document> result = getDocumentsByDocumentIds(documentIds);
        connector.close(methodId);
        return result;
    }

    private <K extends ResourceId> String makeResourceIdsString(List<K> resourceIds) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < resourceIds.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(resourceIds.get(i).id);
        }
        sb.append(")");
        return sb.toString();
    }

    public Map<UserId, List<Document>> getDocumentsByUserIds(List<UserId> userIds) {
        final String methodId = connectorId + " getDocumentsByUserIds";
        final String selectQuery =
                "SELECT user_id, document_ids FROM dev.selectDocumentIdsByUserId WHERE user_id IN" + makeResourceIdsString(userIds);
        Map<UserId, List<DocumentId>> userIdDocumentIdMap = new HashMap<>();
        connector.connect(methodId);
        ResultSet rs = connector.getSession().execute(selectQuery);
        for (Row row: rs.all()) {
            UserId userId = new UserId(row.getUuid("user_id"));
            Set<UUID> documentUUIDs = row.getSet("document_ids", UUID.class);
            List<DocumentId> documentIds = new ArrayList<>();
            if (documentUUIDs != null) {
                for (UUID documentUUID: documentUUIDs) {
                    documentIds.add(new DocumentId(documentUUID));
                }
            }
            userIdDocumentIdMap.put(userId, documentIds);
        }
        Map<UserId, List<Document>> userIdDocumentsMap = new HashMap<>();
        for (UserId userId: userIdDocumentIdMap.keySet()) {
            userIdDocumentsMap.put(userId, getDocumentsByDocumentIds(userIdDocumentIdMap.get(userId)));
        }
        connector.close(methodId);
        return userIdDocumentsMap;
    }

    public boolean insertDocument(UserId userId, Document document) {
        final String methodId = connectorId + " insertDocument";
        final String insertDocumentIdQuery =
                "UPDATE dev.selectDocumentIdsByUserId " +
                        "SET document_ids = document_ids + {" + document.docId.id + "} " +
                        "WHERE user_id = " + userId.id + ";";
        final String insertDocumentQuery =
                "INSERT INTO dev.selectDocumentsByDocumentId (document_id, pages) " +
                        "VALUES (" + document.docId.id + ", ?);";
        Statement stmt = SimpleStatement.newInstance(insertDocumentQuery, document.pages);
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(insertDocumentIdQuery);
        session.execute(stmt);
        connector.close(methodId);
        return true;
    }

    public boolean insertDocuments(Map<UserId, List<Document>> documents) {
        for (UserId userId : documents.keySet()) {
            for (Document document: documents.get(userId)) {
                insertDocument(userId, document);
            }
        }
        return true;
    }

    private String makeString(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("'").append(strings.get(i)).append("'");
        }
        return sb.toString();
    }

    private String getInsertDocumentQuery(Document document) {
        return "INSERT INTO dev.selectDocumentsByDocumentId (document_id, pages) " +
                "VALUES (" + document.docId.id + ", [" + makeString(document.pages) + "]);";
    }

    public void insertDocumentOnly(Document document) {
        final String methodId = connectorId + " insertDocumentOnly";
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(getInsertDocumentQuery(document));
        connector.close(methodId);
    }

    public void insertDocumentsOnly(List<Document> documents) {
        final String methodId = connectorId + " insertDocumentsOnly";
        StringBuilder batchQuery = new StringBuilder("BEGIN BATCH ");
        for (Document document: documents) {
            batchQuery.append(getInsertDocumentQuery(document));
        }
        batchQuery.append("APPLY BATCH");
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(batchQuery.toString());
        connector.close(methodId);
    }

    private String getDocIdsSetString(List<Document> documents) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(documents.get(i).docId.id);
        }
        return sb.toString();
    }

    private String getInsertDocumentIdsQuery(UserId userId, List<Document> documents) {
        return "INSERT INTO dev.selectDocumentIdsByUserId (user_id, document_ids) " +
                "VALUES (" + userId.id + ", { " + getDocIdsSetString(documents) + " });";
    }

    public void insertDocumentsByBatch(Map<UserId, List<Document>> documents) {
        final String methodId = connectorId + " insertDocumentsByBatch";
        StringBuilder batchQuery = new StringBuilder("BEGIN BATCH ");
        List<Document> allDocuments = new ArrayList<>();
        for (UserId userId: documents.keySet()) {
            batchQuery.append(getInsertDocumentIdsQuery(userId, documents.get(userId)));
            allDocuments.addAll(documents.get(userId));
        }
        batchQuery.append("APPLY BATCH");
        connector.connect(methodId);
        CqlSession session = connector.getSession();
        session.execute(batchQuery.toString());
        insertDocumentsOnly(allDocuments);
        connector.close(methodId);
    }
}
