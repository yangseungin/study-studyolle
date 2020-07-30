package com.giantdwarf.account;

import com.giantdwarf.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    @InitBinder("signUpForm") // SignUpForm 데이터를 받을떄 바인딩해줌.
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
//        model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute(new SignUpForm()); //class 이름과 동일한 attribute를 사용하면 생략 가능
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        //계정 저장후
        Account account = new Account().builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(signUpForm.getPassword())
                .studyCreatedByWeb(true)
                .studyUpdatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .build();

        Account savedAccount = accountRepository.save(account);
        savedAccount.generateEmailCheckToken();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("스터디올래, 회원 가입 인증");
        simpleMailMessage.setTo(savedAccount.getEmail());
        simpleMailMessage.setText("/check-email-token?token="+savedAccount.getEmailCheckToken()+"&email="+savedAccount.getEmail());

        //이메일 보내기
        javaMailSender.send(simpleMailMessage);

        return "redirect:/";
    }
}
