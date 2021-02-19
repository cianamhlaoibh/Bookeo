package ie.bookeo.adapter.bookeo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;
import java.util.List;


import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.model.bookeo.MyCaptionStyle;


/**
 * Reference
 *  - URL - https://github.com/kenglxn/QRGen
 *  - Creator - Ken Gullaksen
 */
public class BookeoPagesAdapter extends RecyclerView.Adapter<BookeoPageHolder>  {
    private List<BookeoPage> pages;
    private Context contx;
    private BookeoMediaItemDao dao;
    private List<BookeoMediaItem> items;

    public BookeoPagesAdapter(List<BookeoPage> pages, List<BookeoMediaItem> items, Context contx) {
        this.pages = pages;
        this.items = items;
        this.contx = contx;
        dao = new BookeoMediaItemDao();
    }

    @NonNull
    @Override
    public BookeoPageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contx).inflate(R.layout.holder_book_page, parent, false);
        return new BookeoPageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookeoPageHolder holder, int position) {
        final BookeoPage page = pages.get(position);
        final BookeoMediaItem item = page.getItem();

                   Glide.with(contx)
                           .load(item.getUrl())
                           .fitCenter()
                           .into(holder.ivMedia);
                   String caption = item.getCaption();
                   if (caption != null) {
                       holder.tvCaption.setText(caption);
                       MyCaptionStyle style = page.getStyle();
                       if (style != null) {
                           style.applyCaptionStyle(style, holder.tvCaption);
                       }
                   }
                   if (item.getEnlarged() == null || item.getEnlarged() == false) {
                       holder.ivMedia.setVisibility(View.VISIBLE);
                       holder.ivMediaLrg.setVisibility(View.GONE);
                       Glide.with(contx).load(item.getUrl()).into(holder.ivMedia);
                   } else if (item.getEnlarged() == true) {
                       holder.ivMediaLrg.setVisibility(View.VISIBLE);
                       holder.ivMedia.setVisibility(View.GONE);
                       Glide.with(contx).load(item.getUrl()).into(holder.ivMediaLrg);
                   }
               //GENERATE QR CODE - IF VIDEO CLIP
               String extension = item.getName().substring(item.getName().lastIndexOf("."));
               if (extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")) {
                   // The data that the QR code will contain
                   String data = item.getUrl();
                   // Create the QR code and display
                   Bitmap qr = createQR(data);
                   Glide.with(contx).load(qr).into(holder.ivQR);
                   holder.ivQR.setVisibility(View.VISIBLE);
               }


            holder.ivPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(contx, ie.bookeo.view.bookeo.BookeoPage.class);
                    intent.putExtra("id", page.getPageUuid());
                    intent.putExtra("position", position);
                    intent.putExtra("albumUuid", page.getAlbumUuid());
                    contx.startActivity(intent);
                }
            });
            //This sets position field when album is first generated
           // if(page.getPageNumber() == -1) {
            //    dao.updatePosition(item.getAlbumUuid(), item.getUuid(), position);
            //}

    }

    // Function to create the QR code
    public static Bitmap createQR(String data){
        Bitmap myBitmap = QRCode.from(data).bitmap();
        return myBitmap;
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }
}
