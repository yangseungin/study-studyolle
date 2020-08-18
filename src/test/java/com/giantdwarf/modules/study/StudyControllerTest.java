package com.giantdwarf.modules.study;

import com.giantdwarf.infra.AbstractContainerBaseTest;
import com.giantdwarf.infra.MockMvcTest;
import com.giantdwarf.modules.account.Account;
import com.giantdwarf.modules.account.AccountFactory;
import com.giantdwarf.modules.account.AccountRepository;
import com.giantdwarf.modules.account.WithAccount;
import com.giantdwarf.modules.tag.TagRepository;
import com.giantdwarf.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
public class StudyControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired TagRepository tagRepository;
    @Autowired ZoneRepository zoneRepository;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @WithAccount("yang")
    void 스터디개설_폼_조회() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @WithAccount("yang")
    void 스터디개설_성공() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("yang");
        assertTrue(study.getManagers().contains(account));
    }

    @Test
    @WithAccount("yang")
    void 스터디개설_실패() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "Long wrong path Long wrong path Long wrong path Long wrong path Long wrong path Long wrong path Long wrong path ")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("account"));

        Study study = studyRepository.findByPath("wrong path");
        assertNull(study);
    }

    @Test
    @WithAccount("yang")
    void 스터디_조회() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");

        Account yang = accountRepository.findByNickname("yang");
        studyService.createNewStudy(study, yang);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("yang")
    void 스터디_가입() throws Exception {
        Account admin = accountFactory.createAccount("admin");
        Study study = studyFactory.createStudy("test-study", admin);

        mockMvc.perform(get("/study/" + study.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account yang = accountRepository.findByNickname("yang");
        assertTrue(study.getMembers().contains(yang));
    }

    @Test
    @WithAccount("yang")
    void 스터디_탈퇴() throws Exception {
        Account admin = accountFactory.createAccount("admin");
        Study study = studyFactory.createStudy("test-study", admin);

        Account yang = accountRepository.findByNickname("yang");
        studyService.addMember(study, yang);

        mockMvc.perform(get("/study/" + study.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(yang));
    }
}