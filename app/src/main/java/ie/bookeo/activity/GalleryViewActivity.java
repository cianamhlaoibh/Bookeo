package ie.bookeo.activity;

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

import ie.bookeo.utils.ShowGallery;
import ie.bookeo.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

import cn.jzvd.JZVideoPlayerStandard;

/**
 * Reference
 *  - URL - https://github.com/sheetalkumar105/androidimagevideogallery
 *  - Creator - Sheetal Kumar Maurya
 *  - Modified by Cian O Sullivan
 *
 * This Activity allows user to swipe through images and videos
 */

public class GalleryViewActivity extends AppCompatActivity {

    public static final int PERMISSION_REQUEST = 111;

    ArrayList<String> paths = new ArrayList<>();
    int position=0;
    private GalleryPagerAdapter gallaryAdapter;
    ViewPager vpager;
    ImageView ivClose;

    int callback;
    public String TAG = GalleryViewActivity.this.getClass().getSimpleName();


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

        position=b.getInt("position",0);
        callback=b.getInt("callback",0);
        paths=  b.getStringArrayList("items");

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        final ImageView _btn_action = (ImageView) findViewById(R.id.ivDelete);
        if(callback==1){
            _btn_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowGallery.actionCallback.onAction( paths.get(position),position);
                    paths.remove(position);
                    gallaryAdapter.notifyDataSetChanged();
                    if(paths.size()==0){
                        _btn_action.setVisibility(View.GONE);
                    }
                }
            });
        }else {
            _btn_action.setVisibility(View.GONE);
        }
        if(paths.size()==0){
            _btn_action.setVisibility(View.GONE);
        }

        if(checkWriteExternalPermission())
            _init();
        else
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


    class GalleryPagerAdapter extends PagerAdapter {

        Context _context;
        LayoutInflater _inflater;

        boolean isFullscreen = false;

        public GalleryPagerAdapter(Context context) {
            _context = context;
            _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return paths.size();
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
            String path = paths.get(position);
            String extension = path.substring(path.lastIndexOf("."));

            Log.e("Extension",extension);

            View itemView = null;
            if(extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")){
                itemView = _inflater.inflate(R.layout.pager_video_item, container, false);
                container.addView(itemView);



                final JZVideoPlayerStandard jzVideoPlayerStandard = (JZVideoPlayerStandard) itemView.findViewById(R.id.videoplayer);
                jzVideoPlayerStandard.setUp(path,
                        JZVideoPlayerStandard.SCREEN_WINDOW_LIST,
                        "");
                Glide.with(_context).load(path)
                        .into(jzVideoPlayerStandard.thumbImageView);



            }else{
                itemView = _inflater.inflate(R.layout.pager_gallery_item, container, false);
                container.addView(itemView);
                final SubsamplingScaleImageView imageView =
                        (SubsamplingScaleImageView) itemView.findViewById(R.id.image);
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(paths.get(position))
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


            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
