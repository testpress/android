package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exam implements Parcelable {
    private String totalMarks;
    private String url;
    private Integer id;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private String duration;
    private Integer numberOfQuestions;
    private String negativeMarks;
    private String markPerQuestion;
    private Integer templateType;
    private String attemptsUrl;
    private List<Attempt> attempts;
    private Map<String, String> additionalProperties = new HashMap<String, String>();

    // Parcelling part
    public Exam(Parcel parcel){
        totalMarks         = parcel.readString();
        url                = parcel.readString();
        id                 = parcel.readInt();
        title              = parcel.readString();
        description        = parcel.readString();
        startDate          = parcel.readString();
        endDate            = parcel.readString();
        duration           = parcel.readString();
        numberOfQuestions  = parcel.readInt();
        negativeMarks      = parcel.readString();
        markPerQuestion      = parcel.readString();
        templateType       = parcel.readInt();
        attemptsUrl        = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(totalMarks);
        parcel.writeString(url);
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(startDate);
        parcel.writeString(endDate);
        parcel.writeString(duration);
        parcel.writeInt(numberOfQuestions);
        parcel.writeString(negativeMarks);
        parcel.writeString(markPerQuestion);
        parcel.writeInt(templateType);
        parcel.writeString(attemptsUrl);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Exam createFromParcel(Parcel in) {
            return new Exam(in);
        }

        public Exam[] newArray(int size) {
            return new Exam[size];
        }
    };

    /**
     *
     * @return
     * The totalMarks
     */
    public String getTotalMarks() {
        return totalMarks;
    }

    /**
     *
     * @param totalMarks
     * The total_marks
     */
    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

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
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

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
     * The startDate
     */
    public String getStartDate() {
        startDate = startDate.substring(0,startDate.length()-10);
        return startDate;
    }

    /**
     *
     * @param startDate
     * The start_date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     *
     * @return
     * The endDate
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     *
     * @param endDate
     * The end_date
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     *
     * @return
     * The duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     *
     * @param duration
     * The duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     *
     * @return
     * The numberOfQuestions
     */
    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public String getNumberOfQuestionsString() {
        return numberOfQuestions.toString();
    }

    /**
     *
     * @param numberOfQuestions
     * The number_of_questions
     */
    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    /**
     *
     * @return
     * The negativeMarks
     */
    public String getNegativeMarks() {
        return negativeMarks;
    }

    /**
     *
     * @param negativeMarks
     * The negative_marks
     */
    public void setNegativeMarks(String negativeMarks) {
        this.negativeMarks = negativeMarks;
    }


    /**
     *
     * @return
     * The mark per question
     */
    public String getMarkPerQuestion() {
        return markPerQuestion;
    }

    /**
     *
     * @param markPerQuestion
     * The mark per question
     */
    public void setMarkPerQuestion(String markPerQuestion) {
        this.markPerQuestion = markPerQuestion;
    }

    /**
     *
     * @return
     * The templateType
     */
    public Integer getTemplateType() {
        return templateType;
    }

    /**
     *
     * @param templateType
     * The template_type
     */
    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }

    /**
     *
     * @return
     * The startUrl
     */
    public String getAttemptsUrl() {
        return attemptsUrl;
    }

    public String getAttemptsFrag() {
        try {
            URL url = new URL(attemptsUrl);
            return url.getFile();
        }
        catch (Exception e) {
            return null;
        }
    }
    /**
     *
     * @param startUrl
     * The start_url
     */
    public void setAttemptsUrl(String startUrl) {
        this.attemptsUrl = startUrl;
    }

    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, String value) {
        this.additionalProperties.put(name, value);
    }

}
