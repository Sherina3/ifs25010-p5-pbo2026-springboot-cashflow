package org.delcom.ifs25010springbootcashflow.framework.auth;

import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Pengelola sesi login: menyimpan ID user di VaadinSession + HTTP session + cookie bertanda tangan
 * (HMAC-SHA256), sehingga sesi dapat dipulihkan setelah refresh. Flag LOGGED_OUT_KEY mencegah
 * pemulihan otomatis setelah pengguna logout.
 */
public final class AuthSession {
    public static final String COOKIE_NAME = "cashflow_uid";
    static final String USER_ID_KEY = "cashflow.userId";
    static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final int COOKIE_MAX_AGE_SECONDS = 8 * 60 * 60;
    private static final String FALLBACK_SECRET = randomSecret();

    private AuthSession() {
    }

    /** Menandai user sebagai sudah login pada sesi saat ini dan menulis cookie. */
    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, user.getId());
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        session.getSession().setAttribute(USER_ID_KEY, user.getId());
        writeCookie(sign(user.getId()), COOKIE_MAX_AGE_SECONDS);
    }

    /** Mengakhiri sesi login dan menghapus cookie. */
    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        session.setAttribute(USER_ID_KEY, null);
        session.getSession().removeAttribute(USER_ID_KEY);
        writeCookie("", 0);
    }

    /** User yang sedang login (dipulihkan dari cookie bila perlu), atau kosong jika belum login. */
    public static Optional<User> currentUser(IUserRepository userRepository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return Optional.empty();
        }
        Object storedId = session.getAttribute(USER_ID_KEY);
        Optional<Integer> userId = storedId instanceof Integer id ? Optional.of(id) : restoreFromCookie();
        Optional<User> user = userId.flatMap(userRepository::findById);
        user.ifPresent(found -> session.setAttribute(USER_ID_KEY, found.getId()));
        return user;
    }

    private static Optional<Integer> restoreFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static String sign(int userId) {
        return userId + "." + hmac(String.valueOf(userId));
    }

    private static Optional<Integer> verify(String value) {
        int dot = value == null ? -1 : value.indexOf('.');
        if (dot <= 0) {
            return Optional.empty();
        }
        try {
            int userId = Integer.parseInt(value.substring(0, dot));
            byte[] expected = hmac(String.valueOf(userId)).getBytes(StandardCharsets.UTF_8);
            byte[] actual = value.substring(dot + 1).getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(expected, actual) ? Optional.of(userId) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Gagal menandatangani cookie sesi", ex);
        }
    }

    /** Rahasia penandatangan dari SESSION_SECRET (.env / environment); acak per-start bila tidak diset. */
    private static String secret() {
        String configured = System.getProperty("SESSION_SECRET");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("SESSION_SECRET");
        }
        return configured == null || configured.isBlank() ? FALLBACK_SECRET : configured;
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
