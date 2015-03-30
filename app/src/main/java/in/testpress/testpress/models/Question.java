package in.testpress.testpress.models;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Question {

    private String questionHtml;
    private List<Answer> answers = new ArrayList<Answer>();
    private Object subject;
    private String type;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
    public void setSubject(Object subject) {
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