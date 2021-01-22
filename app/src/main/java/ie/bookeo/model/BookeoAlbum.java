package ie.bookeo.model;

import com.google.type.DateTime;

public class BookeoAlbum {

    private String uuid;
    private String name;
    private String createDate;
    private int count = 0;
    private String firstItem;
    private String fk_user;

    public BookeoAlbum() {

    }
    public BookeoAlbum(String uuid, String name, String date) {
        this.uuid = uuid;
        this.name = name;
        this.createDate = date;
    }
    public BookeoAlbum(String uuid, String name, String date, String fk_user) {
        this.uuid = uuid;
        this.name = name;
        this.createDate = date;
        this.fk_user = fk_user;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFirstItem() {
        return firstItem;
    }

    public void setFirstItem(String firstItem) {
        this.firstItem = firstItem;
    }

    public String getFk_user() {
        return fk_user;
    }

    public void setFk_user(String fk_user) {
        this.fk_user = fk_user;
    }
}
