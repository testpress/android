package in.testpress.testpress.models;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Parcel;
import android.os.Parcelable;

public class AttemptQuestion implements Parcelable {

    private String questionHtml;
    private List<AttemptAnswer> answers = new ArrayList<AttemptAnswer>();
    private String subject;
    private String type;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    // Parcelling part
    public AttemptQuestion(Parcel parcel){
        answers = new ArrayList<AttemptAnswer>();
        parcel.readTypedList(answers, AttemptAnswer.CREATOR);
        questionHtml = parcel.readString();
        subject = parcel.readString();
        type = parcel.readString();
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
        parcel.writeString(type);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public AttemptQuestion createFromParcel(Parcel parcel) {
            return new AttemptQuestion(parcel);
        }

        public AttemptQuestion[] newArray(int size) {
            return new AttemptQuestion[size];
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
    public List<AttemptAnswer> getAttemptAnswers() {
        return answers;
    }

    /**
     *
     * @param attemptAnswers
     * The answers
     */
    public void setAttemptAnswers(List<AttemptAnswer> attemptAnswers) {
        this.answers = attemptAnswers;
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
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}