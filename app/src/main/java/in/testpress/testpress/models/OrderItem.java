package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {

    private String product;
    private Integer quantity;
    private String price;

    public OrderItem(){}

    // Parcelling part
    public OrderItem(Parcel parcel){
        product  = parcel.readString();
        price    = parcel.readString();
        quantity = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(product);
        parcel.writeString(price);
        parcel.writeInt(quantity);
    }

    public static final Creator CREATOR = new Creator() {
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };

    /**
     *
     * @return
     * The product
     */
    public String getProduct() {
        return product;
    }

    /**
     *
     * @param product
     * The product
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     *
     * @return
     * The quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     *
     * @param quantity
     * The quantity
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

}