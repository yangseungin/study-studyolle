package com.giantdwarf.modules.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giantdwarf.infra.AbstractContainerBaseTest;
import com.giantdwarf.infra.MockMvcTest;
import com.giantdwarf.modules.account.Account;
import com.giantdwarf.modules.account.AccountFactory;
import com.giantdwarf.modules.account.AccountRepository;
import com.giantdwarf.modules.account.WithAccount;
import com.giantdwarf.modules.account.form.TagForm;
import com.giantdwarf.modules.account.form.ZoneForm;
import com.giantdwarf.modules.tag.Tag;
import com.giantdwarf.modules.tag.TagRepository;
import com.giantdwarf.modules.zone.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingsControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyRepository studyRepository;
    @Autowired StudyService studyService;
    @Autowired ZoneRepository zoneRepository;
    @Autowired TagRepository tagRepository;
    @Autowired ObjectMapper objectMapper;

    @Test
    @WithAccount("yang")
    void 스터디소개수정폼_조회_실패_권한없는유저() throws Exception {
        Account admin = accountFactory.createAccount("admin");
        Study study = studyFactory.createStudy("test-study", admin);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("yang")
    void 스터디소개수정폼_조회_성공() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("yang")
    void 스터디소개_수정_성공() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);

        String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionUrl)
                .param("shortDescription", "short description")
                .param("fullDescription", "full description")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsDescriptionUrl))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("yang")
    void 스터디소개_수정_실패() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);

        String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionUrl)
                .param("shortDescription", "")
                .param("fullDescription", "full description")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"));
    }
    //배너이미지 테스트

    @Test
    @WithAccount("yang")
    void 스터디태그수정폼_조회_성공() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);

        String settingsTagsUrl = "/study/" + study.getPath() + "/settings/tags";
        mockMvc.perform(get(settingsTagsUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @Test
    @WithAccount("yang")
    void 스터디태그수정폼_태그추가() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("jpa");

        String settingsTagsUrl = "/study/" + study.getPath() + "/settings/tags/add";
        mockMvc.perform(post(settingsTagsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        Study studyToUpdate = studyService.getStudyToUpdate(yang, "test-study");
        assertTrue(studyToUpdate.getTags().size() == 1);
    }

    @Test
    @WithAccount("yang")
    void 스터디태그수정폼_태그삭제() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);
        Tag jpa = findOrCreateNew("jpa");
        studyService.addTag(study, jpa);

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("jpa");

        String settingsTagsUrl = "/study/" + study.getPath() + "/settings/tags/remove";
        mockMvc.perform(post(settingsTagsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        Study studyToUpdate = studyService.getStudyToUpdate(yang, "test-study");
        assertTrue(studyToUpdate.getTags().size() == 0);
    }

    @Test
    @WithAccount("yang")
    void 스터디지역수정폼_조회_성공() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);

        String settingsZonesUrl = "/study/" + study.getPath() + "/settings/zones";
        mockMvc.perform(get(settingsZonesUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/zones"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @Test
    @WithAccount("yang")
    void 스터디지역수정폼_지역추가() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName("Suwon(수원시)/Gyeonggi");

        String settingsTagsUrl = "/study/" + study.getPath() + "/settings/zones/add";
        mockMvc.perform(post(settingsTagsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        Study studyToUpdate = studyService.getStudyToUpdateZone(yang, "test-study");
        assertTrue(studyToUpdate.getZones().size() == 1);
    }

    @Test
    @WithAccount("yang")
    void 스터디지역수정폼_지역삭제() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Study study = studyFactory.createStudy("test-study", yang);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName("Suwon(수원시)/Gyeonggi");
        studyService.addZone(study, zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName()));

        String settingsTagsUrl = "/study/" + study.getPath() + "/settings/zones/remove";
        mockMvc.perform(post(settingsTagsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        Study studyToUpdate = studyService.getStudyToUpdateZone(yang, "test-study");
        assertTrue(studyToUpdate.getZones().size() == 0);
    }

    protected Tag findOrCreateNew(String tagTitle) {
        Tag tag = tagRepository.findByTitle(tagTitle);
        if (Objects.isNull(tag)) {
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }
        return tag;
    }
}