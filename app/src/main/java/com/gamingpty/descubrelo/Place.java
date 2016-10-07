package com.gamingpty.descubrelo;


import java.io.Serializable;

public class Place implements Serializable {
    String id, thumb, name, address, timestamp;

    public Place() {
    }

    public Place(String id, String thumb, String name, String address, String timestamp) {
        this.id = id;
        this.thumb = thumb;
        this.name = name;
        this.address = address;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
