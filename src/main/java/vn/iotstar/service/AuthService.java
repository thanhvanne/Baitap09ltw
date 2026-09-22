package vn.iotstar.service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final OtpTokenRepository otps;
    private final PasswordEncoder encoder;
    private final MailService mailService;

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

        if (users.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException(
                "Username đã tồn tại."
            );
        }

        if (users.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException(
                "Email đã tồn tại."
            );
        }

        Role role = roles
            .findByNameIgnoreCase("USER")
            .orElseThrow();

        User user = new User();

        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setFullName(dto.getFullName().trim());

        user.setPassword(
            encoder.encode(dto.getPassword())
        );

        user.setEnabled(false);
        user.setRole(role);

        users.save(user);

        createAndSendOtp(
            user,
            "REGISTER",
            "Xác thực đăng ký"
        );
    }

    @Transactional
    public void resendRegisterOtp(String email) {

        User user = users.findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Không tìm thấy email."
                )
            );

        createAndSendOtp(
            user,
            "REGISTER",
            "Xác thực đăng ký"
        );
    }

    @Transactional
    public void verifyRegister(
            String email,
            String otp
    ) {

        User user = users
            .findByEmailIgnoreCase(email)
            .orElseThrow();

        OtpToken token = validOtp(
            user,
            "REGISTER",
            otp
        );

        token.setUsed(true);

        user.setEnabled(true);

        otps.save(token);
        users.save(user);
    }

    @Transactional
    public void forgotPassword(String email) {

        User user = users
            .findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Không tìm thấy email."
                )
            );

        createAndSendOtp(
            user,
            "FORGOT_PASSWORD",
            "Quên mật khẩu"
        );
    }

    @Transactional
    public void resetPassword(
            ResetPasswordDTO dto
    ) {

        User user = users
            .findByEmailIgnoreCase(dto.getEmail())
            .orElseThrow();

        OtpToken token = validOtp(
            user,
            "FORGOT_PASSWORD",
            dto.getOtp()
        );

        token.setUsed(true);

        user.setPassword(
            encoder.encode(dto.getPassword())
        );

        otps.save(token);
        users.save(user);
    }

    private OtpToken validOtp(
            User user,
            String type,
            String otp
    ) {

        OtpToken token = otps
            .findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(
                user.getId(),
                type
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "OTP không tồn tại."
                )
            );

        if (!token.getOtp().equals(otp)) {
            throw new IllegalArgumentException(
                "OTP không đúng."
            );
        }

        if (token.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                "OTP đã hết hạn."
            );
        }

        return token;
    }

    private void createAndSendOtp(
            User user,
            String type,
            String subject
    ) {

        String otp = String.format(
            "%06d",
            ThreadLocalRandom.current()
                .nextInt(0, 1_000_000)
        );

        OtpToken token = new OtpToken();

        token.setOtp(otp);
        token.setType(type);
        token.setUsed(false);
        token.setUser(user);

        token.setExpiresAt(
            LocalDateTime.now().plusMinutes(5)
        );

        otps.save(token);

        mailService.sendOtp(
            user.getEmail(),
            otp,
            subject
        );
    }
}