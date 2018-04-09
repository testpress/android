package in.testpress.testpress.core;

import java.util.Map;

import in.testpress.testpress.models.Notes;
import in.testpress.testpress.models.TestpressApiResponse;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;

interface DocumentsService {

    @GET("/{documents_url}")
    TestpressApiResponse<Notes> getDocumentsList(
            @Path(value = "documents_url", encode = false) String urlFrag,
            @QueryMap Map<String, String> options);

    @GET("/" + Constants.Http.URL_DOCUMENTS_FRAG + "{slug}/download" )
    Notes getDownloadUrl(@Path("slug") String urlFrag);

}


