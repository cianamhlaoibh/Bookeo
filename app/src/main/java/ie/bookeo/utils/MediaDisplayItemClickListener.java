package ie.bookeo.utils;

import android.content.Context;

import java.util.ArrayList;

import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.model.MediaItem;

/**
 *
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *
 */
public interface MediaDisplayItemClickListener {

    /**
     * Called when a picture is clicked
     * @param holder The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     */
    void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<MediaItem> pics, Context contx);
    void onPicClicked(String pictureFolderPath, String folderName);
}