package ie.bookeo.utils;

import java.util.ArrayList;

import ie.bookeo.model.bookeo.BookeoMediaItem;

public interface FirebaseMediaItemsResultListener {
    void OnComplete(ArrayList<BookeoMediaItem> items);
}
