package pl.jakubchmura.suchary.android.joke.api.network;

public class APIToken {

    public static final String TOKEN_PREF = "token_pref";

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
