package com.smhrd.model;

import lombok.Data;

@Data // getter, setter, toString
public class UserInfo {
    // Fields
    private String email;
    private String pw;
    private String nickname;
    private String joinDate;

    // Signup constructor (joinDate handled by DB)
    public UserInfo(String email, String pw, String nickname) {
        this.email = email;
        this.pw = pw;
        this.nickname = nickname;
    }
    

    public UserInfo(String email, String pw) {
        this.email = email;
        this.pw = pw;
    }
    
    public UserInfo(String email, String pw, String nickname, String joinDate) {
        this.email = email;
        this.pw = pw;
        this.nickname = nickname;
        this.joinDate = joinDate;
    }
    
    // Default constructor
    public UserInfo() {}
    
    // Getter methods
    public String getEmail() {
        return email;
    }
    
    public String getPw() {
        return pw;
    }
    
    
    public String getNickname() {
        return nickname;
    }
    
    public String getJoinDate() {
        return joinDate;
    }
    
    // Setter methods
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPw(String pw) {
        this.pw = pw;
    }
    
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }
    
    // toString method
    @Override
    public String toString() {
        return "UserInfo [email=" + email + ", nickname=" + nickname + ", joinDate=" + joinDate + "]";
    }
}