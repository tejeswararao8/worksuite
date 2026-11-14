package com.platform.hr.platform.subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for subscription status-change actions (cancel, suspend, reactivate, etc.). */
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionActionRequest {

    private String reason;
}
