package ie.bookeo.adapter.bookeo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import ie.bookeo.R;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.utils.MediaDisplayItemClickListener;

/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 * This is the adapter class for the MainActivity Recycler View that populates a RecyclerView with folder from device.
 * This class also contain the Folder View Holder which represent a folder on the screen
 */

public class BookeoMainFolderAdapter extends RecyclerView.Adapter<BookeoMainFolderAdapter.FolderHolder>{

    private ArrayList<BookeoAlbum> arFolders;
    private Context contx;
    private MediaDisplayItemClickListener mediaDisplayItemListener;

    /**
     *
     * @param folders An ArrayList of String that represents paths to folders on the external storage that contain pictures
     * @param folderContx The Activity or fragment Context
     * @param listen interFace for communication between adapter and fragment or activity
     */
    public BookeoMainFolderAdapter(ArrayList<BookeoAlbum> folders, Context folderContx, MediaDisplayItemClickListener listen) {
        this.arFolders = folders;
        this.contx = folderContx;
        this.mediaDisplayItemListener = listen;
    }

    @NonNull
    @Override
    public FolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cell = inflater.inflate(R.layout.picture_folder_item, parent, false);
        return new FolderHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderHolder holder, int position) {
        final BookeoAlbum folder = arFolders.get(position);

        if(folder.getFirstItem() != null) {
            Glide.with(contx)
                    .load(folder.getFirstItem())
                    .apply(new RequestOptions().centerCrop())
                    .into(holder.folderPic);
        }else{
            Glide.with(contx)
                    .load(R.drawable.ic_photo_library)
                    .apply(new RequestOptions().centerCrop())
                    .into(holder.folderPic);
        }
        //setting the number of images
        String text = ""+folder.getName();
        //String folderSizeString=""+folder.getCount()+" Media";
        //holder.folderSize.setText(folderSizeString);
        holder.folderName.setText(text);

        holder.folderPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaDisplayItemListener.onBPicClicked(folder.getUuid(),folder.getName());
            }
        });

    }

    @Override
    public int getItemCount() {
        return arFolders.size();
    }


    public class FolderHolder extends RecyclerView.ViewHolder{
        ImageView folderPic;
        TextView folderName;
        //set textview for foldersize
        TextView folderSize;

        CardView folderCard;

        public FolderHolder(@NonNull View itemView) {
            super(itemView);
            folderPic = itemView.findViewById(R.id.folderPic);
            folderName = itemView.findViewById(R.id.folderName);
            folderSize=itemView.findViewById(R.id.folderSize);
            folderCard = itemView.findViewById(R.id.folderCard);
        }
    }

}
