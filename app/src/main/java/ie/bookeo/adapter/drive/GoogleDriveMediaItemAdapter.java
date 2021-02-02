package ie.bookeo.adapter.drive;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.dao.drive.DriveServiceHelper;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.model.gallery_model.DeviceMediaItem;
import ie.bookeo.utils.GoogleDriveDownload;
import ie.bookeo.utils.LoadListener;
import ie.bookeo.utils.MediaDisplayItemClickListener;

import static androidx.core.view.ViewCompat.setTransitionName;
import static ie.bookeo.dao.drive.DriveServiceHelper.mDriveService;
import static ie.bookeo.dao.drive.DriveServiceHelper.credential;

public class GoogleDriveMediaItemAdapter extends  RecyclerView.Adapter<MediaAdapterHolder> implements LoadListener {

    private ArrayList<GoogleDriveMediaItem> arMediaList;
    private Context contx;
    private MediaDisplayItemClickListener mediaDisplayItemListener = null;
    //private DriveServiceHelper driveServiceHelper;
    //
    DriveServiceHelper driveServiceHelper = new DriveServiceHelper();

    ArrayList<String> fileIds;

    private SparseBooleanArray selectedItems;
    private ArrayList<GoogleDriveMediaItem> arSelectedItems;
    private int selectedIndex = -1;

    byte[] data;

    public GoogleDriveMediaItemAdapter() {
    }

    public GoogleDriveMediaItemAdapter(ArrayList<GoogleDriveMediaItem> arMediaList, Context contx, MediaDisplayItemClickListener mediaDisplayItemListener) {
        this.arMediaList = arMediaList;
        this.contx = contx;
        this.mediaDisplayItemListener = mediaDisplayItemListener;
        selectedItems = new SparseBooleanArray();
        arSelectedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public MediaAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cell = inflater.inflate(R.layout.pic_holder_item, parent, false);
        return new MediaAdapterHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull final MediaAdapterHolder holder, final int position) {
        final GoogleDriveMediaItem file = arMediaList.get(position);

        final ArrayList<String> names = new ArrayList<>();
        for (GoogleDriveMediaItem p : arMediaList) {
            names.add(p.getName());
        }
//
        final ArrayList<String> urls = new ArrayList<>();
        for (GoogleDriveMediaItem p : arMediaList) {
            urls.add(p.getThumbnailLink());
        }

        final ArrayList<String> ids = new ArrayList<>();
        for (GoogleDriveMediaItem p : arMediaList) {
            ids.add(p.getFileId());
        }
        //reset data value
        data = null;
        RequestOptions options = new RequestOptions();
        options.skipMemoryCache(true);
        options.diskCacheStrategy(DiskCacheStrategy.NONE);
       // mDriveService.files().get(file.getFileId()).buildHttpRequestUrl())

        Glide.with(contx)
               // .load(driveServiceHelper.executeDownload(arMediaList.get(position).getUuid()))
                .load(file.getThumbnailLink())
                //.apply(new RequestOptions().centerCrop())
                .apply(options)
                .into(holder.picture);


        //setTransitionName(holder.picture, String.valueOf(position) + "_image");

        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaDisplayItemListener == null)return;
                mediaDisplayItemListener.onDrivePicClicked(holder, arMediaList.get(position).getName(), arMediaList.get(position).getThumbnailLink(), arMediaList.get(position).getFileId(), position);
            }
        });
        holder.picture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mediaDisplayItemListener == null) {
                    return false;
                } else {
                    mediaDisplayItemListener.onLongPress(holder, arMediaList.get(position), position);
                    return true;
                }
            }
        });
        toggleIcon(holder, position);
    }

    @Override
    public void OnSuccess(byte[] data) {
        this.data = data;
    }

    @Override
    public void OnComplete(Object obj) {

    }

    private byte[] downloadFile(String id, LoadListener loadListener) {
        //final ArrayList<byte[]> data = new ArrayList<>();
        driveServiceHelper.downloadFile(id)
            .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                   loadListener.OnSuccess(task.getResult());
                }
            });
        return data;
    }

    @Override
    public int getItemCount() {
        return arMediaList.size();
    }


    public void updateDataSet(ArrayList<GoogleDriveMediaItem> list) {
        arMediaList.clear();
        arMediaList.addAll(list);
        this.notifyDataSetChanged();
    }

    /*
     This method will trigger when we we long press the item and it will change the icon of the item to check icon.
   */
    public void toggleIcon(MediaAdapterHolder holder, int position) {
        if (selectedItems.get(position, false)) {
            //bi.lytImage.setVisibility(View.GONE);
            holder.ivCheck.setVisibility(View.VISIBLE);
            if (selectedIndex == position) selectedIndex = -1;
        } else {
            //bi.lytImage.setVisibility(View.VISIBLE);
            holder.ivCheck.setVisibility(View.GONE);
            if (selectedIndex == position) selectedIndex = -1;
        }
    }

    public void removeIcons() {

    }

    /*
       This method helps you to get all selected items from the list
     */

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public List<GoogleDriveMediaItem> getUploadItems() {
        return arSelectedItems;
    }

    /*
       this will be used when we want to delete items from our list
     */
    public void removeItems(int position) {
        arSelectedItems.remove(position);
        arSelectedItems = null;
        selectedItems.delete(position);
        selectedIndex = -1;
    }

    /*
       for clearing our selection
     */

    public void clearSelection() {
        selectedItems.clear();
        arSelectedItems.clear();
        notifyDataSetChanged();
    }

    /*
             this function will toggle the selection of items
     */

    public void toggleSelection(int position) {
        selectedIndex = position;
        if (selectedItems.get(position, false)) {
            if(arSelectedItems.size()==1){
                arSelectedItems.clear();
                selectedItems.clear();
            }else {
                arSelectedItems.remove(selectedIndex);
                selectedItems.delete(position);
                Log.d("ITEM DESELECTED", "Removed from arraylist");
            }
        } else {
            selectedItems.put(position, true);
            arSelectedItems.add(arMediaList.get(position));
            Log.d("ITEM SELECTED", "Added to arraylist");
        }
        notifyItemChanged(position);
    }

    /*
      How many items have been selected? this method exactly the same . this will return a total number of selected items.
     */

    public int selectedItemCount() {
        return selectedItems.size();
    }
}

