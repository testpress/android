package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order implements Parcelable {

    private String url;
    private Integer id;
    private String date;
    private String user;
    private String status;
    private String email;
    private String name;
    private String phone;
    private String shippingAddress;
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();
    private String amount;
    private String mobileSdkHash;
    private String checksum;
    private String apikey;
    private String zip;
    private String landMark;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    // Parcelling part
    public Order(Parcel parcel){
        url     = parcel.readString();
        date    = parcel.readString();
        id      = parcel.readInt();
        user    = parcel.readString();
        status  = parcel.readString();
        email   = parcel.readString();
        name    = parcel.readString();
        phone   = parcel.readString();
        shippingAddress = parcel.readString();
        amount   = parcel.readString();
        checksum = parcel.readString();
        mobileSdkHash = parcel.readString();
        apikey   = parcel.readString();
        zip      = parcel.readString();
        landMark = parcel.readString();
        orderItems = new ArrayList<OrderItem>();
        parcel.readTypedList(orderItems, OrderItem.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(date);
        parcel.writeInt(id);
        parcel.writeString(user);
        parcel.writeString(status);
        parcel.writeString(email);
        parcel.writeString(name);
        parcel.writeString(phone);
        parcel.writeString(shippingAddress);
        parcel.writeString(amount);
        parcel.writeString(checksum);
        parcel.writeString(mobileSdkHash);
        parcel.writeString(apikey);
        parcel.writeString(zip);
        parcel.writeString(landMark);
        parcel.writeTypedList(orderItems);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

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
     * The date
     */
    public String getDate() {
        return date;
    }

    /**
     *
     * @param date
     * The date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     *
     * @return
     * The user
     */
    public String getUser() {
        return user;
    }

    /**
     *
     * @param user
     * The user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     *
     * @return
     * The status
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     * The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(String email) {
        this.email = email;
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
     * The phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     *
     * @param phone
     * The phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     *
     * @return
     * The shippingAddress
     */
    public String getShippingAddress() {
        return shippingAddress;
    }

    /**
     *
     * @param shippingAddress
     * The shipping_address
     */
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    /**
     *
     * @return
     * The orderItems
     */
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    /**
     *
     * @param orderItems
     * The order_items
     */
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    /**
     *
     * @return
     * The amount
     */
    public String getAmount() {
        return amount;
    }

    /**
     *
     * @param amount
     * The amount
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     *
     * @return
     * The checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     *
     * @param checksum
     * The checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     *
     * @return
     * The checksum
     */
    public String getMobileSdkHash() {
        return mobileSdkHash;
    }

    /**
     *
     * @param mobileSdkHash
     * The checksum
     */
    public void setMobileSdkHash(String mobileSdkHash) {
        this.mobileSdkHash = mobileSdkHash;
    }

    /**
     *
     * @return
     * The apikey
     */
    public String getApikey() {
        return apikey;
    }

    /**
     *
     * @param apikey
     * The apikey
     */
    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    /**
     *
     * @return
     * The zip
     */
    public String getZip() {
        return zip;
    }

    /**
     *
     * @param zip
     * The zip
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     *
     * @return
     * The landMark
     */
    public String getLandMark() {
        return landMark;
    }

    /**
     *
     * @param landMark
     * The land_mark
     */
    public void setLandMark(String landMark) {
        this.landMark = landMark;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}