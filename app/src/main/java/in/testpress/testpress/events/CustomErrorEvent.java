package in.testpress.testpress.events;


public class CustomErrorEvent {
    private String error_code;
    private String detail;

    public CustomErrorEvent(String error_code, String detail) {
        this.error_code = error_code;
        this.detail = detail;
    }


    public String getErrorCode() {
        return error_code;
    }

    public String getDetail() {
        return detail;
    }
}
