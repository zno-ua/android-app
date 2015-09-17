package net.zno_ua.app.service;

/**
 * @author Vojko Vladimir
 */
public class Command {

    private int method;
    private int resource;
    private long id;

    public Command(int method, int resource, long id) {
        this.method = method;
        this.resource = resource;
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            Command cmdObj = (Command) obj;
            return method == cmdObj.method && resource == cmdObj.resource && id == cmdObj.id;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int getMethod() {
        return method;
    }

    public int getResource() {
        return resource;
    }

    public long getId() {
        return id;
    }
}
