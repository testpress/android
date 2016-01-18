package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ProductDetails extends Product implements Parcelable {

    private String description;
    private String additionalInfo;
    private String paymentLink;
    private String institute;
    private Boolean requiresShipping;
    private List<RawExam> exams = new ArrayList<RawExam>();
    private List<Notes> notes = new ArrayList<Notes>();

    // Parcelling part
    public ProductDetails(Parcel parcel){
        super(parcel);
        description      = parcel.readString();
        additionalInfo   = parcel.readString();
        paymentLink      = parcel.readString();
        institute        = parcel.readString();
        requiresShipping = parcel.readByte() != 0;
        parcel.readTypedList(exams, RawExam.CREATOR);
        parcel.readTypedList(notes, Notes.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(description);
        parcel.writeString(additionalInfo);
        parcel.writeString(paymentLink);
        parcel.writeString(institute);
        parcel.writeByte((byte) (requiresShipping ? 1 : 0));
        parcel.writeTypedList(exams);
        parcel.writeTypedList(notes);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ProductDetails createFromParcel(Parcel in) {
            return new ProductDetails(in);
        }

        public ProductDetails[] newArray(int size) {
            return new ProductDetails[size];
        }
    };

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
     * The description
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     *
     * @param additionalInfo
     * The description
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     *
     * @return
     * The description
     */
    public String getPaymentLink() {
        return paymentLink;
    }

    /**
     *
     * @param paymentLink
     * The description
     */
    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    /**
     *
     * @return
     * The institute
     */
    public String getInstitute() {
        return institute;
    }

    /**
     *
     * @param institute
     * The institute
     */
    public void setInstitute(String institute) {
        this.institute = institute;
    }

    /**
     *
     * @return
     * The requiresShipping
     */
    public Boolean getRequiresShipping() {
        return requiresShipping;
    }

    /**
     *
     * @param requiresShipping
     * The requires_shipping
     */
    public void setRequiresShipping(Boolean requiresShipping) {
        this.requiresShipping = requiresShipping;
    }

    /**
     *
     * @return
     * The exams
     */
    public List<RawExam> getExams() {
        return exams;
    }

    /**
     *
     * @param exams
     * The exams
     */
    public void setExams(List<RawExam> exams) {
        this.exams = exams;
    }

    /**
     *
     * @return
     * The notes
     */
    public List<Notes> getNotes() {
        return notes;
    }

    /**
     *
     * @param notes
     * The notes
     */
    public void setNotes(List<Notes> notes) {
        this.notes = notes;
    }
}
