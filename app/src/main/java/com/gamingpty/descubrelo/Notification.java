package com.gamingpty.descubrelo;


import java.io.Serializable;

public class Notification implements Serializable {
    String id, thumb, title, body, timestamp;

    public Notification() {
    }

    public Notification(String id, String thumb, String title, String body, String timestamp) {
        this.id = id;
        this.thumb = thumb;
        this.title = title;
        this.body = body;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
