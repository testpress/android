package in.testpress.testpress.events;


public class CustomErrorEvent {
    private String error_code;
    private String detail;
    private String title;

    public CustomErrorEvent(String error_code, String detail, String title) {
        this.error_code = error_code;
        this.detail = detail;
        this.title = title;
    }


    public String getErrorCode() {
        return error_code;
    }

    public String getDetail() {
        return detail;
    }

    public String getTitle() {
        return title;
    }
}
