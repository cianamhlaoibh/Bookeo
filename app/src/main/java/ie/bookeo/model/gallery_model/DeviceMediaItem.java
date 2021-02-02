package ie.bookeo.model.gallery_model;

import ie.bookeo.model.MediaItem;

/**
 *
 * This is the model class for storing information about an media item (video / image).
 * In later iterations it will be used to upload images to storage and for storing data about images from cloud.
 *
 */

public class DeviceMediaItem extends MediaItem {

    private String path;

    public DeviceMediaItem(){ }
    public DeviceMediaItem(String name, String path, String date) {
        super.setName(name);
        super.setDate(date);
        this.path = path;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
