package com.app.rbac.integration;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ADMIN = "admin1";
    private static final String ADMIN_PASSWORD = "admin123";

    private static final String SEEDED_USER = "shubham";
    private static final String SEEDED_USER_PASSWORD = "shubham123"; // has USER role -> SECURE_DATA_READ out of the box

    @Test
    void unauthenticatedRequest_toSecureData_isRejected() throws Exception {
        mockMvc.perform(get("/secure-data"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void seededUser_withSecureDataReadPermission_canAccessSecureData() throws Exception {
        mockMvc.perform(get("/secure-data").with(httpBasic(SEEDED_USER, SEEDED_USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedBy").value(SEEDED_USER))
                .andExpect(jsonPath("$.message").value(containsString("Access granted")));
    }

    @Test
    void newlyRegisteredUser_withNoRoles_isForbiddenFromSecureData() throws Exception {
        registerUser("noPermsUser", "password1");

        // Correct credentials, but zero roles assigned -> zero permissions -> 403, not 401.
        mockMvc.perform(get("/secure-data").with(httpBasic("noPermsUser", "password1")))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullDynamicAuthorizationFlow_grantingAndCheckingANewPermission() throws Exception {
        // 1. Admin creates a brand-new permission that did not exist before.
        Long permissionId = createPermission("REPORT_VIEW");

        // 2. Admin creates a brand-new role.
        Long roleId = createRole("AUDITOR");

        // 3. Admin wires the new permission to the new role - pure DB configuration, no code change.
        mockMvc.perform(post("/roles/" + roleId + "/permissions/" + permissionId)
                        .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("REPORT_VIEW")));

        // 4. Doing it again must be rejected as a duplicate mapping.
        mockMvc.perform(post("/roles/" + roleId + "/permissions/" + permissionId)
                        .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
                .andExpect(status().isConflict());

        // 5. A new user registers and is assigned the AUDITOR role by the admin.
        Long userId = registerUser("auditorAlice", "password1");

        mockMvc.perform(post("/users/" + userId + "/roles/" + roleId)
                        .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
                .andExpect(status().isOk());

        // Non-admin cannot assign roles - USER_ROLE_ASSIGN is not in john's permission set.
        Long anotherUserId = registerUser("bystander", "password1");
        mockMvc.perform(post("/users/" + anotherUserId + "/roles/" + roleId)
                        .with(httpBasic(SEEDED_USER, SEEDED_USER_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationErrors_returnBadRequest_withFieldDetails() throws Exception {
        mockMvc.perform(post("/roles")
                        .with(httpBasic(ADMIN, ADMIN_PASSWORD))
                        .contentType("application/json")
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void duplicateRoleCreation_returnsConflict() throws Exception {
        mockMvc.perform(post("/roles")
                        .with(httpBasic(ADMIN, ADMIN_PASSWORD))
                        .contentType("application/json")
                        .content("""
                                {"name":"ADMIN"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void assigningPermissionToUnknownRole_returnsNotFound() throws Exception {
        Long permissionId = createPermission("SOME_PERMISSION_" + System.nanoTime());

        mockMvc.perform(post("/roles/999999/permissions/" + permissionId)
                        .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    // --- test helpers -------------------------------------------------------------------

    private Long createRole(String name) throws Exception {
        String json = mockMvc.perform(post("/roles")
                        .with(httpBasic(ADMIN, ADMIN_PASSWORD))
                        .contentType("application/json")
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    private Long createPermission(String name) throws Exception {
        String json = mockMvc.perform(post("/permissions")
                        .with(httpBasic(ADMIN, ADMIN_PASSWORD))
                        .contentType("application/json")
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    private Long registerUser(String username, String password) throws Exception {
        String json = mockMvc.perform(post("/users/register")
                        .contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }
}
