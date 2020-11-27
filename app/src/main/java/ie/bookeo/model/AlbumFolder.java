package ie.bookeo.model;

import com.google.type.DateTime;

/**
 *
 * This is the model class for storing information about and ablum. In later iterations it will be used to create albums
 *
 */

public class AlbumFolder {

    private String path;
    private String name;
    private DateTime createDate;
    private int count = 0;
    private String firstItem;

    public AlbumFolder(){

    }

    public AlbumFolder(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int numberOfPics) {
        this.count = numberOfPics;
    }

    public void add(){
        this.count++;
    }

    public String getFirstItem() {
        return firstItem;
    }

    public void setFirstItem(String firstPic) {
        this.firstItem = firstPic;
    }

    public DateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
    }
}
