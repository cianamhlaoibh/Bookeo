package ie.bookeo.utils;

import java.util.ArrayList;

import ie.bookeo.model.bookeo.BookeoAlbum;

public interface SubFolderResultListener {
    void onSubFolderResult(ArrayList<BookeoAlbum> albums);
}
