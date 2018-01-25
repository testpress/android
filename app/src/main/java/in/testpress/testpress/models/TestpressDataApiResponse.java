package in.testpress.testpress.models;

import java.util.HashMap;
import java.util.Map;

public class TestpressDataApiResponse<T> {

    private Integer count;
    private String next;
    private String previous;
    private Integer perPage;
    private T results;
    private Map<String, String> additionalProperties = new HashMap<String, String>();

    public Integer getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }

    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, String value) {
        this.additionalProperties.put(name, value);
    }

    public void setCount(Integer count) {
        this.count = count;

    }

    public boolean hasMore() {
        return !next.equals("null");
    }
}
