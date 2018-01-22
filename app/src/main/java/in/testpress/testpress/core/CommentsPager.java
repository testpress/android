package in.testpress.testpress.core;

import java.util.List;

import in.testpress.testpress.models.Comment;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.RetrofitError;

public class CommentsPager extends ResourcePager<Comment> {

    private TestpressApiResponse<Comment> response;
    private long postId;

    public CommentsPager(TestpressService service, long postId) {
        super(service);
        this.postId = postId;
    }

    @Override
    public ResourcePager<Comment> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Comment resource) {
        return resource.getId();
    }

    @Override
    public List<Comment> getItems(int page, int size) throws RetrofitError {
        queryParams.put(Constants.Http.PAGE, String.valueOf(page));
        response = service.getComments(postId, queryParams);
        return response.getResults();
    }

    @Override
    public boolean hasNext() {
        return response == null || response.getNext() != null;
    }

    public Integer getCommentsCount() {
        return response != null ? response.getCount() : 0;
    }
}
