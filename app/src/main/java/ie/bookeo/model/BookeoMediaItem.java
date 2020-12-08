package ie.bookeo.model;

public class BookeoMediaItem {

    private String uuid;
    private String url;
    private String name;
    private String caption;
    private String date;
    private String albumUuid;

    public BookeoMediaItem() {
    }

    public BookeoMediaItem(String uuid, String url, String name, String caption, String date, String albumUuid) {
        this.uuid = uuid;
        this.url = url;
        this.name = name;
        this.caption = caption;
        this.date = date;
        this.albumUuid = albumUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAlbumUuid() {
        return albumUuid;
    }

    public void setAlbumUuid(String albumUuid) {
        this.albumUuid = albumUuid;
    }
}
