package com.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.domain.enums.Role;
import com.pulsepass.security.SecurityPrincipal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthorizationServiceTest {

    private final AuthorizationService service = new AuthorizationService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String email, Role... roles) {
        SecurityPrincipal principal =
                new SecurityPrincipal(7L, "andrea", email, "x", true, List.of(roles));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    void hasNoPrincipalWhenAnonymous() {
        assertThat(service.currentPrincipal()).isEmpty();
        assertThatThrownBy(service::requirePrincipal).isInstanceOf(AccessDeniedException.class);
        assertThat(service.isAdmin()).isFalse();
        assertThat(service.isStaffOrAdmin()).isFalse();
    }

    @Test
    void detectsAdminAndStaffRoles() {
        authenticate("andrea@email.com", Role.ADMIN);

        assertThat(service.isAdmin()).isTrue();
        assertThat(service.isStaffOrAdmin()).isTrue();
    }

    @Test
    void allowsTheOwnerById() {
        authenticate("andrea@email.com", Role.USER);

        assertThatCode(() -> service.requireSelfOrAdmin(7L)).doesNotThrowAnyException();
    }

    @Test
    void rejectsAnotherUserByid() {
        authenticate("andrea@email.com", Role.USER);

        assertThatThrownBy(() -> service.requireSelfOrAdmin(99L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowsAnAdminForSomeoneElse() {
        authenticate("admin@email.com", Role.ADMIN);

        assertThatCode(() -> service.requireSelfOrAdmin(99L)).doesNotThrowAnyException();
    }

    @Test
    void matchesTheOwnerEmailIgnoringCase() {
        authenticate("andrea@email.com", Role.USER);

        assertThatCode(() -> service.requireSelfByEmailOrAdmin("ANDREA@EMAIL.COM"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsPurchaseOnBehalfOfAnotherIdentityEvenForAdmin() {
        authenticate("admin@email.com", Role.ADMIN);

        assertThatThrownBy(() -> service.requirePurchaseForSelf("andrea@email.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowsTicketAccessForStaff() {
        authenticate("staff@email.com", Role.STAFF);

        assertThatCode(() -> service.requireTicketOwnerOrPrivileged("andrea@email.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void doesNotAllowStaffToCancelSomeoneElsesTicket() {
        authenticate("staff@email.com", Role.STAFF);

        assertThatThrownBy(() -> service.requireTicketOwnerOrAdmin("andrea@email.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowsAdminToCancelSomeoneElsesTicket() {
        authenticate("admin@email.com", Role.ADMIN);

        assertThatCode(() -> service.requireTicketOwnerOrAdmin("andrea@email.com"))
                .doesNotThrowAnyException();
    }
}
