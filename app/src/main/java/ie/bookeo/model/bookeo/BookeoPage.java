package ie.bookeo.model.bookeo;

import java.util.ArrayList;

public class BookeoPage {
    private String pageUuid;
    private String caption;
    private int pageNumber;
    private MyCaptionStyle  style;
    private String type;
    private Boolean isEnlarged;

    public BookeoMediaItem getItem() {
        return item;
    }

    public void setItem(BookeoMediaItem item) {
        this.item = item;
    }

    private BookeoMediaItem item;
    private String albumUuid;

    public BookeoPage() {
    }

    public String getPageUuid() {
        return pageUuid;
    }

    public void setPageUuid(String pageUuid) {
        this.pageUuid = pageUuid;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getAlbumUuid() {
        return albumUuid;
    }

    public void setAlbumUuid(String albumUuid) {
        this.albumUuid = albumUuid;
    }

    public MyCaptionStyle getStyle() {
        return style;
    }

    public void setStyle(MyCaptionStyle style) {
        this.style = style;
    }

    public Boolean getEnlarged() {
        return isEnlarged;
    }

    public void setEnlarged(Boolean enlarged) {
        isEnlarged = enlarged;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
