package in.testpress.testpress.models.pojo;

import in.testpress.testpress.models.User;

public class LeaderboardItem {
    private Long id;
    private String trophiesCount;
    private Integer difference;
    private Integer category;
    private User user;

    public Long getId() {
        return id;
    }

    public String getTrophiesCount() {
        return trophiesCount;
    }

    public Integer getDifference() {
        return difference;
    }

    public Integer getCategory() {
        return category;
    }
}
