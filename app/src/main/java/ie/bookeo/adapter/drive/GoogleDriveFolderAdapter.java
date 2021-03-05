package ie.bookeo.adapter.drive;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ie.bookeo.R;
import ie.bookeo.dao.drive.DriveServiceHelper;
import ie.bookeo.model.drive.DriveFolder;
import ie.bookeo.view.drive.DriveMediaDisplay;

public class GoogleDriveFolderAdapter extends RecyclerView.Adapter<FolderHolder> {

    private List<DriveFolder> arFolders;
    private Context contx;

    public void setHelper(DriveServiceHelper helper) {
        this.helper = helper;
    }

    private DriveServiceHelper helper;

    public GoogleDriveFolderAdapter(List<DriveFolder> arFolders, Context folderContx) {
        this.arFolders = arFolders;
        this.contx = folderContx;
    }

    @NonNull
    @Override
    public FolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contx).inflate(R.layout.holder_drive_folder, parent, false);
        return new FolderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderHolder holder, int position) {
        final DriveFolder folder = arFolders.get(position);

        String text = ""+ folder.getName();
        holder.tvFolderName.setText(text);

        holder.tvFolderName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(contx, DriveMediaDisplay.class);
                intent.putExtra("folderId", folder.getId());
                intent.putExtra("folderName", folder.getName());
                contx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arFolders.size();
    }
}
