package ie.bookeo.adapter;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.R;

/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 * This is the viewholder for the MediaAdapter class. Each view holder stores can image
 */

public class MediaAdapterHolder extends RecyclerView.ViewHolder{

    public ImageView picture;
    public ImageView ivCheck;
    public FrameLayout fl;

    //ListItemBinding bi;

    public MediaAdapterHolder(@NonNull View itemView) {
        super(itemView);

        picture = itemView.findViewById(R.id.image);
        ivCheck = itemView.findViewById(R.id.tick_image);
        fl = itemView.findViewById(R.id.framelayout);
    }
}
