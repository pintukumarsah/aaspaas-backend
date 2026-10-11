package com.aaspaas.aaspaas_backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Protected orders API rejects request without authentication")
    void protectedOrdersApiRejectsAnonymousRequest() throws Exception {

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected orders API rejects an invalid Bearer token")
    void protectedOrdersApiRejectsInvalidToken() throws Exception {

        mockMvc.perform(
                get("/api/orders")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer invalid-token"
                        )
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(
            username = "customer-test",
            roles = {"CUSTOMER"}
    )
    @DisplayName("Customer cannot access an admin API")
    void customerCannotAccessAdminApi() throws Exception {

        mockMvc.perform(get("/api/admin/security-test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(
            username = "admin-test",
            roles = {"ADMIN"}
    )
    @DisplayName("Admin is not rejected by the role check on an admin API")
    void adminPassesAdminRoleCheck() throws Exception {

        int statusCode = mockMvc.perform(
                get("/api/admin/security-test")
        ).andReturn().getResponse().getStatus();

        // The test path may not have a controller mapping, so 404 is
        // acceptable here. A 401/403 means authentication/authorization
        // did not behave as expected.
        org.junit.jupiter.api.Assertions.assertFalse(
                statusCode == 401 || statusCode == 403,
                "An authenticated ADMIN should pass the admin role check"
        );
    }
}