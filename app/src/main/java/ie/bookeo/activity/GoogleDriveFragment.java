package ie.bookeo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ie.bookeo.R;
import ie.bookeo.adapter.BookeoFolderAdapter;
import ie.bookeo.adapter.BookeoMediaItemAdapter;
import ie.bookeo.adapter.GoogleDriveMediaItemAdapter;
import ie.bookeo.adapter.MediaAdapter;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.ViewPagerAdapter;
import ie.bookeo.dao.DriveServiceHelper;
import ie.bookeo.model.BookeoAlbum;
import ie.bookeo.model.BookeoMediaItem;
import ie.bookeo.model.gallery_model.MediaItem;
import ie.bookeo.utils.AlbumUploadListener;
import ie.bookeo.utils.Config;
import ie.bookeo.utils.GoogleDriveDownload;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;
import ie.bookeo.utils.MyCreateListener;
import ie.bookeo.utils.ShowGallery;

import static android.app.Activity.RESULT_OK;

/**
 *
 */
public class GoogleDriveFragment extends Fragment implements MediaDisplayItemClickListener, AlbumUploadListener {

    DriveServiceHelper driveServiceHelper;
    Button btnDriveSignIn;
    Button btnDriveUpload;
    Button btnListFiles;
    ArrayList<BookeoMediaItem> driveMedia = new ArrayList<>();

    RecyclerView rvMediaItems;
    GoogleDriveMediaItemAdapter adapter;
    GoogleDriveDownload googleDriveDownloadInterface;
    ArrayList<String> ids;

    //select functionality
    Toolbar toolbar;
    TabLayout tabLayout;
    ActionMode actionMode;
    ActionCallback actionCallback;

    //DB
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BookeoFolderAdapter bookeoFolderAdapter;
    private RecyclerView rvAlbums;
    private List<BookeoAlbum> albums;
    TextView tvUploading;
    ProgressBar pbLoader;

    public GoogleDriveFragment() {
        // Required empty public constructor
    }

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_google_drive, container, false);
        btnDriveSignIn = root.findViewById(R.id.btnDriveSignIn);
        btnDriveSignIn.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  requestSignIn();
                                              }
                                          });
        btnDriveUpload = root.findViewById(R.id.btnDriveUpload);
        btnDriveUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(v);
            }
        });
        btnListFiles = root.findViewById(R.id.btnListFiles);
        btnListFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFiles();
            }
        });
        rvMediaItems = root.findViewById(R.id.rvMediaItems);
        rvMediaItems.addItemDecoration(new MarginItemDecoration(getContext()));
        rvMediaItems.hasFixedSize();
        if(driveServiceHelper == null){
            btnDriveSignIn.setVisibility(View.VISIBLE);
        }else{
            btnDriveSignIn.setVisibility(View.GONE);
        }

        //
        toolbar = getActivity().findViewById(R.id.toolbar);
        tabLayout = getActivity().findViewById(R.id.tabs);
        pbLoader = root.findViewById(R.id.loader);
        tvUploading = root.findViewById(R.id.tvUploading);

        actionCallback = new ActionCallback();

        //DB - albums for user to upload to when long press
        albums = new ArrayList<>();
        albums = getAlbums();

        rvAlbums = root.findViewById(R.id.rvBookeoAlbumIcons);
        bookeoFolderAdapter = new BookeoFolderAdapter(albums, getContext(), this);
        rvAlbums.addItemDecoration(new MarginItemDecoration(getContext()));
        rvAlbums.hasFixedSize();
        rvAlbums.setAdapter(bookeoFolderAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter = new GoogleDriveMediaItemAdapter(driveMedia, getContext(), this);
        rvMediaItems.setAdapter(adapter);
    }

    public void listFiles(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Retrieving Images From Google Drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        driveServiceHelper.getListImages()
            .addOnSuccessListener(new OnSuccessListener<ArrayList<BookeoMediaItem>>() {
                @Override
                public void onSuccess(ArrayList<BookeoMediaItem> bookeoMediaItems) {
                    progressDialog.dismiss();
                    driveMedia.addAll(bookeoMediaItems) ;
                    adapter.notifyDataSetChanged();
                }

                //@Override
              //  public void onSuccess(byte list) {
                  //  progressDialog.dismiss();
                   // driveMedia = list;
                   // Log.d("SIZE", "onSuccess: " + list.size());
               // }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Check your google drive api key", Toast.LENGTH_SHORT).show();
                }
            });
        btnDriveSignIn.setVisibility(View.GONE);
    }

    public void uploadFile(View view) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading to Google Drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        String filePath = "/storage/emulated/0/BIS4_S2.pdf";
        driveServiceHelper.createPDFFile(filePath)
            .addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Uplaod Successfully", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Check your google drive api key", Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void requestSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(getContext(), signInOptions);
        startActivityForResult(client.getSignInIntent(), 400);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 400:
                    handleSignInIntent(data);
                break;
        }
    }

    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
                        credential.setSelectedAccount(googleSignInAccount.getAccount());
                        Drive googleDriveService = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("My Drive Tutorial")
                                .build();
                        btnDriveSignIn.setVisibility(View.INVISIBLE);
                        driveServiceHelper = new DriveServiceHelper(googleDriveService);
                        //driveServiceHelper.getDriveIds()
                                //.addOnSuccessListener(new OnSuccessListener<ArrayList<String>>() {
                                  //  @Override
                                 //   public void onSuccess(ArrayList<String> strings) {
                                 //       ids = strings;
                                //    }
                               // });
                        //googleDriveDownloadInterface.getDriveMediaItem(driveServiceHelper, ids);
                        ViewPager viewPager = getActivity().findViewById(R.id.view_pager);
                        ViewPagerAdapter viewPagerAdapter = (ViewPagerAdapter) viewPager.getAdapter();
                        //viewPagerAdapter.dropFragment(3);
                        //viewPagerAdapter.addFragment(3,new BookeoFolderFragment(), "TEST");
                        //viewPager.notify();
                        adapter.setDriveServiceHelper(driveServiceHelper);
                        listFiles();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public ArrayList<BookeoAlbum> getAlbums() {
        final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        db.collection("albums").whereEqualTo("fk_user", userId).get()
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
        Log.d("SIZE", "getAlbums: added" + dbAlbums.size());
        return dbAlbums;
    }

    /**
     * @param holder   The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     * @param paths     An ArrayList of all the items in the Adapter
     */
    @Override
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> paths, Context contx) {
        if (adapter.selectedItemCount() > 0) {
            toggleActionBar(position);
            adapter.toggleIcon(holder, position);
        } else {
            ShowGallery.show(getContext(), paths, position);
        }
    }

    @Override
    public void onBPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> uuid, String albumUuid) {}

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {

    }

    @Override
    public void onBPicClicked(String albumUuid, String AlbumName) {}

    @Override
    public void onLongPress(MediaAdapterHolder view, MediaItem item, int position) {
        //Toast.makeText(this, "long click " + position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLongPress(MediaAdapterHolder holder, BookeoMediaItem item, int position) {
        toolbar.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        toggleActionBar(position);
        adapter.toggleIcon(holder, position);
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
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_TAKEN};
        Cursor cursor = getActivity().getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            cursor.moveToFirst();
            do {
                MediaItem pic = new MediaItem();

                pic.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                //pic.setUri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())));
                pic.setSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                String date = getDate(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)));
                pic.setDate(date);

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
                MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DATE_TAKEN};
        Cursor vidCursor = getActivity().getContentResolver().query(allVideosuri, vidProjection, null, null, null);
        try {
            vidCursor.moveToFirst();
            do {
                MediaItem pic = new MediaItem();

                pic.setName(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
                pic.setPath(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                pic.setSize(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));
                String date = getDate(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)));
                pic.setDate(date);

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

    //https://stackoverflow.com/questions/40193567/get-the-added-modified-taken-date-of-the-video-from-mediastore
    public String getDate(long val){
        val*=1000L;
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(val));
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
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_TAKEN};
        Cursor cursor = getActivity().getContentResolver().query(allImagesuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            cursor.moveToFirst();
            do {
                MediaItem pic = new MediaItem();

                pic.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                pic.setSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                String date = getDate(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)));
                pic.setDate(date);

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
                MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DATE_TAKEN};
        Cursor vidCursor = getActivity().getContentResolver().query(allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            vidCursor.moveToFirst();
            do {
                MediaItem pic = new MediaItem();

                pic.setName(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
                pic.setPath(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                pic.setSize(vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));
                String date = getDate(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)));
                pic.setDate(date);

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
    public void toggleActionBar(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionCallback);
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
    public void onUploadAlbumClicked(final String albumUuid) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("media_items");
        List<BookeoMediaItem> uploadItems;
        uploadItems = adapter.getUploadItems();
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading to Google Drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        for (final BookeoMediaItem uploadItem : uploadItems) {
            final String uuid = UUID.randomUUID().toString();

            BookeoMediaItem upload = new BookeoMediaItem();
            upload.setUuid(uuid);
            upload.setName(uploadItem.getName());
            upload.setDate(uploadItem.getDate());
            upload.setAlbumUuid(albumUuid);
            //https://www.youtube.com/watch?v=Bh0h_ZhX-Qghttps://www.youtube.com/watch?v=Bh0h_ZhX-Qg - add and retireve documents
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
                            progressDialog.dismiss();
                        }
                    });
            //https://www.youtube.com/watch?v=lPfQN-Sfnjw&t=1013s - Firebase Storage - Upload Images
            final StorageReference fileRef = storageReference.child(uuid);
            //https://stackoverflow.com/questions/9897458/how-to-convert-byte-to-inputstream
            byte[] data = uploadItem.getDownload();
            InputStream myInputStream = new ByteArrayInputStream(data);
            fileRef.putStream(myInputStream)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //  https://stackoverflow.com/questions/57183427/download-url-is-getting-as-com-google-android-gms-tasks-zzu441922b-while-using/57183557
                            fileRef.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String url = uri.toString();
                                            //upload.setUrl(url);
                                            //https://www.youtube.com/watch?v=TBr_5QH1EvQ - update firstore
                                            db.collection("albums").document(albumUuid).collection("media_items").document(uuid).update("url", url);
                                            db.collection("albums").document(albumUuid).update("firstItem", url);
                                            Log.d("URL", "onSuccess: " + uri.toString());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        progressDialog.dismiss();
        Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
        //https://stackoverflow.com/questions/39296873/how-can-i-recreate-a-fragment
        getFragmentManager()
                .beginTransaction()
                .detach(GoogleDriveFragment.this)
                .attach(GoogleDriveFragment.this)
                .commit();
        actionMode.finish();
    }

    class ActionCallback implements ActionMode.Callback, MyCreateListener {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            toggleStatusBarColor(getActivity(), R.color.blue_grey_700);

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
                AddAlbumFragment addAlbumFragment = AddAlbumFragment.newInstance("Create Bookeo Album", this);
                addAlbumFragment.show(getActivity().getSupportFragmentManager(), Config.CREATE_BOOKEO_ALBUM);
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
            toggleStatusBarColor(getActivity(), R.color.colorPrimary);
            toolbar.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            rvAlbums.setVisibility(View.GONE);
            getFragmentManager()
                    .beginTransaction()
                    .detach(GoogleDriveFragment.this)
                    .attach(GoogleDriveFragment.this)
                    .commit();
            //  adapter.notifyDataSetChanged();
        }
        @Override
        public void onCreated(BookeoAlbum bookeoAlbum) {
            albums.add(bookeoAlbum);
            bookeoFolderAdapter.notifyDataSetChanged();
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

    @Override
    //https://stackoverflow.com/questions/35810229/how-to-display-and-set-click-event-on-back-arrow-on-toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
