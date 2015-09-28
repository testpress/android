package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    protected String title;
    protected String summary;
    protected String content_html;

    public Post() {}

    public Post(Parcel parcel) {
        title = parcel.readString();
        summary = parcel.readString();
        content_html = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(summary);
        parcel.writeString(content_html);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Post createFromParcel(Parcel parcel) {
            return new Post(parcel);
        }

        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public void setContent_html(String content_html) {
        this.content_html = content_html;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent_html() {
        return content_html;
    }

    public String getSummary() {
        return summary;
    }

    public String getTitle() {
        return title;
    }
}
