package ie.bookeo.utils;

public interface LoadListener {
    void OnSuccess(byte[] data);
    void OnComplete(String url);
}
