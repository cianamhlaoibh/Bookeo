package ie.bookeo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import ie.bookeo.adapter.BookeoFolderAdapter;
import ie.bookeo.model.BookeoAlbum;
import ie.bookeo.model.BookeoMediaItem;
import ie.bookeo.utils.AlbumUploadListener;
import ie.bookeo.utils.Config;
import ie.bookeo.utils.MyCreateListener;
import ie.bookeo.utils.ShowGallery;
import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.MediaAdapter;
import ie.bookeo.model.gallery_model.MediaItem;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.android.gms.common.util.CollectionUtils.mapOf;

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

public class MediaDisplayActivity extends AppCompatActivity implements MediaDisplayItemClickListener, MyCreateListener, AlbumUploadListener {

    RecyclerView rvImage;
    ArrayList<MediaItem> arAllMedia;
    ProgressBar pbLoader;
    String folderPath;
    Toolbar tvFolderName;
    ImageButton ibAlbum;
    MediaAdapter adapter;

    ActionMode actionMode;
    ActionCallback actionCallback;

    //DB
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BookeoFolderAdapter bookeoFolderAdapter;
    private RecyclerView rvAlbums;
    private List<BookeoAlbum> albums;


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


        if (!getIntent().getStringExtra("folderName").contains("all")) {
            tvFolderName = findViewById(R.id.toolbar);
            tvFolderName.setTitle(getIntent().getStringExtra("folderName"));
            folderPath = getIntent().getStringExtra("folderPath");
        } else {
            tvFolderName = findViewById(R.id.toolbar);
            tvFolderName.setTitle("All Media");
        }

        arAllMedia = new ArrayList<>();
        rvImage = findViewById(R.id.rvBookeoAlbums);
        rvImage.addItemDecoration(new MarginItemDecoration(this));
        rvImage.hasFixedSize();
        pbLoader = findViewById(R.id.loader);
        ibAlbum = findViewById(R.id.ibAlbums);

        actionCallback = new ActionCallback();


        if (arAllMedia.isEmpty() && !getIntent().getStringExtra("folderName").contains("all")) {
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = getAllImagesByFolder(folderPath);
            adapter = new MediaAdapter(arAllMedia, MediaDisplayActivity.this, this);
            rvImage.setAdapter(adapter);
            pbLoader.setVisibility(View.GONE);
        } else {
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = getAllImages();
            adapter = new MediaAdapter(arAllMedia, MediaDisplayActivity.this, this);
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

        //DB
        albums = new ArrayList<>();
        albums = getAlbums();

        rvAlbums = findViewById(R.id.rvBookeoAlbumIcons);
        bookeoFolderAdapter = new BookeoFolderAdapter(albums, getApplicationContext(), this);
        rvAlbums.setAdapter(bookeoFolderAdapter);
        // rvAlbums.setVisibility(View.GONE);

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

                                BookeoAlbum arAlbum = new BookeoAlbum(album.getUuid(), album.getName(), album.getCreateDate());

                                dbAlbums.add(arAlbum);

                                //data = albums.get(0).getUuid() + " " + albums.get(0).getName() + " " + album.getCreateDate();
                                Log.d("OUTPUT", "onSuccess create: " + arAlbum.getName());
                            }
                        }
                        bookeoFolderAdapter.notifyDataSetChanged();
                    }
                });
        // BookeoAlbum album = new BookeoAlbum("1234", "TEST", "12/12/20");
        // BookeoAlbum album1 = new BookeoAlbum("2343", "TEST3", "12/12/20");
        // BookeoAlbum album2= new BookeoAlbum("3432", "TEST4", "12/12/20");
        //dbAlbums.add(album);
        // dbAlbums.add(album1);
        //dbAlbums.add(album2);
        Log.d("SIZE", "getAlbums: added" + dbAlbums.size());
        return dbAlbums;
    }

    /**
     * @param holder   The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     * @param pics     An ArrayList of all the items in the Adapter
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

        rvAlbums.setVisibility(View.VISIBLE);
    }

    /**
     * This Method gets all the images in the folder paths passed as a String to the method and returns
     * and ArrayList of pictureFacer a custom object that holds data of a given image
     */
    public ArrayList<MediaItem> getAllImages() {
        ArrayList<MediaItem> images = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_ADDED};
        Cursor cursor = MediaDisplayActivity.this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            cursor.moveToFirst();
            do {
                MediaItem pic = new MediaItem();

                pic.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                //pic.setUri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())));
                pic.setSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                pic.setDate(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)));

                images.add(pic);
            } while (cursor.moveToNext());
            cursor.close();
            ArrayList<MediaItem> reSelection = new ArrayList<>();
            for (int i = images.size() - 1; i > -1; i--) {
                reSelection.add(images.get(i));
            }
            images = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri allVideosuri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] vidProjection = {MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE};
        Cursor vidCursor = MediaDisplayActivity.this.getContentResolver().query(allVideosuri, vidProjection, null, null, null);
        try {
            vidCursor.moveToFirst();
            do {
                MediaItem pic = new MediaItem();

                pic.setName(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));

                pic.setPath(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));

                pic.setSize(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));

                images.add(pic);
            } while (vidCursor.moveToNext());
            vidCursor.close();
            ArrayList<MediaItem> reSelection = new ArrayList<>();
            for (int i = images.size() - 1; i > -1; i--) {
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
     *
     * @param path a String corresponding to a folder path on the device external storage
     */
    public ArrayList<MediaItem> getAllImagesByFolder(String path) {
        ArrayList<MediaItem> media = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_ADDED};
        Cursor cursor = MediaDisplayActivity.this.getContentResolver().query(allImagesuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            cursor.moveToFirst();
            do {
                MediaItem pic = new MediaItem();

                pic.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                pic.setSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                pic.setDate(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)));

                media.add(pic);
            } while (cursor.moveToNext());
            cursor.close();
            ArrayList<MediaItem> reSelection = new ArrayList<>();
            for (int i = media.size() - 1; i > -1; i--) {
                reSelection.add(media.get(i));
            }
            media = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri allVideosuri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] vidProjection = {MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE};
        Cursor vidCursor = MediaDisplayActivity.this.getContentResolver().query(allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            vidCursor.moveToFirst();
            do {
                MediaItem pic = new MediaItem();

                pic.setName(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));

                pic.setPath(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));

                pic.setSize(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));

                media.add(pic);
            } while (vidCursor.moveToNext());
            vidCursor.close();
            ArrayList<MediaItem> reSelection = new ArrayList<>();
            for (int i = media.size() - 1; i > -1; i--) {
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
        if (folderPath != null) {
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = getAllImagesByFolder(folderPath);
            rvImage.setAdapter(new MediaAdapter(arAllMedia, MediaDisplayActivity.this, this));
            pbLoader.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreated(BookeoAlbum bookeoAlbum) {
        albums.add(bookeoAlbum);
        bookeoFolderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUploadAlbumClicked(final String albumUuid) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("media_items");
        List<MediaItem> uploadItems;
        uploadItems = adapter.getUploadItems();
        for (final MediaItem uploadItem : uploadItems) {
            final String uuid = UUID.randomUUID().toString();

            BookeoMediaItem upload = new BookeoMediaItem();
            upload.setUuid(uuid);
            upload.setName(uploadItem.getName());
            upload.setDate(uploadItem.getDate());
            upload.setAlbumUuid(albumUuid);
            db.collection("albums").document(albumUuid).collection("media_items").document(uuid)
                    .set(upload)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("SUCCESS", "DocumentSnapshot added with ID: " + uuid);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("ERROR", "Error adding document", e);
                        }
                    });

            final StorageReference fileRef = storageReference.child(uuid);
            Uri uri = Uri.fromFile(new File(uploadItem.getPath()));
            fileRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pbLoader.setProgress(0);
                                }
                            }, 5000);

                            Toast.makeText(MediaDisplayActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();


                          //  https://stackoverflow.com/questions/57183427/download-url-is-getting-as-com-google-android-gms-tasks-zzu441922b-while-using/57183557
                          fileRef.getDownloadUrl()
                                  .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                      @Override
                                      public void onSuccess(Uri uri) {
                                          String url = uri.toString();
                                          //upload.setUrl(url);
                                          db.collection("albums").document(albumUuid).collection("media_items").document(uuid).update("url", url);
                                          Log.d("URL", "onSuccess: " + uri.toString());
                                      }
                                  });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MediaDisplayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            pbLoader.setProgress((int) progress);
                        }
                    });
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
            if (item.getItemId() == R.id.add) {
                AddAlbumFragment addAlbumFragment = AddAlbumFragment.newInstance("Create Bookeo Album", MediaDisplayActivity.this);
                addAlbumFragment.show(getSupportFragmentManager(), Config.CREATE_BOOKEO_ALBUM);
                adapter.notifyDataSetChanged();
                //mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            actionMode = null;
            toggleStatusBarColor(MediaDisplayActivity.this, R.color.colorPrimary);
            //
            rvAlbums.setVisibility(View.GONE);
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

