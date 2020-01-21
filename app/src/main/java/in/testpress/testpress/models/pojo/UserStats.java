package in.testpress.testpress.models.pojo;

public class UserStats {
    private Long id;
    private String dateFrom;
    private Integer attemptsCount;
    private Integer attemptsCountDifference;
    private String videoWatchedDuration;
    private String videoWatchedDurationDifference;
    private String category;

    public Long getId() {
        return id;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public Integer getAttemptsCountDifference() {
        return attemptsCountDifference;
    }

    public String getVideoWatchedDuration() {
        return videoWatchedDuration;
    }

    public String getVideoWatchedDurationDifference() {
        return videoWatchedDurationDifference;
    }

    public String getCategory() {
        return category;
    }
}