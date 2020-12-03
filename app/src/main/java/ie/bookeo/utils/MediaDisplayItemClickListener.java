package ie.bookeo.utils;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.model.MediaItem;

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
    void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<MediaItem> pics, Context contx);
    void onPicClicked(String pictureFolderPath, String folderName);
    void onLongPress(MediaAdapterHolder holder, MediaItem item, int position);
}
