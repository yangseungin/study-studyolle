package com.giantdwarf.account;

import com.giantdwarf.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void processNewAccount(SignUpForm signUpForm) {
        Account savedAccount = saveNewAccount(signUpForm);
        savedAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(savedAccount);
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        Account account = new Account().builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyUpdatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .build();

        return accountRepository.save(account);
    }

    private void sendSignUpConfirmEmail(Account savedAccount) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("스터디올래, 회원 가입 인증");
        simpleMailMessage.setTo(savedAccount.getEmail());
        simpleMailMessage.setText("http://localhost:8080/check-email-token?token="+savedAccount.getEmailCheckToken()+"&email="+savedAccount.getEmail());
        javaMailSender.send(simpleMailMessage);
    }
}
