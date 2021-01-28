package ie.bookeo.utils;

import com.google.api.services.drive.Drive;

import java.util.ArrayList;

import ie.bookeo.dao.DriveServiceHelper;

public interface GoogleDriveDownload {
    void getDriveMediaItem(DriveServiceHelper myDrive, ArrayList<String> ids);
}
