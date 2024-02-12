package org.chainoptim.desktop.core.user.model;
import lombok.*;

@Data
public class User {

    private String id;
    private String username;
    private String passwordHash;
    private String email;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private Integer organizationId;
    private Role role;


    public enum Role {
        ADMIN,
        MEMBER,
        NONE
    }
}
