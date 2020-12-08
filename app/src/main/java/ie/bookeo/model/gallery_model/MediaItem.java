package ie.bookeo.model.gallery_model;


/**
 *
 * This is the model class for storing information about an media item (video / image).
 * In later iterations it will be used to upload images to storage and for storing data about images from cloud.
 *
 */

public class MediaItem {

    private String name;
    private String path;
    private String size;
    private String uri;
    private String date;
    private Boolean selected = false;
    private Boolean isSelected;

    public MediaItem(){

    }

    public MediaItem(String name, String path, String size, String uri) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.uri = uri;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
