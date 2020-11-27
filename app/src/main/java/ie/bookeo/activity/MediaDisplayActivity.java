package ie.bookeo.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
 * This Activity loads all images to images associated with a particular folder into a recyclerview with grid manager
 */

public class MediaDisplayActivity extends AppCompatActivity implements MediaDisplayItemClickListener {

    RecyclerView rvImage;
    ArrayList<MediaItem> arAllMedia;
    ProgressBar pbLoader;
    String folderPath;
    TextView tvFolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);


        if(!getIntent().getStringExtra("folderName").contains("all")){
            tvFolderName = findViewById(R.id.foldername);
            tvFolderName.setText(getIntent().getStringExtra("folderName"));
            folderPath =  getIntent().getStringExtra("folderPath");
        }

        arAllMedia = new ArrayList<>();
        rvImage = findViewById(R.id.recycler);
        rvImage.addItemDecoration(new MarginItemDecoration(this));
        rvImage.hasFixedSize();
        pbLoader = findViewById(R.id.loader);


        if(arAllMedia.isEmpty() && !getIntent().getStringExtra("folderName").contains("all")){
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = getAllImagesByFolder(folderPath);
            rvImage.setAdapter(new MediaAdapter(arAllMedia, MediaDisplayActivity.this,this));
            pbLoader.setVisibility(View.GONE);
        }else{
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = getAllImages();
            rvImage.setAdapter(new MediaAdapter(arAllMedia, MediaDisplayActivity.this,this));
            pbLoader.setVisibility(View.GONE);
        }
    }

    /**
     *
     * @param holder The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     * @param pics An ArrayList of all the items in the Adapter
     */
    @Override
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<MediaItem> pics, Context contx) {
        ArrayList<String> paths = new ArrayList<>();
        for (MediaItem p : pics) {
            paths.add(p.getPath());
        }
        ShowGallery.show(this, paths, position);
    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {

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
        ArrayList<MediaItem> images = new ArrayList<>();
        Uri allVideosuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = MediaDisplayActivity.this.getContentResolver().query( allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[] {"%"+path+"%"}, null);
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
        return images;
    }

}

