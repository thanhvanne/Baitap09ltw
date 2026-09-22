package vn.iotstar.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(
            String email,
            String otp,
            String purpose
    ) {

        SimpleMailMessage message =
            new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
            "IOTSTAR - " + purpose
        );

        message.setText(
            "Mã OTP của bạn là: " + otp
            + "\nOTP có hiệu lực trong 5 phút."
        );

        mailSender.send(message);
    }
}