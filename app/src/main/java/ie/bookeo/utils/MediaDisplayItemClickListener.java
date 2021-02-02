package ie.bookeo.utils;

import android.content.Context;

import java.util.ArrayList;

import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.model.gallery_model.DeviceMediaItem;

/**
 *
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *
 *  - URL - https://medium.com/better-programming/gmail-like-list-67bc51adc68a
 *  - Creator - Mustufa Ansari
 *  - Modified by Cian O Sullivan
 *
 */
public interface MediaDisplayItemClickListener {

    /**
     * Called when a picture is clicked
     * @param holder The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     */
    void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> path, Context contx);
    void onBPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> uuid, String albumUuid);
    void onPicClicked(String pictureFolderPath, String folderName);
    void onDrivePicClicked(MediaAdapterHolder holder, String names, String urls, String ids, int position);
    void onBPicClicked(String albumUuid, String AlbumName);
    void onLongPress(MediaAdapterHolder holder, DeviceMediaItem item, int position);
    void onLongPress(MediaAdapterHolder holder, GoogleDriveMediaItem item, int position);
}
