package uz.devops.bm.web.rest;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.devops.bm.web.rest.AccountResourceIT.TEST_USER_LOGIN;
import static uz.devops.bm.web.rest.TestUtil.ID_TOKEN;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import uz.devops.bm.IntegrationTest;
import uz.devops.bm.config.TestSecurityConfiguration;
import uz.devops.bm.security.AuthoritiesConstants;
import uz.devops.bm.service.UserService;

/**
 * Integration tests for the {@link AccountResource} REST controller.
 */
@AutoConfigureWebTestClient
@WithMockUser(value = TEST_USER_LOGIN)
@IntegrationTest
class AccountResourceIT {

    static final String TEST_USER_LOGIN = "test";

    private OidcIdToken idToken;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setup() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("groups", Collections.singletonList(AuthoritiesConstants.ADMIN));
        claims.put("sub", "jane");
        claims.put("email", "jane.doe@jhipster.com");
        this.idToken = new OidcIdToken(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), claims);
    }

    @Test
    void testGetExistingAccount() {
        webTestClient
            .mutateWith(mockAuthentication(TestUtil.authenticationToken(idToken)))
            .mutateWith(csrf())
            .get()
            .uri("/api/account")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.login")
            .isEqualTo("jane")
            .jsonPath("$.email")
            .isEqualTo("jane.doe@jhipster.com")
            .jsonPath("$.authorities")
            .isEqualTo(AuthoritiesConstants.ADMIN);
    }

    @Test
    void testGetUnknownAccount() {
        webTestClient.get().uri("/api/account").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().is5xxServerError();
    }

    @Test
    @WithUnauthenticatedMockUser
    void testNonAuthenticatedUser() {
        webTestClient
            .get()
            .uri("/api/authenticate")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .isEmpty();
    }

    @Test
    void testAuthenticatedUser() {
        webTestClient
            .get()
            .uri("/api/authenticate")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .isEqualTo(TEST_USER_LOGIN);
    }
}
