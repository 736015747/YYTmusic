package list;

import java.io.Serializable;

public class Song implements Serializable {
    private String id;
    private String name;
    private String uri;
    private String downUri;
    private String author;
    private String ablum;
    private int playUri;

    public int getPlayUri() {
        return playUri;
    }

    public void setPlayUri(int playUri) {
        this.playUri = playUri;
    }

    public String getAuthor() {
        return author;
    }

    public String getId() {
        return id;
    }

    public Song(String id, String name, String uri, String downUri, String author, String ablum, int playUri) {
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.downUri = downUri;
        this.author = author;
        this.ablum = ablum;
        this.playUri = playUri;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAblum() {
        return ablum;
    }

    public void setAblum(String ablum) {
        this.ablum = ablum;
    }

    public String getName() {
        return name;
    }

    public String getDownUri() {
        return downUri;
    }

    public void setDownUri(String downUri) {
        this.downUri = downUri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
