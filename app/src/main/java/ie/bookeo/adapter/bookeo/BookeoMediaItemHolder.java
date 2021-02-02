package ie.bookeo.adapter.bookeo;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.R;

public class BookeoMediaItemHolder extends RecyclerView.ViewHolder{

    public ImageView picture;


    //ListItemBinding bi;

    BookeoMediaItemHolder(@NonNull View itemView) {
        super(itemView);

        picture = itemView.findViewById(R.id.image);

    }
}

