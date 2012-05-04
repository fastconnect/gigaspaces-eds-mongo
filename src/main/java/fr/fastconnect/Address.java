package fr.fastconnect;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;

@SpaceClass
public class Address implements Serializable {
    private String street;
    
    private String city;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
