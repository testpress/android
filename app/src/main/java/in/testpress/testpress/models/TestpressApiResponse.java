package in.testpress.testpress.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestpressApiResponse<T> {
    private Integer count;
    private String next;
    private String previous;
    private Integer perPage;
    private List<T> results = new ArrayList<T>();
    private Map<String, String> additionalProperties = new HashMap<String, String>();

    /**
     *
     * @return
     * The count
     */
    public Integer getCount() {
        return count;
    }

    /**
     *
     * @param count
     * The count
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     *
     * @return
     * The next
     */
    public String getNext() {
        return next;
    }

    /**
     *
     * @param next
     * The next
     */
    public void setNext(String next) {
        this.next = next;
    }

    /**
     *
     * @return
     * The previous
     */
    public Object getPrevious() {
        return previous;
    }

    /**
     *
     * @param previous
     * The previous
     */
    public void setPrevious(String previous) {
        this.previous = previous;
    }

    /**
     *
     * @return
     * The perPage
     */
    public Integer getPerPage() {
        return perPage;
    }

    /**
     *
     * @param perPage
     * The per_page
     */
    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    /**
     *
     * @return
     * The results
     */
    public List<T> getResults() {
        return results;
    }

    /**
     *
     * @param results
     * The results
     */
    public void setResults(List<T> results) {
        this.results = results;
    }

    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, String value) {
        this.additionalProperties.put(name, value);
    }

    public boolean hasMore() {
        return !next.equals("null");
    }
}