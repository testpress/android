package in.testpress.testpress.models;
import java.util.HashMap;
import java.util.Map;

public class Answer {

    private String textHtml;
    private Integer id;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The textHtml
     */
    public String getTextHtml() {
        return textHtml;
    }

    /**
     *
     * @param textHtml
     * The text_html
     */
    public void setTextHtml(String textHtml) {
        this.textHtml = textHtml;
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

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}