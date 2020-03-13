package redis;

import doas.DocumentDAO;
import models.Document;
import models.DocumentId;
import models.UserId;
import redis.clients.jedis.Jedis;

import java.util.*;

public class RedisDocumentDAO implements DocumentDAO {

    private JedisWrapper jedisWrapper;

    public RedisDocumentDAO(JedisWrapper jedisWrapper) {
        this.jedisWrapper = jedisWrapper;
    }

    public void createDocumentStorage() {}

    public void deleteDocumentStorage() {
        Jedis jedis = jedisWrapper.getJedis();
        String documentPattern = "document*";
        Set<String> keys = jedis.keys(documentPattern);
        for (String key: keys) {
            jedis.del(key);
        }
    }

    public Document getDocumentByDocumentId(DocumentId documentId) {
        Jedis jedis = jedisWrapper.getJedis();
        String key = "document_pages#" + documentId.id.toString();
        List<String> pages = jedis.lrange(key, 0, jedis.llen(key));
        return new Document(documentId, pages);
    }

    public List<Document> getDocumentsByDocumentIds(List<DocumentId> documentIds) {
        List<Document> documents = new ArrayList<>();
        for (DocumentId documentId: documentIds) {
            documents.add(getDocumentByDocumentId(documentId));
        }
        return documents;
    }

    public List<Document> getDocumentsByUserId(UserId userId) {
        Jedis jedis = jedisWrapper.getJedis();
        String key = "document_user_id#" + userId.id.toString();
        Set<String> documentIds = jedis.smembers(key);
        List<Document> documents = new ArrayList<>();
        for (String documentId: documentIds) {
            documents.add(getDocumentByDocumentId(new DocumentId(UUID.fromString(documentId))));
        }
        return documents;
    }

    public boolean insertDocument(UserId userId, Document document) {
        Jedis jedis = jedisWrapper.getJedis();
        jedis.sadd("document_user_id#" + userId.id.toString(), document.docId.id.toString());
        for (String page: document.pages) {
            jedis.lpush("document_pages#" + document.docId, page);
        }
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
