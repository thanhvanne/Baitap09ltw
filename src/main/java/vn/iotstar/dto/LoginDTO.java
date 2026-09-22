package vn.iotstar.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginDTO {

    @NotBlank
    private String login;

    @NotBlank
    private String password;

    public LoginDTO() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}