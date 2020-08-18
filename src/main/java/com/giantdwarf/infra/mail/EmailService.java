package com.giantdwarf.infra.mail;

public interface EmailService {

    void sendEmail(EmailMessage emailMessage);
}
