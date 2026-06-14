package com.hospital.service;

import com.hospital.dto.AuthDTO;
import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import com.hospital.security.JwtUtils;
import com.hospital.security.UserDetailsImpl;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Unit Tests")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = TestDataBuilder.buildAdminUser();
        adminUser.setId(1L);
    }

    // ── login ──────────────────────────────────────────────────────
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("✅ تسجيل دخول ناجح يرجع JWT Response")
        void shouldLoginSuccessfully() {
            UserDetailsImpl userDetails = UserDetailsImpl.build(adminUser);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            given(authenticationManager.authenticate(any())).willReturn(auth);
            given(jwtUtils.generateJwtToken(auth)).willReturn("mocked.jwt.token");
            given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));

            AuthDTO.LoginRequest req = new AuthDTO.LoginRequest();
            req.setUsernameOrEmail("admin");
            req.setPassword("admin123");

            AuthDTO.JwtResponse response = authService.login(req);

            assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
            assertThat(response.getUsername()).isEqualTo("admin");
            assertThat(response.getType()).isEqualTo("Bearer");
            assertThat(response.getRoles()).contains(User.Role.ROLE_ADMIN);
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند كلمة مرور خاطئة")
        void shouldThrowOnBadCredentials() {
            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            AuthDTO.LoginRequest req = new AuthDTO.LoginRequest();
            req.setUsernameOrEmail("admin");
            req.setPassword("wrong");

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    // ── register ──────────────────────────────────────────────────
    @Nested
    @DisplayName("register()")
    class Register {

        private AuthDTO.RegisterRequest buildRequest() {
            AuthDTO.RegisterRequest req = new AuthDTO.RegisterRequest();
            req.setUsername("newuser");
            req.setEmail("newuser@hospital.com");
            req.setPassword("password123");
            req.setRoles(Set.of(User.Role.ROLE_PATIENT));
            return req;
        }

        @Test
        @DisplayName("✅ تسجيل مستخدم جديد بنجاح")
        void shouldRegisterSuccessfully() {
            AuthDTO.RegisterRequest req = buildRequest();

            given(userRepository.existsByUsername("newuser")).willReturn(false);
            given(userRepository.existsByEmail("newuser@hospital.com")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("$2a$10$encoded");
            given(userRepository.save(any(User.class))).willReturn(adminUser);

            AuthDTO.MessageResponse response = authService.register(req);

            assertThat(response.getMessage()).contains("نجاح");
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("✅ تسجيل بدون تحديد دور يعطي ROLE_PATIENT تلقائياً")
        void shouldDefaultToPatientRole() {
            AuthDTO.RegisterRequest req = buildRequest();
            req.setRoles(null);

            given(userRepository.existsByUsername(any())).willReturn(false);
            given(userRepository.existsByEmail(any())).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("$2a$10$encoded");
            given(userRepository.save(any(User.class))).willReturn(adminUser);

            authService.register(req);

            then(userRepository).should().save(argThat(u ->
                    u.getRoles().contains(User.Role.ROLE_PATIENT)));
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند تكرار اسم المستخدم")
        void shouldThrowWhenUsernameExists() {
            AuthDTO.RegisterRequest req = buildRequest();
            given(userRepository.existsByUsername("newuser")).willReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("اسم المستخدم");

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("❌ يرمي استثناء عند تكرار البريد الإلكتروني")
        void shouldThrowWhenEmailExists() {
            AuthDTO.RegisterRequest req = buildRequest();
            given(userRepository.existsByUsername(any())).willReturn(false);
            given(userRepository.existsByEmail("newuser@hospital.com")).willReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("البريد الإلكتروني");
        }

        @Test
        @DisplayName("✅ يشفر كلمة المرور قبل الحفظ")
        void shouldEncodePasswordBeforeSaving() {
            AuthDTO.RegisterRequest req = buildRequest();

            given(userRepository.existsByUsername(any())).willReturn(false);
            given(userRepository.existsByEmail(any())).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("$2a$10$encoded");
            given(userRepository.save(any())).willReturn(adminUser);

            authService.register(req);

            then(passwordEncoder).should().encode("password123");
            then(userRepository).should().save(argThat(u ->
                    u.getPassword().equals("$2a$10$encoded")));
        }
    }
}
