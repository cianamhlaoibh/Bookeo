package ie.bookeo.utils;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import ie.bookeo.activity.GalleryViewActivity;
/**
 * Reference
 *  - URL - https://github.com/sheetalkumar105/androidimagevideogallery
 *  - Creator - Sheetal Kumar Maurya
 *  - Modified by Cian O Sullivan
 *
 * This class pass paths from MediaDisplay Activity to the GalleryViewActivity to display them.
 */

public class ShowGallery {

    public static ActionCallback actionCallback;

    public static void show(Context context, ArrayList<String> paths, int position){
        Intent intent = new Intent(context, GalleryViewActivity.class);
        intent.putExtra("items", paths);
        intent.putExtra("position", position);
        context.startActivity(intent);
    }
}
