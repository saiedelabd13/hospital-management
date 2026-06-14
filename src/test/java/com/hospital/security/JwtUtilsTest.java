package com.hospital.security;

import com.hospital.entity.User;
import com.hospital.util.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtils - Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "TestSecretKeyForJWTTokenGenerationInUnitAndIntegrationTests2024VeryLong");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000L);
    }

    private Authentication buildAuthentication(String username) {
        User user = TestDataBuilder.buildAdminUser();
        user.setUsername(username);
        user.setId(1L);
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("✅ يولد JWT Token بنجاح")
    void shouldGenerateToken() {
        Authentication auth = buildAuthentication("admin");

        String token = jwtUtils.generateJwtToken(auth);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature
    }

    @Test
    @DisplayName("✅ يستخرج اسم المستخدم من التوكن")
    void shouldExtractUsernameFromToken() {
        Authentication auth = buildAuthentication("admin");
        String token = jwtUtils.generateJwtToken(auth);

        String username = jwtUtils.getUsernameFromJwtToken(token);

        assertThat(username).isEqualTo("admin");
    }

    @Test
    @DisplayName("✅ يتحقق من صحة التوكن")
    void shouldValidateValidToken() {
        Authentication auth = buildAuthentication("admin");
        String token = jwtUtils.generateJwtToken(auth);

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("❌ يرفض توكن غير صحيح")
    void shouldRejectInvalidToken() {
        boolean isValid = jwtUtils.validateJwtToken("invalid.token.here");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("❌ يرفض توكن فارغ")
    void shouldRejectEmptyToken() {
        boolean isValid = jwtUtils.validateJwtToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("❌ يرفض توكن منتهي الصلاحية")
    void shouldRejectExpiredToken() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1000L);
        Authentication auth = buildAuthentication("admin");
        String expiredToken = jwtUtils.generateJwtToken(auth);

        boolean isValid = jwtUtils.validateJwtToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("✅ يولد توكنات مختلفة لمستخدمين مختلفين")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtUtils.generateJwtToken(buildAuthentication("user1"));
        String token2 = jwtUtils.generateJwtToken(buildAuthentication("user2"));

        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtils.getUsernameFromJwtToken(token1)).isEqualTo("user1");
        assertThat(jwtUtils.getUsernameFromJwtToken(token2)).isEqualTo("user2");
    }
}
