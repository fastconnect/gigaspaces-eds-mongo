package fr.fastconnect;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import com.gigaspaces.metadata.SpaceDocumentSupport;

@SpaceClass
public class People implements Serializable  {
    
    private String id;
    
    private String firstname;
    
    private String lastname;
    
    private Address address;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @SpaceProperty(documentSupport = SpaceDocumentSupport.CONVERT)
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @SpaceId(autoGenerate=true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
