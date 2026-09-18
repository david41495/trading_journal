package com.davidserrano.tradejournal.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedTradeApiRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanRegisterLoginAndLoadTheirProfile() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "David",
                                  "email": "david@example.com",
                                  "password": "secure-pass-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName").value("David"))
                .andExpect(jsonPath("$.email").value("david@example.com"));

        MvcResult login = mockMvc.perform(formLogin("/api/auth/login")
                        .user("david@example.com")
                        .password("secure-pass-123"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession)
                login.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("David"))
                .andExpect(jsonPath("$.email").value("david@example.com"));
    }
}
