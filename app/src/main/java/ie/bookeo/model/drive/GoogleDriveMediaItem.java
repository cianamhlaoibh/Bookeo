package ie.bookeo.model.drive;

import android.os.Parcel;
import android.os.Parcelable;

import ie.bookeo.model.MediaItem;

public class GoogleDriveMediaItem extends MediaItem   {

    private String fileId;
    private String thumbnailLink;

    public GoogleDriveMediaItem() {
    }

    public GoogleDriveMediaItem(String name, String date, String fileId, String thumbnailLink) {
        super.setName(name);
        super.setDate(date);
        this.fileId = fileId;
        this.thumbnailLink = thumbnailLink;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

}
