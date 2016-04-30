package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductImage implements Parcelable{
    private String original;
    private String medium;
    private String small;

    // Parcelling part
    public ProductImage(Parcel parcel){
        original = parcel.readString();
        medium   = parcel.readString();
        small    = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(original);
        parcel.writeString(medium);
        parcel.writeString(small);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ProductImage createFromParcel(Parcel in) {
            return new ProductImage(in);
        }

        public ProductImage[] newArray(int size) {
            return new ProductImage[size];
        }
    };

    public String getOriginal() { return original; }
    public void setOriginal(String original) { this.original = original; }

    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }

    public String getSmall() { return small; }
    public void setSmall(String small) { this.small = small; }
}
