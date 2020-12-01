package ie.bookeo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.MediaFolderAdapter;
import ie.bookeo.model.AlbumFolder;
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
 * This Activity loads all folders containing images int a RecyclerView
 */

public class FolderViewActivity extends AppCompatActivity implements MediaDisplayItemClickListener {

    RecyclerView rvFolder;
    TextView tvEmpty;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    /**
     * Request the user for permission to access media files and read images on the device
     * this will be useful as from api 21 and above, if this check is not done the Activity will crash
     *
     * Setting up the RecyclerView and getting all folders that contain pictures from the device
     * the getPicturePaths() returns an ArrayList of imageFolder objects that is then used to
     * create a RecyclerView Adapter that is set to the RecyclerView
     *
     * @param savedInstanceState saving the activity state
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(ContextCompat.checkSelfPermission(FolderViewActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(FolderViewActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        //____________________________________________________________________________________

        tvEmpty =findViewById(R.id.empty);

        rvFolder = findViewById(R.id.recycler);
        rvFolder.addItemDecoration(new MarginItemDecoration(this));
        rvFolder.hasFixedSize();
        ArrayList<AlbumFolder> folds = getPicturePaths();

        if(folds.isEmpty()){
            tvEmpty.setVisibility(View.VISIBLE);
        }else{
            RecyclerView.Adapter folderAdapter = new MediaFolderAdapter(folds, FolderViewActivity.this,this);
            rvFolder.setAdapter(folderAdapter);
        }

        changeStatusBarColor();

        ImageButton ibPhotos;
        ibPhotos = findViewById(R.id.ibPhotos);
        ibPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FolderViewActivity.this, MediaDisplayActivity.class);
                intent.putExtra("folderName","all");
                startActivity(intent);
            }
        });
    }

    /**1
     * @return
     * gets all folders with pictures on the device and loads each of them in a custom object imageFolder
     * the returns an ArrayList of these custom objects
     */
    private ArrayList<AlbumFolder> getPicturePaths(){
        ArrayList<AlbumFolder> imgFolders = new ArrayList<>();
        ArrayList<String> imgPaths = new ArrayList<>();
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA , MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do{
                AlbumFolder folds = new AlbumFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));
                folderpaths = folderpaths+folder+"/";
                if (!imgPaths.contains(folderpaths)) {
                    imgPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setName(folder);
                    folds.setFirstItem(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.add();
                    imgFolders.add(folds);
                }else{
                    for(int i = 0;i<imgFolders.size();i++){
                        if(imgFolders.get(i).getPath().equals(folderpaths)){
                            imgFolders.get(i).setFirstItem(datapath);
                            imgFolders.get(i).add();
                        }
                    }
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri allVideouri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] vidProjection = { MediaStore.Video.VideoColumns.DATA , MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.BUCKET_ID};
        Cursor vidCursor = this.getContentResolver().query(allVideouri, vidProjection, null, null, null);
        try {
            if (vidCursor != null) {
                vidCursor.moveToFirst();
            }
            do{
                AlbumFolder folds = new AlbumFolder();
                String name = vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));
                folderpaths = folderpaths+folder+"/";
                if (!imgPaths.contains(folderpaths)) {
                    imgPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setName(folder);
                    folds.setFirstItem(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.add();
                    imgFolders.add(folds);
                }else{
                    for(int i = 0;i<imgFolders.size();i++){
                        if(imgFolders.get(i).getPath().equals(folderpaths)){
                            imgFolders.get(i).setFirstItem(datapath);
                            imgFolders.get(i).add();
                        }
                    }
                }
            }while(vidCursor.moveToNext());
            vidCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgFolders;
    }



    @Override
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<MediaItem> pics, Context contx) {

    }

    /**
     * Each time an item in the RecyclerView is clicked this method from the implementation of the transitListerner
     * in this activity is executed, this is possible because this class is passed as a parameter in the creation
     * of the RecyclerView's Adapter, see the adapter class to understand better what is happening here
     * @param pictureFolderPath a String corresponding to a folder path on the device external storage
     */
    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {
        Intent move = new Intent(FolderViewActivity.this, MediaDisplayActivity.class);
        move.putExtra("folderPath",pictureFolderPath);
        move.putExtra("folderName",folderName);

        //move.putExtra("recyclerItemSize",getCardsOptimalWidth(4));
        startActivity(move);
    }


    /**
     * Default status bar height 24dp,with code API level 24
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeStatusBarColor()
    {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.black));

    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.Licenses:
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<AlbumFolder> folds = getPicturePaths();

        if(folds.isEmpty()){
            tvEmpty.setVisibility(View.VISIBLE);
        }else{
            RecyclerView.Adapter folderAdapter = new MediaFolderAdapter(folds, FolderViewActivity.this,this);
            rvFolder.setAdapter(folderAdapter);
        }
    }
}
