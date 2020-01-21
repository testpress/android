package in.testpress.testpress.models.pojo;

import in.testpress.util.IntegerList;

public class DashboardSection {
    private String slug;
    private String displayName;
    private String url;
    private String contentType;
    private String order;
    private String displayType;
    private IntegerList items;

    public String getSlug() {
        return slug;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public String getOrder() {
        return order;
    }

    public String getDisplayType() {
        return displayType;
    }

    public IntegerList getItems() {
        return items;
    }
}
