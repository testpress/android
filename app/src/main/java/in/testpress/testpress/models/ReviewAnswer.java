package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ReviewAnswer extends AttemptAnswer {
    private Boolean isCorrect;

    // Parcelling part
    public ReviewAnswer(Parcel parcel){
        textHtml = parcel.readString();
        id = parcel.readInt();
        isCorrect = parcel.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(textHtml);
        parcel.writeInt(id);
        if (isCorrect == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (isCorrect ? 1 : 0)); //if review == true, byte == 1
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ReviewAnswer createFromParcel(Parcel parcel) {
            return new ReviewAnswer(parcel);
        }

        public ReviewAnswer[] newArray(int size) {
            return new ReviewAnswer[size];
        }
    };


    /**
     *
     * @return
     * Is correct
     */
    public Boolean getIsCorrect() {
        return isCorrect;
    }

    /**
     *
     * @param isCorrect
     * Is correct
     */
    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
