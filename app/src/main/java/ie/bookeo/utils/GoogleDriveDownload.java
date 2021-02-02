package ie.bookeo.utils;

import java.util.ArrayList;

import ie.bookeo.dao.drive.DriveServiceHelper;

public interface GoogleDriveDownload {
    void getDriveMediaItem(DriveServiceHelper myDrive, ArrayList<String> ids);
}
