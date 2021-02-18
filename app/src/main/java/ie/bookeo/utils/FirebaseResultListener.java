package ie.bookeo.utils;

import ie.bookeo.model.bookeo.BookeoMediaItem;

public interface FirebaseResultListener {
    void onComplete(BookeoMediaItem item);
}
