package in.testpress.testpress.models;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Parcel;
import android.os.Parcelable;

public class ReviewQuestion implements Parcelable {

    private String questionHtml;
    private List<ReviewAnswer> answers = new ArrayList<ReviewAnswer>();
    private String subject;
    private String explanationHtml;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    // Parcelling part
    public ReviewQuestion(Parcel parcel){
        answers = new ArrayList<ReviewAnswer>();
        parcel.readTypedList(answers, ReviewAnswer.CREATOR);
        questionHtml = parcel.readString();
        subject = parcel.readString();
        explanationHtml = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(answers);
        parcel.writeString(questionHtml);
        parcel.writeString(subject);
        parcel.writeString(explanationHtml);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ReviewQuestion createFromParcel(Parcel parcel) {
            return new ReviewQuestion(parcel);
        }

        public ReviewQuestion[] newArray(int size) {
            return new ReviewQuestion[size];
        }
    };

    /**
     *
     * @return
     * The questionHtml
     */
    public String getQuestionHtml() {
        return questionHtml;
    }

    /**
     *
     * @param questionHtml
     * The question_html
     */
    public void setQuestionHtml(String questionHtml) {
        this.questionHtml = questionHtml;
    }

    /**
     *
     * @return
     * The answers
     */
    public List<ReviewAnswer> getAnswers() {
        return answers;
    }

    /**
     *
     * @param answers
     * The answers
     */
    public void setAnswers(List<ReviewAnswer> answers) {
        this.answers = answers;
    }

    /**
     *
     * @return
     * The subject
     */
    public Object getSubject() {
        return subject;
    }

    /**
     *
     * @param subject
     * The subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     *
     * @return
     * The explanationHtml
     */
    public String getExplanationHtml() {
        return explanationHtml;
    }

    /**
     *
     * @param explanationHtml
     * The explanationHtml
     */
    public void setExplanationHtml(String explanationHtml) {
        this.explanationHtml = explanationHtml;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}