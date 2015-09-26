package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product implements Parcelable {

    private Integer id;
    private String url;
    private String title;
    private String slug;
    private String image;
    private String startDate;
    private String endDate;
    private List<String> categories = new ArrayList<String>();
    private List<String> types = new ArrayList<String>();
    private Integer examsCount;
    private Integer notesCount;
    private String price;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    // Parcelling part
    public Product(Parcel parcel){
        url        = parcel.readString();
        title      = parcel.readString();
        id         = parcel.readInt();
        slug       = parcel.readString();
        image      = parcel.readString();
        startDate  = parcel.readString();
        endDate    = parcel.readString();
        price      = parcel.readString();
        parcel.readStringList(categories);
        parcel.readStringList(types);
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
        parcel.writeString(image);
        parcel.writeString(startDate);
        parcel.writeString(endDate);
        parcel.writeString(price);
        parcel.writeStringList(categories);
        parcel.writeStringList(types);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
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
     * The image
     */
    public String getImage() {
        return image;
    }

    /**
     *
     * @param image
     * The image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     *
     * @return
     * The startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     *
     * @param startDate
     * The start_date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     *
     * @return
     * The endDate
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     *
     * @param endDate
     * The end_date
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     *
     * @return
     * The categories
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     *
     * @param categories
     * The categories
     */
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    /**
     *
     * @return
     * The types
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     *
     * @param types
     * The types
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }

    /**
     *
     * @return
     * The examsCount
     */
    public Integer getExamsCount() {
        return examsCount;
    }

    /**
     *
     * @param examsCount
     * The exams_count
     */
    public void setExamsCount(Integer examsCount) {
        this.examsCount = examsCount;
    }

    /**
     *
     * @return
     * The notesCount
     */
    public Integer getNotesCount() {
        return notesCount;
    }

    /**
     *
     * @param notesCount
     * The notes_count
     */
    public void setNotesCount(Integer notesCount) {
        this.notesCount = notesCount;
    }

    /**
     *
     * @return
     * The price
     */
    public String getPrice() {
        return price;
    }

    /**
     *
     * @param price
     * The price
     */
    public void setPrice(String price) {
        this.price = price;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}