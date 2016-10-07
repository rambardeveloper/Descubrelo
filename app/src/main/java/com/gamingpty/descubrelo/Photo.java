package com.gamingpty.descubrelo;


import java.io.Serializable;

public class Photo implements Serializable {
    String id, thumb, title, description, timestamp;

    public Photo() {
    }

    public Photo(String id, String thumb, String title, String description, String timestamp) {
        this.id = id;
        this.thumb = thumb;
        this.title = title;
        this.description = description;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}