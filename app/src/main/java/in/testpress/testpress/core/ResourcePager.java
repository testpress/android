package in.testpress.testpress.core;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.TestpressServiceProvider;


/**
 * Generic resource pager for elements with an id that can be paged
 *
 * @param <E>
 */
public abstract class ResourcePager<E> {

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
    protected final Map<Object, E> resources = new LinkedHashMap<Object, E>();

    /**
     * Query Params to be passed
     */
    public Map<String, String> queryParams = new LinkedHashMap<String, String>();

    /**
     * Are more pages available?
     */
    protected boolean hasMore;

    protected boolean networkFail = false;

    public ResourcePager(final TestpressService service) {
        this.service = service;
    }
    /**
     * Reset the number of the next page to be requested from {@link #next()}
     * and clear all stored state
     *
     * @return this pager
     */
    public ResourcePager<E> reset() {
        page = 1;
        return clear();
    }

    /**
     * Clear all stored resources and have the next call to {@link #next()} load
     * all previously loaded pages
     *
     * @return this pager
     */
    public ResourcePager<E> clear() {
        count = Math.max(1, page - 1);
        page = 1;
        resources.clear();
        hasMore = true;
        return this;
    }

    /**
     * Get number of resources loaded into this pager
     *
     * @return number of resources
     */
    public int size() {
        return resources.size();
    }

    /**
     * Get resources
     *
     * @return resources
     */
    public List<E> getResources() {
        if(networkFail) {
            return null;
        }
        return new ArrayList<E>(resources.values());
    }

    /**
     * Get the next page of issues
     *
     * @return true if more pages
     * @throws java.io.IOException
     */
    public boolean next() throws IOException {
        boolean emptyPage = false;
        networkFail = false;
        try {
            for (int i = 0; i < count && hasNext(); i++) {
                List<E> resourcePage = getItems(page, -1);
                emptyPage = resourcePage.isEmpty();
                if (emptyPage)
                    break;
                for (E resource : resourcePage) {
                    resource = register(resource);
                    if (resource == null)
                        continue;
                    resources.put(getId(resource), resource);
                }
            }
            // Set page to count value if first call after call to reset()
            if (count > 1) {
                page = count;
                count = 1;
            }

            page++;
        } catch (Exception e) {
            hasMore = false;
            networkFail = true;
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
    public abstract List<E> getItems(final int page, final int size);

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

