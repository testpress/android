package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ExamCategory implements Parcelable {

    private Integer id;
    private String url;
    private String name;
    private String description;
    private String slug;
    private String parentUrl;
    private Boolean leaf;

    protected ExamCategory(Parcel in) {
        url = in.readString();
        name = in.readString();
        description = in.readString();
        slug = in.readString();
        parentUrl = in.readString();
    }

    public static final Creator<ExamCategory> CREATOR = new Creator<ExamCategory>() {
        @Override
        public ExamCategory createFromParcel(Parcel in) {
            return new ExamCategory(in);
        }

        @Override
        public ExamCategory[] newArray(int size) {
            return new ExamCategory[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(slug);
        dest.writeString(parentUrl);
    }

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
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
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
     * The parentUrl
     */
    public String getParentUrl() {
        return parentUrl;
    }

    /**
     *
     * @param parentUrl
     * The parent_url
     */
    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    /**
     *
     * @return
     * The leaf
     */
    public Boolean getLeaf() {
        return leaf;
    }

    /**
     *
     * @param leaf
     * The leaf
     */
    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

}
