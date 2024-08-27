package org.chainoptim.desktop.core.organization.model;

import lombok.Data;
import org.chainoptim.desktop.core.user.model.User;

import java.util.Set;

@Data
public class Organization {

    private Integer id;
    private String name;
    private String address;
    private String contactInfo;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private SubscriptionPlanTier subscriptionPlanTier;

    private Set<User> users;

    public PlanDetails getSubscriptionPlan() {
        return BaseSubscriptionPlans.getPlans().get(subscriptionPlanTier);
    }
}
