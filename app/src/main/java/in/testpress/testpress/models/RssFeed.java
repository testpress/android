package in.testpress.testpress.models;

import java.util.List;

public class RssFeed {

    /**
     * List of parsed {@link RssItem} objects
     */
    private List<RssItem> mItems;

    public List<RssItem> getItems() {
        return mItems;
    }

    public void setItems(List<RssItem> items) {
        mItems = items;
    }
}
