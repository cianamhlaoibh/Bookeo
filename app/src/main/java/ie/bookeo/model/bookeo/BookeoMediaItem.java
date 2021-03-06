package ie.bookeo.model.bookeo;

import android.os.Parcel;
import android.os.Parcelable;

import ie.bookeo.model.MediaItem;

public class BookeoMediaItem extends MediaItem {

    private String uuid;
    private String url;
    private String caption;
    private String albumUuid;

    public BookeoMediaItem() {
    }

    public BookeoMediaItem(String uuid, String url, String name, String date, String albumUuid) {
        this.uuid = uuid;
        this.url = url;
        super.setName(name);
        super.setDate(date);
        this.albumUuid = albumUuid;
    }

    public BookeoMediaItem(String uuid, String url, String name, String caption, String date, String albumUuid) {
        this.uuid = uuid;
        this.url = url;
        this.caption = caption;
        super.setName(name);
        super.setDate(date);
        this.albumUuid = albumUuid;
    }

    //protected BookeoMediaItem(Parcel in) {
        //uuid = in.readString();
        //url = in.readString();

       // caption = in.readString();
       // super.setName(name);
      //  super.setDate(date);
     //   albumUuid = in.readString();
   // }


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


    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getAlbumUuid() {
        return albumUuid;
    }

    public void setAlbumUuid(String albumUuid) {
        this.albumUuid = albumUuid;
    }

}
