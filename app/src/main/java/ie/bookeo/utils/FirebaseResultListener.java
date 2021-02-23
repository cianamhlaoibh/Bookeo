package ie.bookeo.utils;

import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;

public interface FirebaseResultListener {
    void onComplete(BookeoAlbum album);
}
