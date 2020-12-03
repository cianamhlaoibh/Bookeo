package ie.bookeo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.adapter.MediaFolderAdapter;
import ie.bookeo.model.AlbumFolder;
import ie.bookeo.utils.ShowGallery;
import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.MediaAdapter;
import ie.bookeo.model.MediaItem;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;

import java.util.ArrayList;

/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 *  - URL - https://medium.com/better-programming/gmail-like-list-67bc51adc68a
 *  - Creator - Mustufa Ansari
 *  - Modified by Cian O Sullivan
 *
 * This Activity loads all images to images associated with a particular folder into a recyclerview with grid manager
 */

public class MediaDisplayActivity extends AppCompatActivity implements MediaDisplayItemClickListener {

    RecyclerView rvImage;
    ArrayList<MediaItem> arAllMedia;
    ProgressBar pbLoader;
    String folderPath;
    Toolbar tvFolderName;
    ImageButton ibAlbum;

    MediaAdapter adapter;

    ActionMode actionMode;
    ActionCallback actionCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_image_display);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if(!getIntent().getStringExtra("folderName").contains("all")){
            tvFolderName = findViewById(R.id.toolbar);
            tvFolderName.setTitle(getIntent().getStringExtra("folderName"));
            folderPath =  getIntent().getStringExtra("folderPath");
        }else{
            tvFolderName = findViewById(R.id.toolbar);
            tvFolderName.setTitle("All Media");
        }

        arAllMedia = new ArrayList<>();
        rvImage = findViewById(R.id.recycler);
        rvImage.addItemDecoration(new MarginItemDecoration(this));
        rvImage.hasFixedSize();
        pbLoader = findViewById(R.id.loader);
        ibAlbum = findViewById(R.id.ibAlbums);

        actionCallback = new ActionCallback();


        if(arAllMedia.isEmpty() && !getIntent().getStringExtra("folderName").contains("all")){
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = getAllImagesByFolder(folderPath);
            adapter = new MediaAdapter(arAllMedia, MediaDisplayActivity.this,this);
            rvImage.setAdapter(adapter);
            pbLoader.setVisibility(View.GONE);
        }else{
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = getAllImages();
            adapter = new MediaAdapter(arAllMedia, MediaDisplayActivity.this,this);
            rvImage.setAdapter(adapter);
            pbLoader.setVisibility(View.GONE);
        }

        ibAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaDisplayActivity.this, FolderViewActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     *
     * @param holder The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     * @param pics An ArrayList of all the items in the Adapter
     */
    @Override
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<MediaItem> pics, Context contx) {
        if (adapter.selectedItemCount() > 0) {
            toggleActionBar(position);
            adapter.toggleIcon(holder, position);
        } else {
            ArrayList<String> paths = new ArrayList<>();
            for (MediaItem p : pics) {
                paths.add(p.getPath());
            }
            ShowGallery.show(this, paths, position);
        }
    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {

    }

    @Override
    public void onLongPress(MediaAdapterHolder view, MediaItem item, int position) {
        Toast.makeText(this, "long click " + position, Toast.LENGTH_SHORT).show();
        toggleActionBar(position);
        adapter.toggleIcon(view, position);
    }

    /**
     * This Method gets all the images in the folder paths passed as a String to the method and returns
     * and ArrayList of pictureFacer a custom object that holds data of a given image
     */
    public ArrayList<MediaItem> getAllImages(){
        ArrayList<MediaItem> images = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA , MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = MediaDisplayActivity.this.getContentResolver().query( allImagesuri, projection, null,null, null);
        try {
            cursor.moveToFirst();
            do{
                MediaItem pic = new MediaItem();

                pic.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));

                pic.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));

                pic.setSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));

                images.add(pic);
            }while(cursor.moveToNext());
            cursor.close();
            ArrayList<MediaItem> reSelection = new ArrayList<>();
            for(int i = images.size()-1;i > -1;i--){
                reSelection.add(images.get(i));
            }
            images = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri allVideosuri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] vidProjection = { MediaStore.Video.VideoColumns.DATA , MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE};
        Cursor vidCursor = MediaDisplayActivity.this.getContentResolver().query( allVideosuri, vidProjection, null,null, null);
        try {
            vidCursor.moveToFirst();
            do{
                MediaItem pic = new MediaItem();

                pic.setName(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));

                pic.setPath(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));

                pic.setSize(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));

                images.add(pic);
            }while(vidCursor.moveToNext());
            vidCursor.close();
            ArrayList<MediaItem> reSelection = new ArrayList<>();
            for(int i = images.size()-1;i > -1;i--){
                reSelection.add(images.get(i));
            }
            images = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    /**
     * This Method gets all the images in the folder paths passed as a String to the method and returns
     * and ArrayList of pictureFacer a custom object that holds data of a given image
     * @param path a String corresponding to a folder path on the device external storage
     */
    public ArrayList<MediaItem> getAllImagesByFolder(String path){
        ArrayList<MediaItem> media = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = MediaDisplayActivity.this.getContentResolver().query( allImagesuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[] {"%"+path+"%"}, null);
        try {
            cursor.moveToFirst();
            do{
                MediaItem pic = new MediaItem();

                pic.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));

                pic.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));

                pic.setSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));

                media.add(pic);
            }while(cursor.moveToNext());
            cursor.close();
            ArrayList<MediaItem> reSelection = new ArrayList<>();
            for(int i = media.size()-1;i > -1;i--){
                reSelection.add(media.get(i));
            }
            media = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri allVideosuri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] vidProjection = { MediaStore.Video.VideoColumns.DATA , MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE};
        Cursor vidCursor = MediaDisplayActivity.this.getContentResolver().query( allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[] {"%"+path+"%"}, null);
        try {
            vidCursor.moveToFirst();
            do{
                MediaItem pic = new MediaItem();

                pic.setName(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));

                pic.setPath(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));

                pic.setSize(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));

                media.add(pic);
            }while(vidCursor.moveToNext());
            vidCursor.close();
            ArrayList<MediaItem> reSelection = new ArrayList<>();
            for(int i = media.size()-1;i > -1;i--){
                reSelection.add(media.get(i));
            }
            media = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return media;
    }

    /*
       toggling action bar that will change the color and option
     */

    private void toggleActionBar(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionCallback);
        }
        toggleSelection(position);
    }

    /*
       toggle selection of items and show the count of selected items on the action bar
     */

    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.selectedItemCount();
        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(folderPath != null){
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = getAllImagesByFolder(folderPath);
            rvImage.setAdapter(new MediaAdapter(arAllMedia, MediaDisplayActivity.this,this));
            pbLoader.setVisibility(View.GONE);
        }
    }

    private class ActionCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            toggleStatusBarColor(MediaDisplayActivity.this, R.color.blue_grey_700);
            mode.getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delteItem:
                    //deleteInbox();
                    mode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            actionMode = null;
            toggleStatusBarColor(MediaDisplayActivity.this, R.color.colorPrimary);
        }
    }
    /*
      this will toggle or action bar color
     */
    public static void toggleStatusBarColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimary));
    }
}

