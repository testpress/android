package in.testpress.testpress.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class Assessment {

    @Id
    private Long id;
    private String score;
    private String reviewPdfUrl;
    private int correctCount;
    private int incorrectCount;
    private String timeTaken;
    private String percentage;
    private int unansweredCount;
    private int speed;
    private int accuracy;
    private int exam;
    @Generated(hash = 186676685)
    public Assessment(Long id, String score, String reviewPdfUrl, int correctCount,
            int incorrectCount, String timeTaken, String percentage,
            int unansweredCount, int speed, int accuracy, int exam) {
        this.id = id;
        this.score = score;
        this.reviewPdfUrl = reviewPdfUrl;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.timeTaken = timeTaken;
        this.percentage = percentage;
        this.unansweredCount = unansweredCount;
        this.speed = speed;
        this.accuracy = accuracy;
        this.exam = exam;
    }
    @Generated(hash = 2086173289)
    public Assessment() {
    }
    public String getScore() {
        return this.score;
    }
    public void setScore(String score) {
        this.score = score;
    }
    public String getReviewPdfUrl() {
        return this.reviewPdfUrl;
    }
    public void setReviewPdfUrl(String reviewPdfUrl) {
        this.reviewPdfUrl = reviewPdfUrl;
    }
    public int getCorrectCount() {
        return this.correctCount;
    }
    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }
    public int getIncorrectCount() {
        return this.incorrectCount;
    }
    public void setIncorrectCount(int incorrectCount) {
        this.incorrectCount = incorrectCount;
    }
    public String getTimeTaken() {
        return this.timeTaken;
    }
    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }
    public String getPercentage() {
        return this.percentage;
    }
    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
    public int getUnansweredCount() {
        return this.unansweredCount;
    }
    public void setUnansweredCount(int unansweredCount) {
        this.unansweredCount = unansweredCount;
    }
    public int getSpeed() {
        return this.speed;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    public int getAccuracy() {
        return this.accuracy;
    }
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
    public int getExam() {
        return this.exam;
    }
    public void setExam(int exam) {
        this.exam = exam;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
}
