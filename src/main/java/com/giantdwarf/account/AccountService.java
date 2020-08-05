package com.giantdwarf.account;

import com.giantdwarf.domain.Account;
import com.giantdwarf.domain.Tag;
import com.giantdwarf.settings.form.Notifications;
import com.giantdwarf.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account savedAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(savedAccount);
        return savedAccount;
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

    public void sendSignUpConfirmEmail(Account savedAccount) {
        //메일 전송 토큰생성
        savedAccount.generateEmailCheckToken();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("스터디올래, 회원 가입 인증");
        simpleMailMessage.setTo(savedAccount.getEmail());
        simpleMailMessage.setText("http://localhost:8080/check-email-token?token=" + savedAccount.getEmailCheckToken() + "&email=" + savedAccount.getEmail());
        javaMailSender.send(simpleMailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (Objects.isNull(account)) {
            account = accountRepository.findByNickname(emailOrNickname);
        }
        if (Objects.isNull(account)) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account);   // source에 있는것을 destination에 등록해준다.
                                            // account.setUrl(profile.getUrl())처럼 -url, occupation, location, bio, img등등을 다 해줄필요 없음
        accountRepository.save(account);

    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);    //merge
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("스터디올래, 로그인 링크");
        simpleMailMessage.setTo(account.getEmail());
        simpleMailMessage.setText("http://localhost:8080/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        javaMailSender.send(simpleMailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }
}
