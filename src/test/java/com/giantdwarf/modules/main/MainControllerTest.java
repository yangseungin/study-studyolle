package com.giantdwarf.modules.main;

import com.giantdwarf.infra.AbstractContainerBaseTest;
import com.giantdwarf.infra.MockMvcTest;
import com.giantdwarf.modules.account.AccountRepository;
import com.giantdwarf.modules.account.AccountService;
import com.giantdwarf.modules.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class MainControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;

    @BeforeEach
    public void 계정추가() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("yang");
        signUpForm.setEmail("rhfpdk12@gmail.com");
        signUpForm.setPassword("qwerasdf");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    public void 계정삭제() {
        accountRepository.deleteAll();
    }

    @Test
    public void 로그인성공_이메일() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "rhfpdk12@gmail.com")
                .param("password", "qwerasdf")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("yang"));
    }

    @Test
    public void 로그인성공_닉네임() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "yang")
                .param("password", "qwerasdf")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("yang"));
    }

    @Test
    public void 로그인실패() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "unknown")
                .param("password", "password")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @WithMockUser
    @Test
    public void 로그아웃() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

}