package ie.bookeo.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;

import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.view.bookeo.BookeoGalleryView;
import ie.bookeo.view.drive.DriveGalleryView;
import ie.bookeo.view.mediaExplorer.GalleryViewActivity;

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

    public static void bShow(Context context, ArrayList<String> names, ArrayList<String> urls, int position, ArrayList<String> uuids, String albumUuid){
        Intent intent = new Intent(context, BookeoGalleryView.class);
        intent.putExtra("names", names);
        intent.putExtra("urls", urls);
        intent.putExtra("position", position);
        intent.putExtra("albumUuid", albumUuid);
        intent.putExtra("uuids", uuids);
        context.startActivity(intent);
    }

    public static void dShow(Context contx, String names, String urls, String ids, int position) {
        Intent intent = new Intent(contx, DriveGalleryView.class);
        intent.putExtra("position", position);
        intent.putExtra("names", names);
        intent.putExtra("urls", urls);
        intent.putExtra("ids", ids);
//        Bundle data = new Bundle();
//        data.putParcelableArrayList("driveItems", (ArrayList<? extends Parcelable>) items);
        //ArrayList<> byteList = new ArrayList<Byte>();
       //intent.putCharSequenceArrayListExtra("driveItems", byteList);
//        intent.putExtra("bundle", data);
        contx.startActivity(intent);
    }
}
