package ie.bookeo.view.bookeo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.MyCaptionStyle;
import ie.bookeo.utils.FirebaseResultListener;

/**
 * References
 *
 * - fetching data from firebase and returning it to the main thread [duplicate]
 * - URL - https://stackoverflow.com/questions/60194810/fetching-data-from-firebase-and-returning-it-to-the-main-thread
 *
 */

public class BookeoPage extends AppCompatActivity implements View.OnClickListener, FirebaseResultListener {
    String id, albumUuid;
    int postion;
    BookeoMediaItemDao bookeoMediaItemDao;
    ArrayList<BookeoMediaItem> result;
    BookeoMediaItem item;
    ImageView ivImageStd, ivImageLrg, ivCaption, ivEnlarge, ivFilter, ivDelete, ivDone;
    TextView tvCaption;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookeo_page);

        id = getIntent().getStringExtra("id");
        albumUuid = getIntent().getStringExtra("albumUuid");
        postion = getIntent().getIntExtra("position", -1);

        bookeoMediaItemDao = new BookeoMediaItemDao(this);

        tvCaption = findViewById(R.id.tvCaption);

        ivImageStd = findViewById(R.id.ivImageStd);
        ivImageLrg = findViewById(R.id.ivImageLrg);
        ivDone = findViewById(R.id.ivDone);
        ivCaption = findViewById(R.id.ivCaption);
        ivEnlarge = findViewById(R.id.ivFormat);
        ivFilter = findViewById(R.id.ivSize);
        ivDelete = findViewById(R.id.ivColor);
        ivDone.setOnClickListener(this);
        ivCaption.setOnClickListener(this);
        ivEnlarge.setOnClickListener(this);
        ivFilter.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bookeoMediaItemDao.getMediaItem(id,albumUuid);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivDone:
                bookeoMediaItemDao.updateEnlargement(albumUuid, id,item.getEnlarged());
                finish();
                break;
            case R.id.ivCaption:
                Intent intent = new Intent(this, EditCaptionActivity.class);
                intent.putExtra("albumUuid",albumUuid);
                intent.putExtra("uuid",id);
                startActivity(intent);
                break;
            case R.id.ivFormat:
                if(item.getEnlarged() == null || item.getEnlarged() == false){
                    ivImageStd.setVisibility(View.GONE);
                    ivImageLrg.setVisibility(View.VISIBLE);
                    Glide.with(this).load(item.getUrl()).into(ivImageLrg);
                    item.setEnlarged(true);
                }else{
                    ivImageLrg.setVisibility(View.GONE);
                    ivImageStd.setVisibility(View.VISIBLE);
                    Glide.with(this).load(item.getUrl()).into(ivImageStd);
                    item.setEnlarged(false);
                }
                    break;
            case R.id.ivSize:
                break;
            case R.id.ivColor:
                bookeoMediaItemDao.deleteMediaItem(albumUuid,id,this);
                finish();
                break;
        }
    }

    @Override
    public void onComplete(BookeoMediaItem item) {
        this.item = item;
        if(item.getEnlarged() == null || item.getEnlarged() == false){
            ivImageLrg.setVisibility(View.GONE);
            Glide.with(this).load(item.getUrl()).into(ivImageStd);
        }else{
            ivImageStd.setVisibility(View.GONE);
            Glide.with(this).load(item.getUrl()).into(ivImageLrg);
        }
        tvCaption.setText(item.getCaption());
        MyCaptionStyle style = item.getStyle();
        if(style != null) {
            if (style != null) {
                style.applyCaptionStyle(style, tvCaption);
            }
        }
    }
}
