package vn.iotstar.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Service
public class AuthService {

    private static final String REGISTER =
        "REGISTER";

    private static final String FORGOT_PASSWORD =
        "FORGOT_PASSWORD";

    private static final int OTP_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository users;
    private final RoleRepository roles;
    private final OtpTokenRepository otps;
    private final PasswordEncoder encoder;
    private final MailService mailService;

    private final SecureRandom random =
        new SecureRandom();

    public AuthService(
            UserRepository users,
            RoleRepository roles,
            OtpTokenRepository otps,
            PasswordEncoder encoder,
            MailService mailService
    ) {
        this.users = users;
        this.roles = roles;
        this.otps = otps;
        this.encoder = encoder;
        this.mailService = mailService;
    }

    @Transactional
    public void register(RegisterDTO dto) {

        String username =
            dto.getUsername().trim();

        String email =
            dto.getEmail()
                .trim()
                .toLowerCase();

        if (users.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException(
                "Username đã tồn tại."
            );
        }

        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                "Email đã tồn tại."
            );
        }

        Role role = roles
            .findByNameIgnoreCase("USER")
            .orElseThrow(() ->
                new IllegalStateException(
                    "Chưa có role USER."
                )
            );

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(
            dto.getFullName().trim()
        );
        user.setPassword(
            encoder.encode(
                dto.getPassword()
            )
        );
        user.setEnabled(false);
        user.setRole(role);

        users.save(user);

        createAndSendOtp(
            user,
            REGISTER,
            "Xác thực đăng ký"
        );
    }

    @Transactional
    public void resendRegisterOtp(
            String email
    ) {

        User user =
            findUserByEmail(email);

        if (user.isEnabled()) {
            throw new IllegalArgumentException(
                "Tài khoản đã được xác thực."
            );
        }

        createAndSendOtp(
            user,
            REGISTER,
            "Xác thực đăng ký"
        );
    }

    @Transactional
    public void verifyRegister(
            String email,
            String otp
    ) {

        User user =
            findUserByEmail(email);

        if (user.isEnabled()) {
            throw new IllegalArgumentException(
                "Tài khoản đã được xác thực."
            );
        }

        OtpToken token =
            validOtp(
                user,
                REGISTER,
                otp
            );

        token.setUsed(true);
        user.setEnabled(true);

        otps.save(token);
        users.save(user);
    }

    @Transactional
    public void forgotPassword(
            String email
    ) {

        User user =
            findUserByEmail(email);

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                "Tài khoản chưa được xác thực."
            );
        }

        createAndSendOtp(
            user,
            FORGOT_PASSWORD,
            "Quên mật khẩu"
        );
    }

    @Transactional
    public void resetPassword(
            ResetPasswordDTO dto
    ) {

        if (!dto.getPassword().equals(
                dto.getConfirmPassword()
        )) {
            throw new IllegalArgumentException(
                "Xác nhận mật khẩu không khớp."
            );
        }

        User user =
            findUserByEmail(
                dto.getEmail()
            );

        OtpToken token =
            validOtp(
                user,
                FORGOT_PASSWORD,
                dto.getOtp()
            );

        token.setUsed(true);

        user.setPassword(
            encoder.encode(
                dto.getPassword()
            )
        );

        otps.save(token);
        users.save(user);
    }

    private OtpToken validOtp(
            User user,
            String type,
            String otp
    ) {

        if (otp == null
                || !otp.matches("\\d{6}")) {

            throw new IllegalArgumentException(
                "OTP phải gồm đúng 6 chữ số."
            );
        }

        OtpToken token =
            otps
                .findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(
                    user.getId(),
                    type
                )
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "OTP không tồn tại hoặc đã được sử dụng."
                    )
                );

        if (token.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                "OTP đã hết hạn."
            );
        }

        if (token.getAttempts()
                >= MAX_ATTEMPTS) {

            throw new IllegalArgumentException(
                "OTP đã bị khóa do nhập sai quá 5 lần. "
                + "Vui lòng yêu cầu mã mới."
            );
        }

        if (!encoder.matches(
                otp,
                token.getOtpHash()
        )) {

            token.setAttempts(
                token.getAttempts() + 1
            );

            otps.save(token);

            int remaining =
                MAX_ATTEMPTS
                    - token.getAttempts();

            if (remaining <= 0) {
                throw new IllegalArgumentException(
                    "OTP không đúng. Mã đã bị khóa, "
                    + "vui lòng yêu cầu OTP mới."
                );
            }

            throw new IllegalArgumentException(
                "OTP không đúng. Còn "
                + remaining
                + " lần thử."
            );
        }

        return token;
    }

    private void createAndSendOtp(
            User user,
            String type,
            String subject
    ) {

        otps
            .findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(
                user.getId(),
                type
            )
            .ifPresent(old -> {
                old.setUsed(true);
                otps.save(old);
            });

        String otp =
            String.format(
                "%06d",
                random.nextInt(1_000_000)
            );

        OtpToken token =
            new OtpToken();

        token.setOtpHash(
            encoder.encode(otp)
        );
        token.setType(type);
        token.setAttempts(0);
        token.setUsed(false);
        token.setUser(user);
        token.setCreatedAt(
            LocalDateTime.now()
        );
        token.setExpiresAt(
            LocalDateTime
                .now()
                .plusMinutes(OTP_MINUTES)
        );

        otps.save(token);

        mailService.sendOtp(
            user.getEmail(),
            otp,
            subject
        );
    }

    private User findUserByEmail(
            String email
    ) {

        if (email == null
                || email.isBlank()) {

            throw new IllegalArgumentException(
                "Email không được để trống."
            );
        }

        return users
            .findByEmailIgnoreCase(
                email.trim()
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Không tìm thấy email."
                )
            );
    }
}