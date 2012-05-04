package fr.fastconnect.eds;

import com.gigaspaces.document.SpaceDocument;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;
import java.util.Set;

/**
 * @author uri
 */
public class SpaceDocumentToDBObjectConverter implements Converter<SpaceDocument, DBObject> {
    public DBObject convert(SpaceDocument source) {
        BasicDBObject dbObject = new BasicDBObject();
        populate(source.getProperties(), dbObject);
        return dbObject;
    }

    private void populate(Map<String, Object> source, BasicDBObject target) {
        Set<String> fieldNames = source.keySet();
        for (String fieldName : fieldNames) {
            Object value = source.get(fieldName);
            if (value instanceof Map) {
                // if it's a map, act as an embeded object
                BasicDBObject subObject  = new BasicDBObject();
                populate((Map)value, subObject);
                target.put(fieldName, subObject);
            } else if(value instanceof SpaceDocument){
                // if it's a SpaceDocument, it's an embeded object
                DBObject subObject  = convert((SpaceDocument)value);
                target.put(fieldName, subObject);
            } else if(value != null) {
                // skip null fields
                target.put(fieldName, value);
            }
        }
    }
}



