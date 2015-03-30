package in.testpress.testpress.models;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Questions {

    private String url;
    private Question question;
    private List<Integer> selectedAnswers = new ArrayList<Integer>();
    private Boolean review;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * The question
     */
    public Question getQuestion() {
        return question;
    }

    /**
     *
     * @param question
     * The question
     */
    public void setQuestion(Question question) {
        this.question = question;
    }

    /**
     *
     * @return
     * The selectedAnswers
     */
    public List<Integer> getSelectedAnswers() {
        return selectedAnswers;
    }

    /**
     *
     * @param selectedAnswers
     * The selected_answers
     */
    public void setSelectedAnswers(List<Integer> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    /**
     *
     * @return
     * The review
     */
    public Boolean getReview() {
        return review;
    }

    /**
     *
     * @param review
     * The review
     */
    public void setReview(Boolean review) {
        this.review = review;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}