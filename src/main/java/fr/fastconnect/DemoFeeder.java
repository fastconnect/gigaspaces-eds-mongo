package fr.fastconnect;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class DemoFeeder implements InitializingBean, DisposableBean {

    private final Logger log= LoggerFactory.getLogger(this.getClass());
    
    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> sf;
    
    private long defaultDelay = 1000;
    
    @GigaSpaceContext
    private GigaSpace gigaSpace;
    
    public void destroy() throws Exception {
        log.info("Stop scheduler");
        sf.cancel(false);
        sf = null;
        executorService.shutdown();
    }

    public void afterPropertiesSet() throws Exception {
        
        log.info("Start scheduler");
        Runnable task = new Runnable() {
            
            public void run() {
                
                log.info("Write");
                try {
                    People people = new People();
                    people.setFirstname("Mathias");
                    people.setLastname("Kluba");
                    Address address = new Address();
                    address.setCity("Issy les moulineaux");
                    address.setStreet("32 rue d'Erevan");
                    people.setAddress(address);
                    
                    gigaSpace.write(people);

                } catch (Exception e) {
                    log.info(e.toString());
                }
            }
        };
        
        executorService = Executors.newScheduledThreadPool(1);
        
        sf = executorService.scheduleAtFixedRate(task, defaultDelay, defaultDelay,
                TimeUnit.MILLISECONDS);
    }

}
