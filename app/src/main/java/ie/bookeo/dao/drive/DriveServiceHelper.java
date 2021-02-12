package ie.bookeo.dao.drive;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.squareup.okhttp.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ie.bookeo.model.drive.DriveFolder;
import ie.bookeo.model.drive.GoogleDriveMediaItem;


/**
 * References
 *
 *  - Google Drive API in Android Studio Tutorial Series
 *  - URL - https://www.youtube.com/watch?v=KuXeMoJwcis&list=PLF0BIlN2vd8sqGfhxmzJ93SmfMLMtM1nW
 *  - Creator - Coding With Tea
 *
 *  - Search for files and folders
 *  - URL - https://developers.google.com/drive/api/v3/search-files
 *  - Creator - Google Drive Docs
 *
 *  - Download files
 *  - URL - https://developers.google.com/drive/api/v3/manage-downloads
 *  - Creator - Google Drive Docs
 *
 */
public class DriveServiceHelper {

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    public static Drive mDriveService;
    public static GoogleAccountCredential credential;
    byte[] data;

    public DriveServiceHelper(Drive mDriveService, GoogleAccountCredential credential) {
        this.mDriveService = mDriveService;
        this.credential = credential;
    }

    public DriveServiceHelper() {

    }

    //https://developers.google.com/drive/api/v3/search-files
    public Task<ArrayList<GoogleDriveMediaItem>> getListImages() {
        ArrayList<GoogleDriveMediaItem> driveMedia = new ArrayList<>();
       return Tasks.call(mExecutor, () -> {
            String pageToken = null;
            do {
                FileList result = null;
                try {
                    result = mDriveService.files().list()
                            .setQ("(mimeType contains 'image/' or mimeType contains 'video/') and 'root' in parents")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name, createdTime, thumbnailLink)")
                            .setPageToken(pageToken)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (File file : result.getFiles()) {
                    //OutputStream outputStream = new ByteArrayOutputStream();
                    //mDriveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
                    //data = ((ByteArrayOutputStream) outputStream).toByteArray();
                    GoogleDriveMediaItem item = new GoogleDriveMediaItem(file.getName(), file.getCreatedTime().toString(),file.getId(), file.getThumbnailLink());
                    driveMedia.add(item);
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            return driveMedia;
        });
    }

    //https://developers.google.com/drive/api/v3/search-files
    public Task<ArrayList<DriveFolder>> getRootFolders() {
        ArrayList<DriveFolder> driveFolders = new ArrayList<>();
        return Tasks.call(mExecutor, () -> {
            String pageToken = null;
            do {
                FileList result = null;
                try {
                    result = mDriveService.files().list()
                            .setQ("mimeType = 'application/vnd.google-apps.folder' and 'root' in parents")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageToken(pageToken)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (File file : result.getFiles()) {
                    DriveFolder driveFolder = new DriveFolder();
                    driveFolder.setId(file.getId());
                    driveFolder.setName(file.getName());
                    driveFolders.add(driveFolder);
                    Log.d("Folder", "getFolders: " + file.getId() + " " + file.getName());
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            return driveFolders;
        });
    }

    //https://developers.google.com/drive/api/v3/search-files
    public Task<ArrayList<DriveFolder>> getSubFolders(String id) {
        ArrayList<DriveFolder> driveFolders = new ArrayList<>();
        return Tasks.call(mExecutor, () -> {
            String pageToken = null;
            do {
                FileList result = null;
                try {
                    result = mDriveService.files().list()
                            .setQ("mimeType = 'application/vnd.google-apps.folder' and '" + id + "' in parents")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageToken(pageToken)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (File file : result.getFiles()) {
                    DriveFolder driveFolder = new DriveFolder();
                    driveFolder.setId(file.getId());
                    driveFolder.setName(file.getName());
                    driveFolders.add(driveFolder);
                    Log.d("Folder", "getFolders: " + file.getId() + " " + file.getName());
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            return driveFolders;
        });
    }

    //https://developers.google.com/drive/api/v3/manage-downloads
    public Task<byte[]> downloadFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            byte[] data;
                    OutputStream outputStream = new ByteArrayOutputStream();
                    mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                    data = ((ByteArrayOutputStream) outputStream).toByteArray();
            return  data;
        });
    }

    public void deleteFile(String fileId) {
        Tasks.call(mExecutor, () -> {
            byte[] data;
            mDriveService.files().delete(fileId).execute();
            return null;
        });
    }

    public Task<ArrayList<GoogleDriveMediaItem>> getMediaByFolder(String id) {
        ArrayList<GoogleDriveMediaItem> driveMedia = new ArrayList<>();
        return Tasks.call(mExecutor, () -> {
            String pageToken = null;
            do {
                FileList result = null;
                try {
                    result = mDriveService.files().list()
                            .setQ("(mimeType contains 'image/' or mimeType contains 'video/') and '" + id + "' in parents")
                            //.setSpaces("drive")
                            .setFields("nextPageToken, files(id, name, createdTime, thumbnailLink)")
                            .setPageToken(pageToken)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (File file : result.getFiles()) {
                    GoogleDriveMediaItem item = new GoogleDriveMediaItem(file.getName(), file.getCreatedTime().toString(),file.getId(), file.getThumbnailLink());
                    driveMedia.add(item);
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            return driveMedia;
        });
    }
}
