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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import ie.bookeo.R;
import ie.bookeo.adapter.BookeoMainFolderAdapter;
import ie.bookeo.adapter.LoginAdapter;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.MediaFolderAdapter;
import ie.bookeo.model.BookeoAlbum;
import ie.bookeo.model.gallery_model.AlbumFolder;
import ie.bookeo.model.gallery_model.MediaItem;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 *  -URL - https://www.youtube.com/watch?v=TwHmrZxiPA8
 *
 * This Activity loads all folders containing images int a RecyclerView
 */

public class FolderViewActivity extends AppCompatActivity implements MediaDisplayItemClickListener {

    RecyclerView rvFolder, rvAlbums;
    TextView tvEmpty;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private BookeoMainFolderAdapter bookeoMainFolderAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference albumsRef = db.collection("albums");

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

        //____________________________________________________________________________________

        tvEmpty =findViewById(R.id.empty);

        rvFolder = findViewById(R.id.rvFolders);
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

        //-------------------------------------------------------------------
        rvAlbums = findViewById(R.id.rvBookeoAlbums);
        rvAlbums.addItemDecoration(new MarginItemDecoration(this));
        rvAlbums.hasFixedSize();


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
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> paths, Context contx) {

    }

    @Override
    public void onBPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> uuid, String albumUuid) {

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

    @Override
    public void onBPicClicked(String albumUuid, String AlbumName) {
        Intent move = new Intent(FolderViewActivity.this, BookeoMediaDisplay.class);
        move.putExtra("folderUuid",albumUuid);
        move.putExtra("folderName",AlbumName);
        startActivity(move);
    }

    @Override
    public void onLongPress(MediaAdapterHolder view, MediaItem item, int position) {

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
                //https://developers.google.com/android/guides/opensource
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
            case R.id.Logout:
                //log out for user who signed in with google
                GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
                if(signInAccount != null) {
                    GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Logout Failure", Toast.LENGTH_LONG).show();
                            }
                        });
                    //also sign out of firebase auth
                    FirebaseAuth.getInstance().signOut();
                }else{
                    //logout for email/password
                    //https://www.youtube.com/watch?v=TwHmrZxiPA8
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
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

        ArrayList<BookeoAlbum> albums = getAlbums();
        bookeoMainFolderAdapter = new BookeoMainFolderAdapter(albums, FolderViewActivity.this,this);
        rvAlbums.setAdapter(bookeoMainFolderAdapter);
    }

    public ArrayList<BookeoAlbum> getAlbums() {
        final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
        db.collection("albums").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                    String data = "";

                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoAlbum album = new BookeoAlbum();
                                album = documentSnapshot.toObject(BookeoAlbum.class);

                                final BookeoAlbum arAlbum = new BookeoAlbum(album.getUuid(), album.getName(), album.getCreateDate());
                                arAlbum.setFirstItem(album.getFirstItem());


                                dbAlbums.add(arAlbum);

                                //data = albums.get(0).getUuid() + " " + albums.get(0).getName() + " " + album.getCreateDate();
                                Log.d("OUTPUT", "onSuccess create: " + arAlbum.getName());
                            }
                        }
                        bookeoMainFolderAdapter.notifyDataSetChanged();
                    }
                });
        return dbAlbums;
    }
}
