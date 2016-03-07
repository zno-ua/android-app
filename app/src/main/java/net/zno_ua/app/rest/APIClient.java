package net.zno_ua.app.rest;

import net.zno_ua.app.rest.model.Objects;
import net.zno_ua.app.rest.model.Point;
import net.zno_ua.app.rest.model.Question;
import net.zno_ua.app.rest.model.Review;
import net.zno_ua.app.rest.model.TestInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * @author vojkovladimir.
 */
public interface APIClient {
    @GET("test/{test_id}/")
    Call<TestInfo> getTestInfo(@Path("test_id") long testId);

    @GET("question/")
    Call<Objects<Question>> getTestQuestions(@Query("test") long testId);

    @GET("result/?limit=0")
    Call<Objects<Point>> getTestPoints(@Query("test_id") long testId);

    @GET("/{path}/{name}")
    @Streaming
    Call<ResponseBody> getImage(@Path("path") String path, @Path("name") String name);

    @POST("review/")
    Call<ResponseBody> sendReview(@Body Review review);
}
