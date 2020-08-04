package com.giantdwarf.settings;

import com.giantdwarf.account.AccountRepository;
import com.giantdwarf.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    PasswordEncoder passwordEncoder;

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
    public void 닉네임_수정폼() throws Exception {
        String bio = "자기소개 수정.";
        mockMvc.perform(get(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("yang")
    @Test
    public void 닉네임수정_입력값정상() throws Exception {
        String newNickname = "yang2";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        Account yang2 = accountRepository.findByNickname("yang2");
        assertEquals(newNickname, yang2.getNickname());
    }

    @WithAccount("yang")
    @Test
    public void 닉네임수정_입력값비정상() throws Exception {
        String newWrongNickname = "NicknameIsTooLongNicknameIsTooLongNicknameIsTooLongNicknameIsTooLongNicknameIsTooLong";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                .param("nickname", newWrongNickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().hasErrors());
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

    @WithAccount("yang")
    @Test
    public void 패스워드_수정폼() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("yang")
    @Test
    public void 패스워드수정_입력값정상() throws Exception {
        String newPassword = "12345678";
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm", newPassword)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account yang = accountRepository.findByNickname("yang");
        assertTrue(passwordEncoder.matches(newPassword, yang.getPassword()));
    }

    @WithAccount("yang")
    @Test
    public void 패스워드수정_입력값비정상_패스워드불일치() throws Exception {
        String newPassword = "12345678";
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm", "wrongpassword")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithAccount("yang")
    @Test
    public void 패스워드수정_입력값비정상_입력조건위배() throws Exception {
        String newPassword = "1234567";
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm", "wrong")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors());
    }
}

