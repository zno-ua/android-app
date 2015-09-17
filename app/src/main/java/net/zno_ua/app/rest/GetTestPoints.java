package net.zno_ua.app.rest;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONObject;

/**
 * @author Vojko Vladimir
 */
public class GetTestPoints extends RESTMethod<JSONObject> {

    private long testId;

    public GetTestPoints(long testId) {
        this.testId = testId;
    }

    @Override
    protected Request<JSONObject> getRequest(RequestFuture<JSONObject> requestFuture) {
        return new JsonObjectRequest(Request.Method.GET,
                RESTClient.getTestPointsUrl(testId),
                requestFuture,
                requestFuture);
    }
}
