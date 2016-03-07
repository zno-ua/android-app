package net.zno_ua.app.rest.model;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * @author Vladimir Vojko  v.vojko@rollncode.com
 * @since 3/7/16
 */
public class Review {
    private final String name;
    private final String mail;
    private final String message;

    public Review(String name, String mail, String message) {
        this.name = name;
        this.mail = mail;
        this.message = message;
    }

    @JsonGetter
    public String getName() {
        return name;
    }

    @JsonGetter
    public String getMail() {
        return mail;
    }

    @JsonGetter
    public String getMessage() {
        return message;
    }
}
