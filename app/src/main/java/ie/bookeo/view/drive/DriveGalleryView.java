package ie.bookeo.view.drive;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;

import cn.jzvd.JZVideoPlayerStandard;
import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoMainFolderAdapter;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.dao.drive.DriveServiceHelper;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.utils.LoadListener;
import ie.bookeo.view.bookeo.BookeoGalleryView;

public class DriveGalleryView extends AppCompatActivity implements LoadListener{

    private BookeoMainFolderAdapter bookeoMainFolderAdapter;
    private BookeoMediaItemDao bookeoMediaItemDao;

    public static final int PERMISSION_REQUEST = 111;

    int position=0;
    private GalleryPagerAdapter gallaryAdapter;
    ViewPager vpager;
    ImageView ivClose, ivDelete;

//    ArrayList<String> names =new ArrayList<>();
//    ArrayList<String> urls =new ArrayList<>();
//    ArrayList<String> ids =new ArrayList<>();

    String names;
    String urls;
    String ids;
    byte[] data;

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

        vpager= (ViewPager) findViewById(R.id.pager);
        ivClose = (ImageView) findViewById(R.id.btn_close);
        ivDelete = (ImageView) findViewById(R.id.ivDelete);

        position=b.getInt("position",0);
//        names=b.getStringArrayList("names");
//        urls=b.getStringArrayList("urls");
//        ids=b.getStringArrayList("ids");
        names=b.getString("names");
        urls=b.getString("urls");
        ids=b.getString("ids");


        data = getMediaBytes();



        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBookeoItem();
                gallaryAdapter.notifyDataSetChanged();
            }
        });
            }

    public byte[] getMediaBytes(){
        try {
            DriveServiceHelper driveServiceHelper = new DriveServiceHelper();
            final Task<byte[]> appUpdateInfoTask = driveServiceHelper.downloadFile(ids)
                    .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                        @Override
                        public void onComplete(@NonNull Task<byte[]> task) {
                            //loadListener.OnSuccess(task.getResult());
                        }
                    });
            try {
                Tasks.await(appUpdateInfoTask);
                byte[] data = appUpdateInfoTask.getResult();
                return data;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }catch (Exception e){
                e.printStackTrace();
                return null;
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkWriteExternalPermission())
            _init();
        else
            grantPermission();
    }

    private void deleteBookeoItem() {
//        bookeoMediaItemDao = new BookeoMediaItemDao();
//        int position = vpager.getCurrentItem();
//        String currentUuid = uuids.get(position);
//        names.remove(position);
//        urls.remove(position);
//        uuids.remove(position);
//        bookeoMediaItemDao.deleteMediaItem(albumUuid, currentUuid, this);
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
        this.data = data;
    }

    @Override
    public void OnComplete(Object obj) {

    }


    class GalleryPagerAdapter extends PagerAdapter implements LoadListener {

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
            String extension = name.substring(name.lastIndexOf("."));

            LoadListener loadListener = this;


                            View itemView = null;
                            if(extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")){
                                itemView = _inflater.inflate(R.layout.pager_video_item, container, false);
                                container.addView(itemView);



                                final JZVideoPlayerStandard jzVideoPlayerStandard = (JZVideoPlayerStandard) itemView.findViewById(R.id.videoplayer);
                                jzVideoPlayerStandard.setUp(data.toString(),
                                        JZVideoPlayerStandard.SCREEN_WINDOW_LIST,
                                        "");
                                Glide.with(_context).load(data)
                                        .into(jzVideoPlayerStandard.thumbImageView);



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

                                                int xDim=image.getWidth();
                                                int yDim=image.getHeight();
                                                if(xDim<=4096 && yDim <=4096){
                                                    imageView.setImage(ImageSource.bitmap(image));
                                                }else{
                                                    if(xDim>yDim){
                                                        int nh = (int) ( image.getHeight() * (4096f / image.getWidth()) );
                                                        Bitmap scaled = Bitmap.createScaledBitmap(image, 4096, nh, true);
                                                        imageView.setImage(ImageSource.bitmap(scaled));
                                                    }else{
                                                        int nh = (int) ( image.getWidth() * (4096f / image.getHeight()) );
                                                        Bitmap scaled = Bitmap.createScaledBitmap(image, nh,4096 , true);
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

        @Override
        public void OnSuccess(byte[] data) {
            //this.data = data;
        }

        @Override
        public void OnComplete(Object obj) {
            this.object = obj;
        }
    }
}
