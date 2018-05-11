package in.testpress.testpress.core;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class DataResourcePager<E> {
    TestpressService service;

    /**
     * Next page to request
     */
    protected int page = 1;

    /**
     * Number of pages to request
     */
    protected int count = 1;

    /**
     * All resources retrieved
     */
    protected E resources;

    /**
     * Query Params to be passed
     */
    public Map<String, String> queryParams = new LinkedHashMap<String, String>();

    /**
     * Are more pages available?
     */
    protected boolean hasMore;

    public DataResourcePager(final TestpressService service) {
        this.service = service;
    }
    /**
     * Reset the number of the next page to be requested from {@link #next()}
     * and clear all stored state
     *
     * @return this pager
     */
    public DataResourcePager<E> reset() {
        page = 1;
        return clear();
    }

    /**
     * Clear all stored resources and have the next call to {@link #next()} load
     * all previously loaded pages
     *
     * @return this pager
     */
    public DataResourcePager<E> clear() {
        count = Math.max(1, page - 1);
        page = 1;
        resources = null;
        hasMore = true;
        clearQueryParams();
        return this;
    }

    public DataResourcePager<E> clearResources() {
        resources = null;
        return this;
    }

    /**
     * Get number of resources loaded into this pager
     *
     * @return number of resources
     */
    public int size() {
        return 1;
    }

    /**
     * Get resources
     *
     * @return resources
     */
    public E getResources() {
        return resources;
    }

    /**
     * Get the next page of issues
     *
     * @return true if more pages
     * @throws java.io.IOException
     */
    public boolean next() throws IOException {
        boolean emptyPage = false;
        try {
            for (int i = 0; i < count && hasNext(); i++) {
                E resourcePage = getItems(page, -1);
                emptyPage = resourcePage == null;
                if (emptyPage)
                    break;
                E resource = resourcePage;
                resource = register(resource);
                if (resource == null)
                    continue;
                resources = resource;
            }
            // Set page to count value if first call after call to reset()
            if (count > 1) {
                page = count;
                count = 1;
            }

            page++;
        } catch (Exception e) {
            hasMore = false;
            throw e;
        }
        hasMore = hasNext() && !emptyPage;
        return hasMore;
    }

    /**
     * Are more pages available to request?
     *
     * @return true if the last call to {@link #next()} returned true, false
     *         otherwise
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * Callback to register a fetched resource before it is stored in this pager
     * <p>
     * Sub-classes may override
     *
     * @param resource
     * @return resource
     */
    protected E register(final E resource) {
        Log.e("Inside", "register");
        return resource;
    }

    /**
     * Get id for resource
     *
     * @param resource
     * @return id
     */
    protected abstract Object getId(E resource);


    /**
     * Create iterator to return given page and size
     *
     * @param page
     * @param size
     * @return iterator
     */
    public abstract E getItems(final int page, final int size);

    public abstract  boolean hasNext();

    public String getQueryParams(String key) {
        return queryParams.get(key);
    }

    public void setQueryParams(String key, String value) {
        queryParams.put(key, value);
    }

    public void removeQueryParams(String key) {
        queryParams.remove(key);
    }

    public void clearQueryParams() {
        queryParams.clear();
    }
}
