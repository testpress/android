package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
public class Attempt implements Parcelable {
    private String url;
    private Integer id;
    private Exam exam;
    private String user;
    private String date;
    private Integer totalQuestions;
    private String score;
    private String reviewUrl;
    private String questionsUrl;
    private Integer correctCount;
    private Integer incorrectCount;
    private String lastStartedTime;
    private String remainingTime;
    private String state;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    // Parcelling part
    public Attempt(Parcel parcel){
        exam = (Exam) parcel.readParcelable(Exam.class.getClassLoader());
        id = parcel.readInt();
        url = parcel.readString();
        user = parcel.readString();
        date = parcel.readString();
        totalQuestions = parcel.readInt();
        score = parcel.readString();
        reviewUrl = parcel.readString();
        questionsUrl = parcel.readString();
        correctCount = parcel.readInt();
        incorrectCount = parcel.readInt();
        lastStartedTime = parcel.readString();
        remainingTime = parcel.readString();
        state = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(exam, i);
        parcel.writeInt(id);
        parcel.writeString(url);
        parcel.writeString(user);
        parcel.writeString(date);
        parcel.writeInt(totalQuestions);
        parcel.writeString(score);
        parcel.writeString(reviewUrl);
        parcel.writeString(questionsUrl);
        parcel.writeInt(correctCount);
        parcel.writeInt(incorrectCount);
        parcel.writeString(lastStartedTime);
        parcel.writeString(remainingTime);
        parcel.writeString(state);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Attempt createFromParcel(Parcel parcel) {
            return new Attempt(parcel);
        }

        public Attempt[] newArray(int size) {
            return new Attempt[size];
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

    public String getUrlFrag() {
        try {
            URL fragUrl = new URL(url);
            return fragUrl.getFile();
        }
        catch (Exception e) {
            return null;
        }
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
     * The exam
     */
    public Exam getExam() {
        return exam;
    }

    /**
     *
     * @param exam
     * The exam
     */
    public void setExam(Exam exam) {
        this.exam = exam;
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
     * The totalQuestions
     */
    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    /**
     *
     * @param totalQuestions
     * The total_questions
     */
    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    /**
     *
     * @return
     * The score
     */
    public String getScore() {
        return score;
    }

    /**
     *
     * @param score
     * The score
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     *
     * @return
     * The reviewUrl
     */
    public String getReviewUrl() {
        return reviewUrl;
    }

    /**
     *
     * @param reviewUrl
     * The review_url
     */
    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    /**
     *
     * @return
     * The questionsUrl
     */
    public String getQuestionsUrl() {
        return questionsUrl;
    }

    /**
     *
     * @param questionsUrl
     * The questions_url
     */
    public void setQuestionsUrl(String questionsUrl) {
        this.questionsUrl = questionsUrl;
    }

    /**
     *
     * @return
     * The correctCount
     */
    public Integer getCorrectCount() {
        return correctCount;
    }

    /**
     *
     * @param correctCount
     * The correct_count
     */
    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    /**
     *
     * @return
     * The incorrectCount
     */
    public Integer getIncorrectCount() {
        return incorrectCount;
    }

    /**
     *
     * @param incorrectCount
     * The incorrect_count
     */
    public void setIncorrectCount(Integer incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    /**
     *
     * @return
     * The lastStartedTime
     */
    public String getLastStartedTime() {
        return lastStartedTime;
    }

    /**
     *
     * @param lastStartedTime
     * The last_started_time
     */
    public void setLastStartedTime(String lastStartedTime) {
        this.lastStartedTime = lastStartedTime;
    }

    /**
     *
     * @return
     * The remainingTime
     */
    public String getRemainingTime() {
        return remainingTime;
    }

    /**
     *
     * @param remainingTime
     * The remaining_time
     */
    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    /**
     *
     * @return
     * The state
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @param state
     * The state
     */
    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
