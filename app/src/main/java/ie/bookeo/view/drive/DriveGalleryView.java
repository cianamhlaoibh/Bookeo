package ie.bookeo.view.drive;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import cn.jzvd.JZVideoPlayerStandard;
import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoMainFolderAdapter;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.dao.drive.DriveServiceHelper;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.utils.LoadListener;
import ie.bookeo.view.bookeo.BookeoGalleryView;
/**
 * Reference
 *  - URL - https://github.com/sheetalkumar105/androidimagevideogallery
 *  - Creator - Sheetal Kumar Maurya
 *  - Modified by Cian O Sullivan
 */
public class DriveGalleryView extends AppCompatActivity implements LoadListener{

    DriveServiceHelper driveServiceHelper;

    public static final int PERMISSION_REQUEST = 111;

    int position=0;
    private GalleryPagerAdapter gallaryAdapter;
    ViewPager vpager;
    ImageView ivClose, ivDelete;
    ProgressBar progressBar;

    String names;
    String urls;
    String ids;
    String extension;
    String temp_url;
    byte[] data;

    StorageReference storageReference;

    StorageReference fileRef = null;

    int callback;
    public String TAG = DriveGalleryView.this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_gallery_view);
        Bundle b=getIntent().getExtras();

        vpager= findViewById(R.id.pager);
        ivClose =  findViewById(R.id.btn_close);
        ivDelete = findViewById(R.id.ivDelete);
        progressBar = findViewById(R.id.loader);

        position=b.getInt("position",0);
        names=b.getString("names");
        urls=b.getString("urls");
        ids=b.getString("ids");

        extension = names.substring(names.lastIndexOf("."));
        storageReference = FirebaseStorage.getInstance().getReference("temp_items");
        fileRef = storageReference.child(ids);
        driveServiceHelper = new DriveServiceHelper();
        // Create an executor that executes tasks in a background thread.
        ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
        // Execute a task in the background thread.
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")){
                    data = getMediaBytes();
                    getTempUrl();
                }else {
                    data = getMediaBytes();
                }
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                fileRef.delete();
            }
        });
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driveServiceHelper.deleteFile(ids);
                gallaryAdapter.notifyDataSetChanged();
                finish();
            }
        });
        progressBar.setVisibility(View.VISIBLE);
    }

    private void getTempUrl() {
        LoadListener listener = this;
        final Executor mExecutor = Executors.newSingleThreadExecutor();
        driveServiceHelper.downloadFile(ids)
                .addOnCompleteListener(mExecutor, new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        byte[] data = task.getResult();
                        //https://stackoverflow.com/questions/9897458/how-to-convert-byte-to-inputstream
                        InputStream myInputStream = new ByteArrayInputStream(data);
                        fileRef.putStream(myInputStream)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        //  https://stackoverflow.com/questions/57183427/download-url-is-getting-as-com-google-android-gms-tasks-zzu441922b-while-using/57183557
                                        fileRef.getDownloadUrl()
                                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                        listener.OnComplete(task.getResult().toString());
                                                        gallaryAdapter.notifyDataSetChanged();
                                                        progressBar.setVisibility(View.GONE);
                                                        Log.d("URL", "onSuccess: " + task.getResult().toString());
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    public byte[] getMediaBytes() {
        try {
            DriveServiceHelper driveServiceHelper = new DriveServiceHelper();
            final Task<byte[]> appUpdateInfoTask = driveServiceHelper.downloadFile(ids)
                    .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                        @Override
                        public void onComplete(@NonNull Task<byte[]> task) {
                            gallaryAdapter.notifyDataSetChanged();
                            if(!extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")){
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
            try {
                return Tasks.await(appUpdateInfoTask);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkWriteExternalPermission()) {
            _init();
        }else
            grantPermission();
    }

    private boolean checkWriteExternalPermission()
    {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void grantPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _init();
                }
            }
        }
    }


    public void _init(){
        gallaryAdapter = new GalleryPagerAdapter(this);
        vpager.setAdapter(gallaryAdapter);
        vpager.setOffscreenPageLimit(3); // how many images to load into memoryvpager
        vpager.setCurrentItem(position);

        vpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                position = pos;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void OnSuccess(byte[] data) {
       // this.data = data;
    }

    @Override
    public void OnComplete(String url) {
        this.temp_url = url;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //data = null;
        //temp_url = null;
    }

    class GalleryPagerAdapter extends PagerAdapter {

        Context _context;
        LayoutInflater _inflater;
       // byte[] data;
        Object object;

        boolean isFullscreen = false;

        public GalleryPagerAdapter(Context context) {
            _context = context;
            _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            String id = ids;
            String url  = urls;
            String name = names;
            extension = name.substring(name.lastIndexOf("."));

            View itemView = null;

                            if(extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")) {
                                   if (temp_url != null) {
                                       itemView = _inflater.inflate(R.layout.pager_video_item, container, false);
                                       container.addView(itemView);
                                        final JZVideoPlayerStandard jzVideoPlayerStandard = (JZVideoPlayerStandard) itemView.findViewById(R.id.videoplayer);
                                        jzVideoPlayerStandard.setVisibility(View.VISIBLE);
                                        jzVideoPlayerStandard.setUp(temp_url,
                                                JZVideoPlayerStandard.SCREEN_WINDOW_LIST,
                                                "");
                                        Glide.with(_context).load(temp_url)
                                                .into(jzVideoPlayerStandard.thumbImageView);
                                    }
                                }else{
                                    itemView = _inflater.inflate(R.layout.pager_gallery_item, container, false);
                                    container.addView(itemView);
                                    final SubsamplingScaleImageView imageView =
                                            (SubsamplingScaleImageView) itemView.findViewById(R.id.image);
                                    Glide.with(getApplicationContext())
                                            .asBitmap()
                                            .load(data)
                                            .into(new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(Bitmap image,
                                                                            Transition<? super Bitmap> transition) {

                                                    int xDim = image.getWidth();
                                                    int yDim = image.getHeight();
                                                    if (xDim <= 4096 && yDim <= 4096) {
                                                        imageView.setImage(ImageSource.bitmap(image));
                                                    } else {
                                                        if (xDim > yDim) {
                                                            int nh = (int) (image.getHeight() * (4096f / image.getWidth()));
                                                            Bitmap scaled = Bitmap.createScaledBitmap(image, 4096, nh, true);
                                                            imageView.setImage(ImageSource.bitmap(scaled));
                                                        } else {
                                                            int nh = (int) (image.getWidth() * (4096f / image.getHeight()));
                                                            Bitmap scaled = Bitmap.createScaledBitmap(image, nh, 4096, true);
                                                            imageView.setImage(ImageSource.bitmap(scaled));
                                                        }
                                                    }

                                                }
                                            });
                                }

            Log.e("Extension",extension);
           return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
