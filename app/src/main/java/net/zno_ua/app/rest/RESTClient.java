package net.zno_ua.app.rest;

/**
 * @author Vojko Vladimir
 */
public abstract class RESTClient {
    private static final int API_VERSION = 2;

    private static final String SERVER_URL = "http://zno-ua.net";
    private static final String SERVER_API_URL = SERVER_URL + "/api/v" + API_VERSION + "/";

    private static final String FORMAT_JSON = "format=json";
    private static final String GET_TEST = SERVER_API_URL + "question/?format=json&test=";
    private static final String GET_TEST_POINTS = SERVER_API_URL + "result/?format=json&limit=0&test_id=";
    private static final String RESULT = "result";
    private static final String TEST_ID = "test_id";
    private static final String LIMIT = "limit";

    public interface ResourceType {
        int TEST = 0x1;
    }

    private static final String TEST = "test";
    private static final String QUESTION = "question";

    static String getTestInfoUrl(long testId) {
        return SERVER_API_URL + TEST + "/" + testId + "/?" + FORMAT_JSON;
    }

    static String getTestQuestionsUrl(long testId) {
        return SERVER_API_URL + QUESTION + "/?" + FORMAT_JSON + "&" + TEST + "=" + testId;
    }

    static String getImageUrl(String relativePath, String name) {
        return SERVER_URL + relativePath + "/" + name;
    }

    static String getTestPointsUrl(long testId) {
        return SERVER_API_URL + RESULT + "/?" + FORMAT_JSON + "&" + TEST_ID + "=" + testId + "&"
                + LIMIT + "=0";
    }
}
