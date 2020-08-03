package com.giantdwarf.settings;

import com.giantdwarf.account.AccountRepository;
import com.giantdwarf.account.AccountService;
import com.giantdwarf.account.SignUpForm;
import com.giantdwarf.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    public void 계정삭제() {
        accountRepository.deleteAll();
    }

    @WithAccount("yang")
    @Test
    public void 프로필_수정폼() throws Exception {
        String bio = "자기소개 수정.";
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("yang")
    @Test
    public void 프로필수정_입력값정상() throws Exception {
        String bio = "자기소개 수정.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account yang = accountRepository.findByNickname("yang");
        assertEquals(bio, yang.getBio());
    }

    @WithAccount("yang")
    @Test
    public void 프로필수정_입력값비정상() throws Exception {
        String bio = "너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 ";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account yang = accountRepository.findByNickname("yang");
        assertNull(yang.getBio());
    }

}