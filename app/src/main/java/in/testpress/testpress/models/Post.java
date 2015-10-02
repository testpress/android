package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    protected String title;
    protected String summary;
    protected String contentHtml;
    protected String url;
    protected Category category;
    protected Integer id;
    protected Boolean active;
    protected Integer institute;
    protected String created;
    protected String modified;

    public Post() {}

    public Post(Parcel parcel) {
        title       = parcel.readString();
        summary     = parcel.readString();
        contentHtml = parcel.readString();
        url         = parcel.readString();
        category    = parcel.readParcelable(Category.class.getClassLoader());
        id          = parcel.readInt();
        active      = parcel.readByte() != 0;
        created     = parcel.readString();
        modified    = parcel.readString();
        institute   = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(summary);
        parcel.writeString(contentHtml);
        parcel.writeString(url);
        parcel.writeParcelable(category, i);
        parcel.writeInt(id);
        parcel.writeByte((byte) (active ? 1 : 0));
        parcel.writeString(created);
        parcel.writeString(modified);
        parcel.writeInt(institute);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Post createFromParcel(Parcel parcel) {
            return new Post(parcel);
        }

        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public Integer getInstitute() {
        return institute;
    }

    public void setInstitute(Integer institute) {
        this.institute = institute;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }
}
