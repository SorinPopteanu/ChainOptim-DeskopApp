package org.chainoptim.desktop.core.tenant.user.model;
import lombok.*;
import org.chainoptim.desktop.core.tenant.customrole.model.CustomRole;
import org.chainoptim.desktop.core.tenant.organization.model.Organization;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String id;
    private String username;
    private String passwordHash;
    private String email;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private Organization organization;
    private Role role;
    private CustomRole customRole;

    public enum Role {
        ADMIN,
        MEMBER,
        NONE
    }
}
