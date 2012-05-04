package fr.fastconnect.eds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.datasource.SQLDataProvider;
import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.SQLQuery;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;

public class MongoEDS implements BulkDataPersister, ManagedDataSource, DisposableBean, InitializingBean, SQLDataProvider {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private Mongo mongo;
    private String databaseName;
    //private MongoOperations mongoOperations;
    private DBCollection collection;
    
    private final SpaceDocumentToDBObjectConverter spaceDocumentToDBObjectConverter = new SpaceDocumentToDBObjectConverter();
    
    private final DBObjectToSpaceDocumentConverter dbObjectToSpaceDocumentConverter = new DBObjectToSpaceDocumentConverter();

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
        collection = mongo.getDB(databaseName).getCollection("eds");
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void afterPropertiesSet() {
        //mongoOperations = new MongoTemplate(mongo, databaseName);
    }

    protected boolean isConnected() {
        if (mongo == null)
            return false;
        List<String> dbs;
        try {
            dbs = mongo.getDatabaseNames();
        } catch (Exception e) {
            logger.error("Exception on getDatabaseNames, may be disconnected", e);
            mongo = null;
            return false;
        }
        return true;
    }

    public void init(Properties props) throws DataSourceException {
    }

    @Override
    public void executeBulk(List<BulkItem> bulkItems) throws DataSourceException {
        logger.debug("entering executeBulk()");
        if (isConnected()) {
            // we assume a (very) small context, normally
            Map<String, Object> context = new HashMap<String, Object>(5);

            for (BulkItem bulkItem : bulkItems) {
                SpaceDocument object = (SpaceDocument) bulkItem.getItem();

                switch (bulkItem.getOperation()) {
                    case BulkItem.WRITE:
                        logger.debug("BulkItem.WRITE called for " + object);
                        write(context, bulkItem);
                        break;
                    case BulkItem.UPDATE:
                    case BulkItem.PARTIAL_UPDATE:
                        logger.debug("BulkItem.UPDATE called for " + object);
                        update(context, bulkItem);
                        break;
                    case BulkItem.REMOVE:
                        logger.debug("BulkItem.REMOVE called for " + object);
                        remove(context, bulkItem);
                        break;
                    default:
                        logger.debug("unknown operation type " + bulkItem);
                        break;
                }
            }
        } else {
            logger.error("not connected");
        }
    }

    public void shutdown() throws DataSourceException {
        logger.debug("shutdown()");
    }

    protected void write(Map<String, Object> context, BulkItem item) {
        if (logger.isDebugEnabled()) {
            logger.debug("MongoEDS.executeBulk.write " + item);
        }
        
        DBObject dbObject = spaceDocumentToDBObjectConverter.convert((SpaceDocument)item.getItem());
        
        // set the id
        dbObject.put("_id", item.getIdPropertyValue());
        dbObject.put("_type", item.getTypeName());
        
        WriteResult s = collection.insert(dbObject);
        
        if (logger.isDebugEnabled()) {
            logger.debug("WriteResult: " + s);
        }
    }

    protected void update(Map<String, Object> context, BulkItem item) {
        if (logger.isDebugEnabled()) {
            logger.debug("MongoEDS.executeBulk.update " + item);
        }
        
        DBObject query = new BasicDBObject();
        query.put("_id", item.getIdPropertyValue());
        query.put("_type", item.getTypeName());

        DBObject dbObject = spaceDocumentToDBObjectConverter.convert((SpaceDocument)item.getItem());
        
        // set the id
        dbObject.put("_id", item.getIdPropertyValue());
        dbObject.put("_type", item.getTypeName());
        
        WriteResult s = collection.update(query, dbObject);
        if (logger.isDebugEnabled()) {
            logger.debug("UpdateResult: " + s);
        }
    }

    protected void remove(Map<String, Object> context, BulkItem item) {
        if (logger.isDebugEnabled()) {
            logger.debug("MongoEDS.executeBulk.remove " + item);
        }
        
        DBObject query = new BasicDBObject();
        query.put("_id", item.getIdPropertyValue());
        query.put("_type", item.getTypeName());
        
        WriteResult s = collection.remove(query);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Write result: " + s);
        }
    }

    public DataIterator initialLoad() throws DataSourceException {
        return new DataIterator() {
            DBCursor cursor;

            {
                cursor = collection.find(new BasicDBObject());
            }

            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }

            @Override
            public Object next() {
                DBObject dbObject = cursor.next();
                String id = dbObject.get("_id").toString();
                String type = dbObject.get("_type").toString();
                
                SpaceDocument document = dbObjectToSpaceDocumentConverter.convert(dbObject);
                
                document.setTypeName(type);
                document.setProperty("id", id);
                
                return document;
            }

            @Override
            public void remove() {
                cursor.remove();
            }
        };
    }

    @Override
    public void destroy() throws Exception {
        logger.debug("destroy()");
    }

    @Override
    public DataIterator iterator(SQLQuery sqlQuery) throws DataSourceException {
        logger.debug("iterator("+sqlQuery+") called");
        return null;
    }
}
