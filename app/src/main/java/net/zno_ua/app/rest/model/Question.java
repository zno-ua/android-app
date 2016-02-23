package net.zno_ua.app.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author vojkovladimir.
 */
@JsonIgnoreProperties("test")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Question {
    public String answers;
    @JsonProperty("balls")
    public int point;
    @JsonProperty("correct_answer")
    public String correctAnswer;
    public long id;
    @JsonProperty("id_on_test")
    public int positionOnTest;
    @JsonProperty("question")
    public String text;
    @JsonProperty("parent_question")
    public String additionalText;
    @JsonProperty("type_question")
    public int type;
    @JsonProperty("images_formulas_url")
    public String imagesFormulasUrl;
    @JsonProperty("images_formulas")
    public String[] imagesFormulas;
    @JsonProperty("images_relative_url")
    public String imagesRelativeUrl;
    @JsonProperty("images")
    public String[] images;
}
