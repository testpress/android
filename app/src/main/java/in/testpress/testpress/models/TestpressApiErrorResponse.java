package in.testpress.testpress.models;

public class TestpressApiErrorResponse {

    private String detail;
    private String error_code;

    /**
     * @return The detail
     */
    public String getDetail() {
        return detail;
    }

    /**
     * @param detail The detail
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getErrorCode() {
        return error_code;
    }

    public void setErrorCode(String error_code) {
        this.error_code = error_code;
    }
}
