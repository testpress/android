package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Exam implements Parcelable {
    private String totalMarks;
    private String url;
    private Integer id;
    private String title;
    private String description;
    private String course;
    private String startDate;
    private String endDate;
    private String duration;
    private Integer numberOfQuestions;
    private String negativeMarks;
    private String markPerQuestion;
    private Integer templateType;
    private Boolean allowRetake;
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
        course             = parcel.readString();
        startDate          = parcel.readString();
        endDate            = parcel.readString();
        duration           = parcel.readString();
        numberOfQuestions  = parcel.readInt();
        negativeMarks      = parcel.readString();
        markPerQuestion    = parcel.readString();
        templateType       = parcel.readInt();
        allowRetake        = parcel.readByte() != 0;
        attemptsUrl        = parcel.readString();
        attempts = new ArrayList<Attempt>();
        parcel.readTypedList(attempts, Attempt.CREATOR);
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
        parcel.writeString(course);
        parcel.writeString(startDate);
        parcel.writeString(endDate);
        parcel.writeString(duration);
        parcel.writeInt(numberOfQuestions);
        parcel.writeString(negativeMarks);
        parcel.writeString(markPerQuestion);
        parcel.writeInt(templateType);
        if (allowRetake == null) {
            parcel.writeByte((byte) (0));
        } else {
            parcel.writeByte((byte) (allowRetake ? 1 : 0)); //if review == true, byte == 1
        }
        parcel.writeString(attemptsUrl);
        parcel.writeTypedList(attempts);
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
     * The course
     */
    public String getCourse() {
        return course;
    }

    /**
     *
     * @param course
     * The course
     */
    public void setCourse(String course) {
        this.course = course;
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

    public String formatDate(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(inputString != null && !inputString.isEmpty()) {
                date = simpleDateFormat.parse(inputString);
                DateFormat dateformat = DateFormat.getDateInstance();
                return dateformat.format(date);
            }
        } catch (ParseException e) {
        }
        return null;
    }

    /**
     *
     * @return
     * The endDate
     */
    public String getEndDate() {
        return formatDate(endDate);
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
     * Allow retake
     */
    public Boolean getAllowRetake() {
        return allowRetake;
    }

    /**
     *
     * @param allowRetake
     * Allow retake
     */
    public void setAllowRetake(Boolean allowRetake) {
        this.allowRetake = allowRetake;
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
            return url.getFile().substring(1);
        }
        catch (Exception e) {
            return null;
        }
    }


    public List<Attempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<Attempt> attempts) {
        this.attempts = attempts;
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
