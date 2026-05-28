package com.bookstore.backend.service.impl;

import com.bookstore.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Mã Xác Thực OTP - Tiệm Sách Neth BookPoint");

            String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 550px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);">
                    <div style="background-color: #1e2430; padding: 25px; text-align: center; border-bottom: 3px solid #ffc707;">
                        <h2 style="color: #ffffff; margin: 0; font-size: 24px; letter-spacing: 1px;">NETH BOOKPOINT</h2>
                    </div>
                    <div style="padding: 30px; background-color: #ffffff; color: #333333; line-height: 1.6;">
                        <p style="margin-top: 0; font-size: 16px;">Xin chào,</p>
                        <p style="font-size: 15px;">Bạn đang thực hiện thao tác <strong>%s</strong> tại hệ thống quản lý tiệm sách của chúng tôi.</p>
                        <div style="background-color: #f8f9fa; border-left: 4px solid #ffc707; padding: 15px; margin: 25px 0; text-align: center;">
                            <p style="margin: 0; font-size: 13px; color: #666666; text-transform: uppercase; letter-spacing: 1px;">Mã xác thực của bạn là:</p>
                            <h1 style="margin: 10px 0 0 0; color: #1e2430; font-size: 36px; letter-spacing: 5px; font-weight: bold;">%s</h1>
                        </div>
                        <p style="font-size: 14px; color: #ff5555; font-style: italic;">* Lưu ý: Mã xác thực này có hiệu lực trong vòng 3 phút và chỉ sử dụng một lần duy nhất. Vui lòng tuyệt đối không chia sẻ mã này cho bất kỳ ai.</p>
                    </div>
                    <div style="background-color: #f1f3f5; padding: 15px; text-align: center; font-size: 12px; color: #888888; border-top: 1px solid #e9ecef;">
                        Đây là email tự động của hệ thống, vui lòng không phản hồi email này.
                    </div>
                </div>
                """.formatted(purpose, otpCode);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi Mail: " + e.getMessage());
        }
    }
}