package in.testpress.testpress.network;

import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.UnreadMessagesCount;
import retrofit.http.GET;

public interface UnreadMessagesCountService {
    @GET(Constants.Http.URL_UNREAD_MESSAGES_COUNT)
    UnreadMessagesCount getUnreadMessagesCount();
}
