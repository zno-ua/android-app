package net.zno_ua.app.rest.model;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
/**
 * @author vojkovladimir.
 */
@JsonIgnoreProperties("test")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Question {
    private static final String ANSWERS = "answers";
    private static final String POINT = "balls";
    private static final String CORRECT_ANSWER = "correct_answer";
    private static final String ID = "id";
    private static final String POSITION_ON_TEST = "id_on_test";
    private static final String TEXT = "question";
    private static final String ADDITIONAL_TEXT = "parent_question";
    private static final String TYPE = "type_question";
    private static final String IMAGE_FORMULAS_URL = "images_formulas_url";
    private static final String IMAGES_FORMULAS = "images_formulas";
    private static final String IMAGES_RELATIVE_URL = "images_relative_url";
    private static final String IMAGES = "images";

    private String mAnswers;
    private int mPoint;
    private String mCorrectAnswer;
    private long mId;
    private int mPositionOnTest;
    private String mText;
    private String mAdditionalText;
    private int mType;
    private String mImagesFormulasUrl;
    private String[] mImagesFormulas;
    private String mImagesRelativeUrl;
    private String[] mImages;

    public String getAnswers() {
        return mAnswers;
    }

    @JsonSetter(ANSWERS)
    public void setAnswers(String answers) {
        mAnswers = answers;
    }

    public int getPoint() {
        return mPoint;
    }

    @JsonSetter(POINT)
    public void setPoint(int point) {
        mPoint = point;
    }

    public String getCorrectAnswer() {
        return mCorrectAnswer;
    }

    @JsonSetter(CORRECT_ANSWER)
    public void setCorrectAnswer(String correctAnswer) {
        mCorrectAnswer = correctAnswer;
    }

    public long getId() {
        return mId;
    }

    @JsonSetter(ID)
    public void setId(long id) {
        mId = id;
    }

    public int getPositionOnTest() {
        return mPositionOnTest;
    }

    @JsonSetter(POSITION_ON_TEST)
    public void setPositionOnTest(int positionOnTest) {
        mPositionOnTest = positionOnTest;
    }

    public String getText() {
        return mText;
    }

    @JsonSetter(TEXT)
    public void setText(String text) {
        mText = text;
    }

    public String getAdditionalText() {
        return mAdditionalText;
    }

    @JsonSetter(ADDITIONAL_TEXT)
    public void setAdditionalText(String additionalText) {
        mAdditionalText = additionalText;
    }

    public int getType() {
        return mType;
    }

    @JsonSetter(TYPE)
    public void setType(int type) {
        mType = type;
    }

    @Nullable
    public String getImagesFormulasUrl() {
        return mImagesFormulasUrl;
    }

    @JsonSetter(IMAGE_FORMULAS_URL)
    public void setImagesFormulasUrl(String imagesFormulasUrl) {
        mImagesFormulasUrl = imagesFormulasUrl;
    }

    @Nullable
    public String[] getImagesFormulas() {
        return mImagesFormulas;
    }

    @JsonSetter(IMAGES_FORMULAS)
    public void setImagesFormulas(String[] imagesFormulas) {
        mImagesFormulas = imagesFormulas;
    }

    @Nullable
    public String getImagesRelativeUrl() {
        return mImagesRelativeUrl;
    }

    @JsonSetter(IMAGES_RELATIVE_URL)
    public void setImagesRelativeUrl(String imagesRelativeUrl) {
        mImagesRelativeUrl = imagesRelativeUrl;
    }

    @Nullable
    public String[] getImages() {
        return mImages;
    }

    @JsonSetter(IMAGES)
    public void setImages(String[] images) {
        mImages = images;
    }

}
