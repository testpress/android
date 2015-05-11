package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "ReviewAnswer")
public class ReviewAnswer extends Model implements Parcelable {
    @Column(name = "textHtml")
    protected String textHtml;
    @Column(name = "answerId")
    protected Integer id;
    @Column(name = "isCorrect")
    private Boolean isCorrect;
    @Column(name = "ReviewItem", onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    public ReviewItem reviewItem;
    @Column(name = "filter")
    public String filter;

    public ReviewAnswer() {
        super();
    }

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

    public String getTextHtml() {
        return textHtml;
    }

    /**
     *
     * @param textHtml
     * The text_html
     */
    public void setTextHtml(String textHtml) {
        this.textHtml = textHtml;
    }

    /**
     *
     * @return
     * The id
     */
    public Integer getAnswerId() {
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
}
