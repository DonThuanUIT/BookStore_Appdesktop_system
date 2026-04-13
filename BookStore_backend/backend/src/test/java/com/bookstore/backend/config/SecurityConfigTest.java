package com.bookstore.backend.config;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAdminToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                .jwt(jwt -> jwt.subject("admin-user").claim("roles", List.of("ADMIN")))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectStaffFromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF"))
                                .jwt(jwt -> jwt.subject("staff-user").claim("roles", List.of("STAFF")))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowStaffToStaffEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/staff")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF"))
                                .jwt(jwt -> jwt.subject("staff-user").claim("roles", List.of("STAFF")))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowCustomerToCustomerEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/customer")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                                .jwt(jwt -> jwt.subject("customer-user").claim("roles", List.of("CUSTOMER")))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectCustomerFromStaffEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/staff")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                                .jwt(jwt -> jwt.subject("customer-user").claim("roles", List.of("CUSTOMER")))))
                .andExpect(status().isForbidden());
    }
}
