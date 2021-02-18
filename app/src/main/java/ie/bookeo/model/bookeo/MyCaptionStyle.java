package ie.bookeo.model.bookeo;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

public class MyCaptionStyle {
    private String size;
    private String format;
    private String color;

    public MyCaptionStyle() {
    }

    public MyCaptionStyle(String format, String size,  String color) {
        this.size = size;
        this.format = format;
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String font) {
        this.format = font;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void applyCaptionStyle(MyCaptionStyle style, TextView tvCaption) {
        switch (style.getFormat()) {
            case "Bold":
                tvCaption.setTypeface(tvCaption.getTypeface(), Typeface.BOLD);
                break;
            case "Italic":
                tvCaption.setTypeface(tvCaption.getTypeface(), Typeface.ITALIC);
                break;
            case "Normal":
                tvCaption.setTypeface(Typeface.create(tvCaption.getTypeface(), Typeface.NORMAL));
                break;
        }
        switch (style.getSize()) {
            case "Small":
                tvCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP,8);
                break;
            case "Medium":
                tvCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
                break;
            case "Large":
                tvCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                break;
        }
        switch (style.getColor()) {
            case "Black":
                tvCaption.setTextColor(Color.parseColor("#000000"));
                break;
            case "Red":
                tvCaption.setTextColor(Color.parseColor("#FF0000"));
                break;
            case "Beige":
                tvCaption.setTextColor(Color.parseColor("#CFB997"));
                break;
            case "Navy":
                tvCaption.setTextColor(Color.parseColor("#000080"));
                break;
        }
    }
}
