package com.billiontech.bookkeeping.auth;

import com.billiontech.bookkeeping.entity.Client;
import com.billiontech.bookkeeping.entity.Tenant;
import com.billiontech.bookkeeping.entity.User;
import com.billiontech.bookkeeping.repository.ClientRepository;
import com.billiontech.bookkeeping.repository.RefreshTokenRepository;
import com.billiontech.bookkeeping.repository.TenantRepository;
import com.billiontech.bookkeeping.repository.UserRepository;
import com.billiontech.bookkeeping.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MultiTenantAuthTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    private Tenant tenantA;
    private Tenant tenantB;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        clientRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        tenantA = new Tenant();
        tenantA.setName("Firm A");
        tenantA.setSubdomain("firm-a");
        tenantA = tenantRepository.save(tenantA);

        tenantB = new Tenant();
        tenantB.setName("Firm B");
        tenantB.setSubdomain("firm-b");
        tenantB = tenantRepository.save(tenantB);

        userA = new User();
        userA.setTenant(tenantA);
        userA.setEmail("user-a@firma.com");
        userA.setPasswordHash(passwordEncoder.encode("PasswordA1!"));
        userA.setRole("ADMIN");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setTenant(tenantB);
        userB.setEmail("user-b@firmb.com");
        userB.setPasswordHash(passwordEncoder.encode("PasswordB1!"));
        userB.setRole("ADMIN");
        userB = userRepository.save(userB);

        // Two clients for tenant A
        Client clientA1 = new Client();
        clientA1.setTenant(tenantA);
        clientA1.setName("Client A1");
        clientRepository.save(clientA1);

        Client clientA2 = new Client();
        clientA2.setTenant(tenantA);
        clientA2.setName("Client A2");
        clientRepository.save(clientA2);

        // One client for tenant B
        Client clientB1 = new Client();
        clientB1.setTenant(tenantB);
        clientB1.setName("Client B1");
        clientRepository.save(clientB1);
    }

    @Test
    void loginAndListClients_shouldReturnOnlyTenantAClients() throws Exception {
        // Login as user A via the auth endpoint
        String loginJson = objectMapper.writeValueAsString(
                new java.util.LinkedHashMap<>() {{
                    put("email", "user-a@firma.com");
                    put("password", "PasswordA1!");
                }});

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tenantId").value(tenantA.getId().toString()))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // GET /api/clients with user A's token — should return only tenant A's clients
        mockMvc.perform(get("/api/clients")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tenantId").value(tenantA.getId().toString()))
                .andExpect(jsonPath("$[1].tenantId").value(tenantA.getId().toString()));
    }

    @Test
    void loginAsUserB_shouldReturnOnlyTenantBClients() throws Exception {
        String loginJson = objectMapper.writeValueAsString(
                new java.util.LinkedHashMap<>() {{
                    put("email", "user-b@firmb.com");
                    put("password", "PasswordB1!");
                }});

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        mockMvc.perform(get("/api/clients")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenantId").value(tenantB.getId().toString()))
                .andExpect(jsonPath("$[0].name").value("Client B1"));
    }

    @Test
    void accessClientsWithoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessHealthEndpoint_shouldNotRequireAuth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void loginWithWrongPassword_shouldReturn401() throws Exception {
        String loginJson = objectMapper.writeValueAsString(
                new java.util.LinkedHashMap<>() {{
                    put("email", "user-a@firma.com");
                    put("password", "WrongPassword!");
                }});

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }
}
