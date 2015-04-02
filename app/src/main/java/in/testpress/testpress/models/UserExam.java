package in.testpress.testpress.models;

import java.util.HashMap;
import java.util.Map;
public class UserExam {

    private String questionsUrl;
    private Integer uExamId;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * The uExamId
     */
    public Integer getUExamId() {
        return uExamId;
    }

    /**
     *
     * @param uExamId
     * The u_exam_id
     */
    public void setUExamId(Integer uExamId) {
        this.uExamId = uExamId;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
