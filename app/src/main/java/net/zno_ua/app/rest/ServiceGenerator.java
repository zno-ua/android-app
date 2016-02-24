package net.zno_ua.app.rest;

import net.zno_ua.app.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author vojkovladimir.
 */
public class ServiceGenerator {

    private static final OkHttpClient.Builder sHttpClient;
    private static final Retrofit.Builder sBuilder;

    static {
        sHttpClient = new OkHttpClient.Builder();
        sBuilder = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL + "api/v" + BuildConfig.API_VERSION + "/")
                .addConverterFactory(JacksonConverterFactory.create());
    }

    public static <S> S createService(Class<S> serviceClass, final String authToken) {
        if (authToken != null) {
            sHttpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    final Request original = chain.request();
                    final Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", authToken)
                            .method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                }
            });
        }
        final OkHttpClient client = sHttpClient.build();
        final Retrofit retrofit = sBuilder.client(client).build();
        return retrofit.create(serviceClass);
    }
}