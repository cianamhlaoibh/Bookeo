package ie.bookeo.model.drive;

import android.os.Parcel;
import android.os.Parcelable;

import ie.bookeo.model.MediaItem;

public class GoogleDriveMediaItem extends MediaItem implements Parcelable {

    private String fileId;
    private String thumbnailLink;
    //private byte[] download;

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }



    public GoogleDriveMediaItem() {
    }

    public GoogleDriveMediaItem(String name, String date, String fileId, String thumbnailLink) {
        super.setName(name);
        super.setDate(date);
        this.fileId = fileId;
        this.thumbnailLink = thumbnailLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected GoogleDriveMediaItem(Parcel in) {
        fileId = in.readString();
        //download = in.createByteArray();
        super.setName(in.readString());
        super.setDate(in.readString());
        thumbnailLink = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileId);
        //dest.writeByteArray(this.download);
        dest.writeString(super.getName());
        dest.writeString(super.getDate());
        dest.writeString(this.thumbnailLink);
    }

    public static final Creator<GoogleDriveMediaItem> CREATOR = new Creator<GoogleDriveMediaItem>() {
        @Override
        public GoogleDriveMediaItem createFromParcel(Parcel in) {
            return new GoogleDriveMediaItem(in);
        }

        @Override
        public GoogleDriveMediaItem[] newArray(int size) {
            return new GoogleDriveMediaItem[size];
        }
    };

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    //public byte[] getDownload() {
      //  return download;
    //}

    //public void setDownload(byte[] download) {
       // this.download = download;
    //}

}
