package com.Zorvyn.Finance_Dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceDashboardApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginReturnsJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("admin"));
    }

    @Test
    void loginFailsForInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void inactiveUserCannotLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "inactive",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void openApiDocsArePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("Finance Dashboard Backend API"));
    }

    @Test
    void viewerCanAccessDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", bearerToken("viewer", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").exists())
                .andExpect(jsonPath("$.totalExpenses").exists())
                .andExpect(jsonPath("$.recentActivity").isArray());
    }

    @Test
    void viewerCannotReadRecords() throws Exception {
        mockMvc.perform(get("/api/records")
                        .header("Authorization", bearerToken("viewer", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCanFilterRecords() throws Exception {
        mockMvc.perform(get("/api/records")
                        .header("Authorization", bearerToken("analyst", "password"))
                        .param("type", "EXPENSE")
                        .param("category", "Rent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("Rent"));
    }

    @Test
    void analystCanSearchAndPaginateRecords() throws Exception {
        mockMvc.perform(get("/api/records")
                        .header("Authorization", bearerToken("analyst", "password"))
                        .param("search", "software")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content[0].category").value("Software"));
    }

    @Test
    void analystCannotCreateRecord() throws Exception {
        mockMvc.perform(post("/api/records")
                        .header("Authorization", bearerToken("analyst", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 99.00,
                                  "type": "EXPENSE",
                                  "category": "Travel",
                                  "date": "2026-04-04",
                                  "notes": "Taxi"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateUpdateAndDeleteRecord() throws Exception {
        String createResponse = mockMvc.perform(post("/api/records")
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 199.50,
                                  "type": "EXPENSE",
                                  "category": "Travel",
                                  "date": "2026-04-04",
                                  "notes": "Flight transfer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Travel"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long recordId = extractLongField(createResponse, "id");

        mockMvc.perform(put("/api/records/{id}", recordId)
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 219.50,
                                  "type": "EXPENSE",
                                  "category": "Travel",
                                  "date": "2026-04-05",
                                  "notes": "Updated transfer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(219.50));

        mockMvc.perform(delete("/api/records/{id}", recordId)
                        .header("Authorization", bearerToken("admin", "password")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/records/{id}", recordId)
                        .header("Authorization", bearerToken("admin", "password")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/records")
                        .header("Authorization", bearerToken("admin", "password"))
                        .param("search", "transfer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == %s)]".formatted(recordId)).doesNotExist());
    }

    @Test
    void adminCanCreateUser() throws Exception {
        String body = """
                {
                  "name": "Ops Admin",
                  "username": "opsadmin",
                  "email": "ops@finance.local",
                  "password": "securePass123",
                  "roles": ["ADMIN", "ANALYST"],
                  "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("opsadmin"))
                .andExpect(jsonPath("$.email").value("ops@finance.local"))
                .andExpect(jsonPath("$.roles[0]").exists());
    }

    @Test
    void creatingDuplicateUsernameReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Duplicate Admin",
                                  "username": "admin",
                                  "email": "duplicate-admin@finance.local",
                                  "password": "securePass123",
                                  "roles": ["ADMIN"],
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already in use"));
    }

    @Test
    void creatingUserWithInvalidRoleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invalid Role User",
                                  "username": "invalidrole",
                                  "email": "invalid-role@finance.local",
                                  "password": "securePass123",
                                  "roles": ["SUPER_ADMIN"],
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("One or more roles are invalid"));
    }

    @Test
    void adminCanUpdateUserPasswordAndLoginWithNewPassword() throws Exception {
        String createBody = """
                {
                  "name": "Password Reset User",
                  "username": "resetuser",
                  "email": "reset@finance.local",
                  "password": "initialPass1",
                  "roles": ["VIEWER"],
                  "status": "ACTIVE"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/users")
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdUserId = extractLongField(createResponse, "id");

        mockMvc.perform(patch("/api/users/{id}/password", createdUserId)
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "updatedPass1"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "resetuser",
                                  "password": "updatedPass1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("resetuser"));
    }

    @Test
    void adminCanDeleteUser() throws Exception {
        String createBody = """
                {
                  "name": "Delete Me",
                  "username": "deleteuser",
                  "email": "delete@finance.local",
                  "password": "deletePass1",
                  "roles": ["VIEWER"],
                  "status": "ACTIVE"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/users")
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdUserId = extractLongField(createResponse, "id");

        mockMvc.perform(delete("/api/users/{id}", createdUserId)
                        .header("Authorization", bearerToken("admin", "password")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{id}", createdUserId)
                        .header("Authorization", bearerToken("admin", "password")))
                .andExpect(status().isNotFound());
    }

    @Test
    void softDeletedUserCannotLogin() throws Exception {
        String createResponse = mockMvc.perform(post("/api/users")
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Soft Deleted User",
                                  "username": "softdelete",
                                  "email": "softdelete@finance.local",
                                  "password": "deletePass1",
                                  "roles": ["VIEWER"],
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdUserId = extractLongField(createResponse, "id");

        mockMvc.perform(delete("/api/users/{id}", createdUserId)
                        .header("Authorization", bearerToken("admin", "password")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "softdelete",
                                  "password": "deletePass1"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recordValidationReturnsBadRequest() throws Exception {
        String body = """
                {
                  "amount": 0,
                  "type": "EXPENSE",
                  "category": "",
                  "date": "2026-04-04"
                }
                """;

        mockMvc.perform(post("/api/records")
                        .header("Authorization", bearerToken("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void invalidDateRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/records")
                        .header("Authorization", bearerToken("admin", "password"))
                        .param("startDate", "2026-04-10")
                        .param("endDate", "2026-04-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("startDate must be before or equal to endDate"));
    }

    @Test
    void invalidPaginationReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/records")
                        .header("Authorization", bearerToken("admin", "password"))
                        .param("page", "-1")
                        .param("size", "200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginRateLimitReturnsTooManyRequests() throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "ratelimit-user",
                                      "password": "wrong-password"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ratelimit-user",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many login attempts. Please try again later."));
    }

    private String bearerToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String prefix = "\"accessToken\":\"";
        int start = response.indexOf(prefix);
        if (start < 0) {
            throw new IllegalStateException("accessToken not found in login response");
        }

        start += prefix.length();
        int end = response.indexOf('"', start);
        return "Bearer " + response.substring(start, end);
    }

    private Long extractLongField(String json, String fieldName) {
        String prefix = "\"" + fieldName + "\":";
        int start = json.indexOf(prefix);
        if (start < 0) {
            throw new IllegalStateException(fieldName + " not found in response");
        }

        start += prefix.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }
}
