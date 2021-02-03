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
 * Google Drive Docs
 * https://developers.google.com/drive/api/v3/search-files
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

    public Task<String> createPDFFile(String filepath) {
        return Tasks.call(mExecutor, () -> {
            File fileMetaData = new File();
            fileMetaData.setName("mypdffile");

            java.io.File file = new java.io.File(filepath);
            FileContent mediaContent = new FileContent("application/pdf", file);

            File myFile = null;
            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            }catch (Exception e){
                e.printStackTrace();
            }
            if (file == null) {
                throw new IOException("Null result when requesting file creation");
            }
            return myFile.getId();//id of file in drive
        });
    }

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

    public Task<ArrayList<DriveFolder>> getFolders() {
        ArrayList<DriveFolder> driveFolders = new ArrayList<>();
        return Tasks.call(mExecutor, () -> {
            String pageToken = null;
            do {
                FileList result = null;
                try {
                    result = mDriveService.files().list()
                            .setQ("mimeType = 'application/vnd.google-apps.folder'")
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
