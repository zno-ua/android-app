package net.zno_ua.app.rest;

import net.zno_ua.app.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static okhttp3.logging.HttpLoggingInterceptor.Level;

/**
 * @author vojkovladimir.
 */
public class APIServiceGenerator {

    private static final OkHttpClient.Builder sHttpClient;
    private static final Retrofit.Builder sBuilder;
    private static APIClient sApiInstance = null;

    static {
        sHttpClient = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(Level.BASIC);
            sHttpClient.addInterceptor(logging);
        }
        sBuilder = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL + "api/v" + BuildConfig.API_VERSION + "/")
                .addConverterFactory(JacksonConverterFactory.create());
    }

    private static APIClient create() {
        sHttpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                final Request original = chain.request();
                final Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", BuildConfig.API_KEY)
                        .method(original.method(), original.body());
                return chain.proceed(requestBuilder.build());
            }
        });
        final OkHttpClient client = sHttpClient.build();
        final Retrofit retrofit = sBuilder.client(client).build();
        return retrofit.create(APIClient.class);
    }

    public static synchronized APIClient getAPIClient() {
        synchronized (APIServiceGenerator.class) {
            if (sApiInstance == null) {
                sApiInstance = create();
            }

            return sApiInstance;
        }
    }
}
