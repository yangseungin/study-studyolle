package com.giantdwarf.modules.account;

import com.giantdwarf.infra.MockMvcTest;
import com.giantdwarf.infra.mail.EmailMessage;
import com.giantdwarf.infra.mail.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @MockBean EmailService emailService;

    @Test
    public void 인증메일확인_입력값오류() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "asgaefdf")
                .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @Test
    public void 인증메일확인_입력값정상() throws Exception {
        Account account = new Account();
        account.setEmail("rhfpdk12@gmail.com");
        account.setPassword("qwerasdf12");
        account.setNickname("yang");
        Account savedAccount = accountRepository.save(account);
        savedAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", savedAccount.getEmailCheckToken())
                .param("email", savedAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("yang"));
    }

    @Test
    public void 회원가입화면_확인() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @Test
    public void 회원가입_입력값_오류() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "yang")
                .param("email", "email..")
                .param("password", "1235")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    public void 회원가입_입력값_정상() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "yang")
                .param("email", "rhfpdk12@gmail.com")
                .param("password", "qwerasdf")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("yang"));

        Account account = accountRepository.findByEmail("rhfpdk12@gmail.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "qwerasdf");
        assertNotNull(account.getEmailCheckToken());
        assertTrue(accountRepository.existsByEmail("rhfpdk12@gmail.com"));
        then(emailService).should().sendEmail(any(EmailMessage.class));

    }

}