package ie.bookeo.view.mediaExplorer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.mediaExplorer.MediaFolderAdapter;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.model.gallery_model.AlbumFolder;
import ie.bookeo.model.gallery_model.DeviceMediaItem;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;
import ie.bookeo.view.bookeo.BookeoMediaDisplay;

public class FolderViewFragment extends Fragment implements MediaDisplayItemClickListener {
    RecyclerView rvFolder;
    TextView tvEmpty;
    ArrayList<AlbumFolder> folds;
    private static final int REQUEST_PERMISSION = 1001;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_folder_view, container, false);

        tvEmpty = root.findViewById(R.id.empty);
        rvFolder = root.findViewById(R.id.rvFolders);
        rvFolder.addItemDecoration(new MarginItemDecoration(getContext()));
        rvFolder.hasFixedSize();
        folds = getPicturePaths();
        if(folds.isEmpty()){
            tvEmpty.setVisibility(View.VISIBLE);
        }else{
            RecyclerView.Adapter folderAdapter = new MediaFolderAdapter(folds, getContext(),this);
            rvFolder.setAdapter(folderAdapter);
        }
        changeStatusBarColor();
        //-------------------------------------------------------------------
       // rvAlbums = findViewById(R.id.rvBookeoAlbums);
       // rvAlbums.addItemDecoration(new MarginItemDecoration(getContext()));
       // rvAlbums.hasFixedSize();

        return root;
    }

    /**1
     * @return
     * gets all folders with pictures on the device and loads each of them in a custom object imageFolder
     * the returns an ArrayList of these custom objects
     */
    private ArrayList<AlbumFolder> getPicturePaths(){
        ArrayList<AlbumFolder> imgFolders = new ArrayList<>();
        ArrayList<String> imgPaths = new ArrayList<>();
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA , MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = getContext().getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do{
                AlbumFolder folds = new AlbumFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));
                folderpaths = folderpaths+folder+"/";
                if (!imgPaths.contains(folderpaths)) {
                    imgPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setName(folder);
                    folds.setFirstItem(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.add();
                    imgFolders.add(folds);
                }else{
                    for(int i = 0;i<imgFolders.size();i++){
                        if(imgFolders.get(i).getPath().equals(folderpaths)){
                            imgFolders.get(i).setFirstItem(datapath);
                            imgFolders.get(i).add();
                        }
                    }
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri allVideouri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] vidProjection = { MediaStore.Video.VideoColumns.DATA , MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.BUCKET_ID};
        Cursor vidCursor = getContext().getContentResolver().query(allVideouri, vidProjection, null, null, null);
        try {
            if (vidCursor != null) {
                vidCursor.moveToFirst();
            }
            do{
                AlbumFolder folds = new AlbumFolder();
                String name = vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = vidCursor.getString(vidCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));
                folderpaths = folderpaths+folder+"/";
                if (!imgPaths.contains(folderpaths)) {
                    imgPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setName(folder);
                    folds.setFirstItem(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.add();
                    imgFolders.add(folds);
                }else{
                    for(int i = 0;i<imgFolders.size();i++){
                        if(imgFolders.get(i).getPath().equals(folderpaths)){
                            imgFolders.get(i).setFirstItem(datapath);
                            imgFolders.get(i).add();
                        }
                    }
                }
            }while(vidCursor.moveToNext());
            vidCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgFolders;
    }

    @Override
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> paths, Context contx) {

    }

    @Override
    public void onBPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> uuid, String albumUuid) {

    }

    /**
     * Each time an item in the RecyclerView is clicked this method from the implementation of the transitListerner
     * in this activity is executed, this is possible because this class is passed as a parameter in the creation
     * of the RecyclerView's Adapter, see the adapter class to understand better what is happening here
     * @param pictureFolderPath a String corresponding to a folder path on the device external storage
     */
    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {
        Intent move = new Intent(getContext(), MediaDisplayActivity.class);
        move.putExtra("folderPath",pictureFolderPath);
        move.putExtra("folderName",folderName);

        //move.putExtra("recyclerItemSize",getCardsOptimalWidth(4));
        startActivity(move);
    }

    @Override
    public void onDrivePicClicked(MediaAdapterHolder holder, String names, String urls, String ids, int position) {

    }

    @Override
    public void onBPicClicked(String albumUuid, String AlbumName) {
        Intent move = new Intent(getContext(), BookeoMediaDisplay.class);
        move.putExtra("folderUuid",albumUuid);
        move.putExtra("folderName",AlbumName);
        startActivity(move);
    }

    @Override
    public void onLongPress(MediaAdapterHolder view, DeviceMediaItem item, int position) {

    }

    @Override
    public void onLongPress(MediaAdapterHolder holder, GoogleDriveMediaItem item, int position) {

    }

    /**
     * Default status bar height 24dp,with code API level 24
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeStatusBarColor() {
        Window window = getActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.black));

    }
    @Override
    public void onResume() {
        super.onResume();
        ArrayList<AlbumFolder> folds = getPicturePaths();
        if(folds.isEmpty()){
            tvEmpty.setVisibility(View.VISIBLE);
        }else{
            RecyclerView.Adapter folderAdapter = new MediaFolderAdapter(folds, getContext(),this);
            rvFolder.setAdapter(folderAdapter);
        }
    }

}
