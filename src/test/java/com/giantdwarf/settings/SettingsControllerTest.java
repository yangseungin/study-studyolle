package com.giantdwarf.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giantdwarf.account.AccountRepository;
import com.giantdwarf.account.AccountService;
import com.giantdwarf.domain.Account;
import com.giantdwarf.domain.Tag;
import com.giantdwarf.domain.Zone;
import com.giantdwarf.settings.form.TagForm;
import com.giantdwarf.settings.form.ZoneForm;
import com.giantdwarf.tag.TagRepository;
import com.giantdwarf.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.giantdwarf.settings.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    public void before() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    public void after() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("yang")
    @Test
    public void 닉네임_수정폼() throws Exception {
        String bio = "자기소개 수정.";
        mockMvc.perform(get(ROOT + SETTINGS + ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("yang")
    @Test
    public void 닉네임수정_입력값정상() throws Exception {
        String newNickname = "yang2";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + ACCOUNT))
                .andExpect(flash().attributeExists("message"));

        Account yang2 = accountRepository.findByNickname("yang2");
        assertEquals(newNickname, yang2.getNickname());
    }

    @WithAccount("yang")
    @Test
    public void 닉네임수정_입력값비정상() throws Exception {
        String newWrongNickname = "NicknameIsTooLongNicknameIsTooLongNicknameIsTooLongNicknameIsTooLongNicknameIsTooLong";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                .param("nickname", newWrongNickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+ACCOUNT))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().hasErrors());
    }

    @WithAccount("yang")
    @Test
    public void 프로필_수정폼() throws Exception {
        String bio = "자기소개 수정.";
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("yang")
    @Test
    public void 프로필수정_입력값정상() throws Exception {
        String bio = "자기소개 수정.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account yang = accountRepository.findByNickname("yang");
        assertEquals(bio, yang.getBio());
    }

    @WithAccount("yang")
    @Test
    public void 프로필수정_입력값비정상() throws Exception {
        String bio = "너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 너무 길면 에러남 ";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account yang = accountRepository.findByNickname("yang");
        assertNull(yang.getBio());
    }

    @WithAccount("yang")
    @Test
    public void 패스워드_수정폼() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("yang")
    @Test
    public void 패스워드수정_입력값정상() throws Exception {
        String newPassword = "12345678";
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm", newPassword)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account yang = accountRepository.findByNickname("yang");
        assertTrue(passwordEncoder.matches(newPassword, yang.getPassword()));
    }

    @WithAccount("yang")
    @Test
    public void 패스워드수정_입력값비정상_패스워드불일치() throws Exception {
        String newPassword = "12345678";
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm", "wrongpassword")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithAccount("yang")
    @Test
    public void 패스워드수정_입력값비정상_입력조건위배() throws Exception {
        String newPassword = "1234567";
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm", "wrong")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PASSWORD))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors());
    }

    @WithAccount("yang")
    @Test
    public void 계정_태그_수정폼() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("yang")
    @Test
    public void 계정_태그_추가() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("yang").getTags().contains(newTag));
    }

    @WithAccount("yang")
    @Test
    public void 계정태그_삭제() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(yang, newTag);

        assertTrue(yang.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(yang.getTags().contains(newTag));
    }
    @WithAccount("yang")
    @Test
    public void 계정_지역_수정폼() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("yang")
    @Test
    public void 계정_지역정보_추가() throws Exception {

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account yang = accountRepository.findByNickname("yang");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(yang.getZones().contains(zone));
    }

    @WithAccount("yang")
    @Test
    public void 계정_지역정보_삭제() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(yang, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(yang.getZones().contains(zone));
    }

}

