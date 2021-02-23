package ie.bookeo.utils;

import java.util.ArrayList;

import ie.bookeo.model.bookeo.BookeoPage;

public interface FirebasePageResultListener {
    void onComplete(BookeoPage item);
    void onComplete(ArrayList<BookeoPage> pages);
}
