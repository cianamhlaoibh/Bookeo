package ie.bookeo.view.bookeo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.dao.bookeo.BookeoPagesDao;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.model.bookeo.MyCaptionStyle;
import ie.bookeo.utils.FirebasePageResultListener;
import ie.bookeo.utils.FirebaseResultListener;

/**
 * References
 *
 * - fetching data from firebase and returning it to the main thread [duplicate]
 * - URL - https://stackoverflow.com/questions/60194810/fetching-data-from-firebase-and-returning-it-to-the-main-thread
 *
 */

public class BookeoPageActivity extends AppCompatActivity implements View.OnClickListener, FirebasePageResultListener {
    String id, albumUuid;
    int postion;
    BookeoPagesDao pagesDao;
    ArrayList<BookeoMediaItem> result;
    ie.bookeo.model.bookeo.BookeoPage page;
    BookeoMediaItem item;
    ImageView ivImageStd, ivImageLrg, ivCaption, ivEnlarge, ivFilter, ivDelete, ivDone, ivQr;
    TextView tvCaption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookeo_page);

        id = getIntent().getStringExtra("id");
        albumUuid = getIntent().getStringExtra("albumUuid");
        postion = getIntent().getIntExtra("position", -1);

        pagesDao = new BookeoPagesDao(this);

        tvCaption = findViewById(R.id.tvCaption);

        ivImageStd = findViewById(R.id.ivImageStd);
        ivImageLrg = findViewById(R.id.ivImageLrg);
        ivQr = findViewById(R.id.ivQR);
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
        pagesDao.getPage(albumUuid, id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivDone:
                pagesDao.updateEnlargement(albumUuid, id, page.getEnlarged());
                finish();
                break;
            case R.id.ivCaption:
                Intent intent = new Intent(this, EditCaptionActivity.class);
                intent.putExtra("albumUuid", albumUuid);
                intent.putExtra("uuid", id);
                startActivity(intent);
                break;
            case R.id.ivFormat:
                checkIsEnlarged(item);
                break;
            case R.id.ivSize:
                break;
            case R.id.ivColor:
                //bookeoMediaItemDao.deleteMediaItem(albumUuid, id, this);
                finish();
                break;
        }
    }

    // Function to create the QR code
    public static Bitmap createQR(String data){
        Bitmap myBitmap = QRCode.from(data).bitmap();
        return myBitmap;
    }

    private void checkIsEnlargedOnLoad(BookeoMediaItem item) {
        //TODO
        if(page.getEnlarged() == null || page.getEnlarged() == false){
            ivImageStd.setVisibility(View.VISIBLE);
            ivImageLrg.setVisibility(View.GONE);
            Glide.with(this).load(item.getUrl()).into(ivImageStd);
        }else if(page.getEnlarged() == true){
            ivImageLrg.setVisibility(View.VISIBLE);
            ivImageStd.setVisibility(View.GONE);
            Glide.with(this).load(item.getUrl()).into(ivImageLrg);
        }
    }


    private void checkIsEnlarged(BookeoMediaItem item) {
        if(page.getEnlarged() == null || page.getEnlarged() == false){
            ivImageStd.setVisibility(View.GONE);
            ivImageLrg.setVisibility(View.VISIBLE);
            Glide.with(this).load(item.getUrl()).into(ivImageLrg);
            page.setEnlarged(true);
        }else if(page.getEnlarged() == true){
            ivImageLrg.setVisibility(View.GONE);
            ivImageStd.setVisibility(View.VISIBLE);
            Glide.with(this).load(item.getUrl()).into(ivImageStd);
            page.setEnlarged(false);
        }
    }


    @Override
    public void onComplete(ie.bookeo.model.bookeo.BookeoPage page) {
        this.page = page;
        this.item = page.getItem();
        if (page.getEnlarged() == null || page.getEnlarged() == false) {
            ivImageLrg.setVisibility(View.GONE);
            Glide.with(this).load(item.getUrl()).into(ivImageStd);
        } else {
            ivImageStd.setVisibility(View.GONE);
            Glide.with(this).load(item.getUrl()).into(ivImageLrg);
        }
        tvCaption.setText(page.getCaption());
        MyCaptionStyle style = page.getStyle();
        if (style != null) {
            if (style != null) {
                style.applyCaptionStyle(style, tvCaption);
            }
        }
        checkIsEnlargedOnLoad(item);
        //GENERATE QR CODE - IF VIDEO CLIP
        String extension = item.getName().substring(item.getName().lastIndexOf("."));
        if (extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")) {
            // The data that the QR code will contain
            String data = item.getUrl();
            // Create the QR code and display
            Bitmap qr = createQR(data);
            Glide.with(this).load(qr).into(ivQr);
            ivQr.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onComplete(ArrayList<BookeoPage> pages) {

    }
}
