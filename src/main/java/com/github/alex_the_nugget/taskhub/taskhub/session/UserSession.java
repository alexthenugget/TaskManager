package com.github.alex_the_nugget.taskhub.taskhub.session;

public class UserSession {
    private static UserSession instance;
    private String login;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}