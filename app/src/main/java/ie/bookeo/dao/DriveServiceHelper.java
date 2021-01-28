package ie.bookeo.dao;

import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ie.bookeo.model.BookeoMediaItem;
import okhttp3.internal.Util;

public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;
    byte[] data;

    public DriveServiceHelper(Drive mDriveService) {
        this.mDriveService = mDriveService;
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

    public Task<ArrayList<BookeoMediaItem>> getListImages() {
        ArrayList<BookeoMediaItem> driveMedia = new ArrayList<>();
       return Tasks.call(mExecutor, () -> {
            String pageToken = null;
            do {
                FileList result = null;
                try {
                    result = mDriveService.files().list()
                            .setQ("mimeType='image/jpeg'")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name, webContentLink, createdTime, thumbnailLink, webViewLink)")
                            .setPageToken(pageToken)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (File file : result.getFiles()) {
                    System.out.printf("Found file: %s %S (Web Content Link: %s ) (Date: %s ) (Thumbnail: %s ) (Web View Link: %s )\n",
                            file.getName(), file.getId(), file.getWebContentLink(), file.getCreatedTime(), file.getThumbnailLink(), file.getWebViewLink());
                    BookeoMediaItem item = new BookeoMediaItem();



                    //
                    OutputStream outputStream = new ByteArrayOutputStream();
                    mDriveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
                    data = ((ByteArrayOutputStream) outputStream).toByteArray();

                    item.setDownload(data);

                    driveMedia.add(item);



                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            return driveMedia;
        });
    }

    public void executeDownload(String id){

    }

    public void saveToDevice(){

    }
}
