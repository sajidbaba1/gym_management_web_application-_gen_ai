package com.example.gym_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    // Manual getters and builder for Lombok fallback
    public String getAccessToken() { return accessToken; }
    public UserInfo getUser() { return user; }
    
    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }
    
    public static class AuthResponseBuilder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;
        
        public AuthResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public AuthResponseBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }
        
        public AuthResponseBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }
        
        public AuthResponseBuilder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }
        
        public AuthResponseBuilder user(UserInfo user) {
            this.user = user;
            return this;
        }
        
        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.accessToken = this.accessToken;
            response.refreshToken = this.refreshToken;
            response.tokenType = this.tokenType;
            response.expiresIn = this.expiresIn;
            response.user = this.user;
            return response;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String role;
        private String status;
        private String profileImageUrl;
        private Boolean emailVerified;
        
        // Manual getters and builder for Lombok fallback
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
        
        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }
        
        public static class UserInfoBuilder {
            private Long id;
            private String username;
            private String email;
            private String firstName;
            private String lastName;
            private String fullName;
            private String role;
            private String status;
            private String profileImageUrl;
            private Boolean emailVerified;
            
            public UserInfoBuilder id(Long id) { this.id = id; return this; }
            public UserInfoBuilder username(String username) { this.username = username; return this; }
            public UserInfoBuilder email(String email) { this.email = email; return this; }
            public UserInfoBuilder firstName(String firstName) { this.firstName = firstName; return this; }
            public UserInfoBuilder lastName(String lastName) { this.lastName = lastName; return this; }
            public UserInfoBuilder fullName(String fullName) { this.fullName = fullName; return this; }
            public UserInfoBuilder role(String role) { this.role = role; return this; }
            public UserInfoBuilder status(String status) { this.status = status; return this; }
            public UserInfoBuilder profileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; return this; }
            public UserInfoBuilder emailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; return this; }
            
            public UserInfo build() {
                UserInfo userInfo = new UserInfo();
                userInfo.id = this.id;
                userInfo.username = this.username;
                userInfo.email = this.email;
                userInfo.firstName = this.firstName;
                userInfo.lastName = this.lastName;
                userInfo.fullName = this.fullName;
                userInfo.role = this.role;
                userInfo.status = this.status;
                userInfo.profileImageUrl = this.profileImageUrl;
                userInfo.emailVerified = this.emailVerified;
                return userInfo;
            }
        }
    }
}
