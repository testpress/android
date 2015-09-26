package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Notes implements Parcelable {

    private Integer id;
    private String url;
    private String title;
    private String slug;
    private String description;
    private String attachment;
    private String topicsUrl;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    // Parcelling part
    public Notes(Parcel parcel){
        url         = parcel.readString();
        title       = parcel.readString();
        id          = parcel.readInt();
        slug        = parcel.readString();
        description = parcel.readString();
        attachment  = parcel.readString();
        topicsUrl    = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(title);
        parcel.writeInt(id);
        parcel.writeString(slug);
        parcel.writeString(description);
        parcel.writeString(attachment);
        parcel.writeString(topicsUrl);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Notes createFromParcel(Parcel in) {
            return new Notes(in);
        }

        public Notes[] newArray(int size) {
            return new Notes[size];
        }
    };

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     *
     * @param slug
     * The slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The attachment
     */
    public String getAttachment() {
        return attachment;
    }

    /**
     *
     * @param attachment
     * The attachment
     */
    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    /**
     *
     * @return
     * The topicsUrl
     */
    public String getTopicsUrl() {
        return topicsUrl;
    }

    /**
     *
     * @param topicsUrl
     * The topics_url
     */
    public void setTopicsUrl(String topicsUrl) {
        this.topicsUrl = topicsUrl;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
