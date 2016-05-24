package in.testpress.testpress.core;

import java.util.Map;

import in.testpress.testpress.models.Notes;
import in.testpress.testpress.models.TestpressApiResponse;

import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface DocumentsService {

    @GET("/{documents_url}")
    TestpressApiResponse<Notes> getDocumentsList(@Path("documents_url") String urlFrag, @QueryMap Map<String, String> options, @Header("Authorization") String authorization);

    @GET("/" + Constants.Http.URL_DOCUMENTS_FRAG + "{slug}/download" )
    Notes getDownloadUrl(@Path("slug") String urlFrag, @Header("Authorization") String authorization);

}


