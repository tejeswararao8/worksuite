package com.platform.hr.platform.employees;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for accepting a platform invitation.
 *
 * <p>The token is transmitted in the POST body — never in a query string —
 * so it does not appear in server access logs, browser history, or HTTP
 * Referer headers.
 */
@Getter
@Setter
@NoArgsConstructor
public class AcceptInvitationRequest {

    /**
     * The raw invitation token from the invitation email.
     * Must be non-blank. Max length matches the Base64URL-encoded 48-byte token (64 chars).
     * Never logged or included in error messages.
     */
    @NotBlank
    @Size(max = 128)
    private String token;
}
