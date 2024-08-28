package org.chainoptim.desktop.core.tenant.organization.model;

import lombok.Data;
import org.chainoptim.desktop.core.tenant.organization.subscription.model.BaseSubscriptionPlans;
import org.chainoptim.desktop.core.tenant.organization.subscription.model.PlanDetails;
import org.chainoptim.desktop.core.tenant.organization.subscription.model.SubscriptionPlanTier;
import org.chainoptim.desktop.core.tenant.user.model.User;

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
