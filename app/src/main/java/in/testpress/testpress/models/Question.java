package in.testpress.testpress.models;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Parcel;
import android.os.Parcelable;

public class Question implements Parcelable {

    private String questionHtml;
    private List<Answer> answers = new ArrayList<Answer>();
    private String subject;
    private String type;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    // Parcelling part
    public Question(Parcel parcel){
        answers = new ArrayList<Answer>();
        parcel.readTypedList(answers, Answer.CREATOR);
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
        public Question createFromParcel(Parcel parcel) {
            return new Question(parcel);
        }

        public Question[] newArray(int size) {
            return new Question[size];
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
    public List<Answer> getAnswers() {
        return answers;
    }

    /**
     *
     * @param answers
     * The answers
     */
    public void setAnswers(List<Answer> answers) {
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