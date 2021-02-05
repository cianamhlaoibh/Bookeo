package ie.bookeo.view.drive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoFolderAdapter;
import ie.bookeo.adapter.drive.GoogleDriveFolderAdapter;
import ie.bookeo.adapter.drive.GoogleDriveMediaItemAdapter;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.mediaExplorer.ViewPagerAdapter;
import ie.bookeo.dao.drive.DriveServiceHelper;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.drive.DriveFolder;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.model.gallery_model.DeviceMediaItem;
import ie.bookeo.utils.AlbumUploadListener;
import ie.bookeo.utils.Config;
import ie.bookeo.utils.DriveLogoutListener;
import ie.bookeo.utils.GoogleDriveDownload;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;
import ie.bookeo.utils.MyCreateListener;
import ie.bookeo.utils.ShowGallery;
import ie.bookeo.view.mediaExplorer.AddAlbumFragment;
import ie.bookeo.view.mediaExplorer.MainActivity;

import static ie.bookeo.dao.drive.DriveServiceHelper.credential;
import static ie.bookeo.dao.drive.DriveServiceHelper.mDriveService;

/**
 *
 */
public class GoogleDriveFragment extends Fragment implements MediaDisplayItemClickListener, AlbumUploadListener {

    DriveServiceHelper driveServiceHelper;
    Button btnDriveSignIn, btnDriveResync;
    ArrayList<DriveFolder> driveFolders = new ArrayList<>();
    ArrayList<GoogleDriveMediaItem> driveMedia = new ArrayList<>();
    GoogleSignInClient client;

    RecyclerView rvFolders, rvMediaItems;
    GoogleDriveFolderAdapter adapter;
    GoogleDriveMediaItemAdapter mAdapter;

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
    DriveLogoutListener driveLogoutListener;


    public GoogleDriveFragment(DriveLogoutListener listener) {
        this.driveLogoutListener = listener;
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
        btnDriveResync = root.findViewById(R.id.btnDriveResync);
        btnDriveResync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSignIn();
            }
        });
        rvFolders = root.findViewById(R.id.rvFolders);
        rvFolders.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if(signInAccount != null){
            btnDriveResync.setVisibility(View.VISIBLE);
            btnDriveSignIn.setVisibility(View.GONE);
        }else if(driveServiceHelper == null){
            btnDriveResync.setVisibility(View.GONE);
        }
        else{
            btnDriveSignIn.setVisibility(View.VISIBLE);
            btnDriveResync.setVisibility(View.GONE);
        }


        rvMediaItems = root.findViewById(R.id.rvMediaItems);
        rvMediaItems.addItemDecoration(new MarginItemDecoration(getContext()));
        rvMediaItems.hasFixedSize();

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
        adapter = new GoogleDriveFolderAdapter(driveFolders, getContext());
        rvFolders.setAdapter(adapter);
        mAdapter = new GoogleDriveMediaItemAdapter(driveMedia, getContext(), this);
        rvMediaItems.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void listFiles(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Retrieving Media From Google Drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        driveServiceHelper.getFolders()
            .addOnSuccessListener(new OnSuccessListener<ArrayList<DriveFolder>>() {
                @Override
                public void onSuccess(ArrayList<DriveFolder> folders) {
                    driveFolders.addAll(folders) ;
                    adapter.notifyDataSetChanged();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Check your google drive api key", Toast.LENGTH_SHORT).show();
                }
            });
        driveServiceHelper.getListImages()
                .addOnSuccessListener(new OnSuccessListener<ArrayList<GoogleDriveMediaItem>>() {
                    @Override
                    public void onSuccess(ArrayList<GoogleDriveMediaItem> bookeoMediaItems) {
                        progressDialog.dismiss();
                        driveMedia.addAll(bookeoMediaItems) ;
                       mAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Check your google drive api key", Toast.LENGTH_SHORT).show();
                    }
                });
        //progressDialog.dismiss();

        btnDriveSignIn.setVisibility(View.GONE);
        btnDriveResync.setVisibility(View.GONE);
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
        client = GoogleSignIn.getClient(getContext(), signInOptions);
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
                        driveServiceHelper = new DriveServiceHelper(googleDriveService, credential);
                        listFiles();
                        driveLogoutListener.DriveLogin(GoogleDriveFragment.this);
                        mAdapter.notifyDataSetChanged();
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
    }

    @Override
    public void onBPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> uuid, String albumUuid) {}

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {

    }

    @Override
    public void onDrivePicClicked(MediaAdapterHolder holder, String names, String urls, String ids, int position) {
        if (mAdapter.selectedItemCount() > 0) {
            toggleActionBar(position);
            mAdapter.toggleIcon(holder, position);
        } else {
            ShowGallery.dShow(getContext(), names, urls, ids, position);
        }
    }

    @Override
    public void onBPicClicked(String albumUuid, String AlbumName) {}

    @Override
    public void onLongPress(MediaAdapterHolder view, DeviceMediaItem item, int position) {
        //Toast.makeText(this, "long click " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongPress(MediaAdapterHolder holder, GoogleDriveMediaItem item, int position) {
        toolbar.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        toggleActionBar(position);
        mAdapter.toggleIcon(holder, position);
        rvAlbums.setVisibility(View.VISIBLE);
    }

    /**
     * This Method gets all the images in the folder paths passed as a String to the method and returns
     * and ArrayList of pictureFacer a custom object that holds data of a given image
     */

    //https://stackoverflow.com/questions/40193567/get-the-added-modified-taken-date-of-the-video-from-mediastore
    public String getDate(long val){
        val*=1000L;
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(val));
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
        mAdapter.toggleSelection(position);
        int count = mAdapter.selectedItemCount();
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
        List<GoogleDriveMediaItem> uploadItems;
        uploadItems = mAdapter.getUploadItems();
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading to Google Drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        for (final GoogleDriveMediaItem uploadItem : uploadItems) {
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
            final Executor mExecutor = Executors.newSingleThreadExecutor();
            DriveServiceHelper driveServiceHelper = new DriveServiceHelper();
            driveServiceHelper.downloadFile(uploadItem.getFileId())
                    .addOnCompleteListener(mExecutor, new OnCompleteListener<byte[]>() {
                        @Override
                        public void onComplete(@NonNull Task<byte[]> task) {
                            byte[] data = task.getResult();
                            //https://stackoverflow.com/questions/9897458/how-to-convert-byte-to-inputstream
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
                    });
        }
        progressDialog.dismiss();
        Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
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
                mAdapter.notifyDataSetChanged();
                //mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelection();
            actionMode = null;
            toggleStatusBarColor(getActivity(), R.color.colorPrimary);
            toolbar.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            rvAlbums.setVisibility(View.GONE);
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

    public void Logout() {
        btnDriveSignIn.setVisibility(View.VISIBLE);
        driveMedia.clear();
        driveFolders.clear();
        adapter.notifyDataSetChanged();
        mAdapter.notifyDataSetChanged();
    }
}
