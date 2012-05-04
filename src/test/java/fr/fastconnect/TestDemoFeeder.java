package fr.fastconnect;

import java.util.UUID;

import junit.framework.Assert;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

import fr.fastconnect.Address;
import fr.fastconnect.People;

public class TestDemoFeeder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProcessingUnitContainer pu;

    private GigaSpace gigaspace;

    public void startContainers() {
        logger.info("----------------------------------\nStarting containers");
        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.addConfigLocation(new ClassPathResource("/pu-test.xml"));
        // ClusterInfo ci = new ClusterInfo();
        // ci.setNumberOfInstances(1);
        // ci.setNumberOfBackups(1);
        // ci.setSchema("partitioned-sync2backup");
        // provider.setClusterInfo(ci);
        pu = provider.createContainer();
        logger.info("----------------------------------\nStarted containers");

        UrlSpaceConfigurer configurer = new UrlSpaceConfigurer("jini://*/*/space");
        gigaspace = new GigaSpaceConfigurer(configurer).gigaSpace();

        // Create type descriptor:
        SpaceTypeDescriptor typeDescriptor = new SpaceTypeDescriptorBuilder(People.class.getName()).idProperty("id").routingProperty("id").create();
        // Register type:
        gigaspace.getTypeManager().registerTypeDescriptor(typeDescriptor);
    }

    @After
    public void stopContainers() {
        logger.info("----------------------------------\nStopping containers");
        if (pu != null) {
            pu.close();
        }
        logger.info("----------------------------------\nStopped containers");
    }

    @Test
    public void testReadPojo() throws Exception {
        // we read from the space without writing in it before, but it should be
        // not empty because mongo EDS contains data

        Mongo mongo = new Mongo();
        mongo.dropDatabase("gigaspaces");
        DBCollection collection = mongo.getDB("gigaspaces").getCollection("eds");

        // { "_id" : "5ea1129e-dbda-4e48-b804-22f1a587967d", "firstname" :
        // "Mathias", "id" : "5ea1129e-dbda-4e48-b804-22f1a587967d", "_type" :
        // "org.grozeille.People" }
        DBObject data = new BasicDBObject();
        ObjectId id = new ObjectId();
        data.put("_id", id);
        data.put("id", id.toString());
        data.put("firstname", "Mathias");
        data.put("_type", "org.grozeille.People");

        collection.insert(WriteConcern.FSYNC_SAFE, data);

        startContainers();

        People result[] = gigaspace.readMultiple(new People(), 10);

        Assert.assertFalse(result.length == 0);

        for (People p : result) {
            logger.debug("{} {} {}", new Object[] { p.getId(), p.getFirstname(), p.getLastname() });
        }
    }

    @Test
    public void testReadSpaceDocument() throws Exception {
        // we read from the space without writing in it before, but it should be
        // not empty because mongo EDS contains data

        Mongo mongo = new Mongo();
        mongo.dropDatabase("gigaspaces");
        DBCollection collection = mongo.getDB("gigaspaces").getCollection("eds");

        // { "_id" : "5ea1129e-dbda-4e48-b804-22f1a587967d", "firstname" :
        // "Mathias", "id" : "5ea1129e-dbda-4e48-b804-22f1a587967d", "_type" :
        // "org.grozeille.People" }
        DBObject data = new BasicDBObject();
        ObjectId id = new ObjectId();
        data.put("_id", id);
        data.put("id", id.toString());
        data.put("firstname", "Mathias");
        data.put("_type", "org.grozeille.People");

        collection.insert(WriteConcern.FSYNC_SAFE, data);

        startContainers();

        SpaceDocument result[] = gigaspace.readMultiple(new SpaceDocument(People.class.getName()), 10);

        Assert.assertFalse(result.length == 0);

        for (SpaceDocument d : result) {
            logger.debug("{} {} {}", new Object[] { d.getProperty("id"), d.getProperty("firstname"), d.getProperty("lastname") });
        }
    }

    @Test
    public void testWritePojo() throws Exception {

        Mongo mongo = new Mongo();
        mongo.dropDatabase("gigaspaces");

        startContainers();

        People p = new People();
        Address a = new Address();
        a.setCity("Issy les Moulineaux");
        a.setStreet("32 Rue d'Erevan");
        p.setAddress(a);
        p.setFirstname("Mathias");
        p.setLastname("Kluba");

        gigaspace.write(p);
        
        DBCollection collection = mongo.getDB("gigaspaces").getCollection("eds");

        Assert.assertFalse(collection.count() == 0);
    }

    @Test
    public void testWriteSpaceDocument() throws Exception {

        Mongo mongo = new Mongo();
        mongo.dropDatabase("gigaspaces");

        startContainers();

        SpaceDocument document = new SpaceDocument();
        document.setTypeName(People.class.getName());
        document.setProperty("firstname", "Mathias");
        document.setProperty("id", UUID.randomUUID().toString());

        gigaspace.write(document);

        DBCollection collection = mongo.getDB("gigaspaces").getCollection("eds");

        Assert.assertFalse(collection.count() == 0);
    }
    
    @Test
    public void testFullSpaceDocument() throws Exception {

        Mongo mongo = new Mongo();
        mongo.dropDatabase("gigaspaces");

        startContainers();

        SpaceDocument document = new SpaceDocument();
        document.setTypeName(People.class.getName());
        document.setProperty("firstname", "Mathias");
        document.setProperty("id", UUID.randomUUID().toString());

        gigaspace.write(document);

        DBCollection collection = mongo.getDB("gigaspaces").getCollection("eds");

        Assert.assertFalse(collection.count() == 0);
        
        SpaceDocument result[] = gigaspace.readMultiple(new SpaceDocument(People.class.getName()), 10);

        Assert.assertFalse(result.length == 0);

        for (SpaceDocument d : result) {
            logger.debug("{} {} {}", new Object[] { d.getProperty("id"), d.getProperty("firstname"), d.getProperty("lastname") });
        }
    }
}
