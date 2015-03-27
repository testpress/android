package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.Map;

public class Exam {
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
    private Integer templateType;
    private String startUrl;
    private Map<String, String> additionalProperties = new HashMap<String, String>();

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
    public String getStartUrl() {
        return startUrl;
    }

    /**
     *
     * @param startUrl
     * The start_url
     */
    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, String value) {
        this.additionalProperties.put(name, value);
    }

}
