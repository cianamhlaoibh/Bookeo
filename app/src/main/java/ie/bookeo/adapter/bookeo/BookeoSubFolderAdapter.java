package ie.bookeo.adapter.bookeo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.R;
import ie.bookeo.adapter.drive.FolderHolder;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.drive.DriveFolder;
import ie.bookeo.view.bookeo.BookeoMediaDisplay;
import ie.bookeo.view.drive.DriveMediaDisplay;

public class BookeoSubFolderAdapter extends RecyclerView.Adapter<FolderHolder> {

    private ArrayList<BookeoAlbum> arFolders;
    private Context contx;


    public BookeoSubFolderAdapter(ArrayList<BookeoAlbum> arFolders, Context folderContx) {
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
        BookeoAlbum folder = arFolders.get(position);

        String text = folder.getName();
        holder.tvFolderName.setText(text);

        holder.tvFolderName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(contx, BookeoMediaDisplay.class);
                intent.putExtra("folderUuid", folder.getUuid());
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

