package com.giantdwarf.modules.event;

import com.giantdwarf.infra.AbstractContainerBaseTest;
import com.giantdwarf.infra.MockMvcTest;
import com.giantdwarf.modules.account.Account;
import com.giantdwarf.modules.account.AccountFactory;
import com.giantdwarf.modules.account.AccountRepository;
import com.giantdwarf.modules.account.WithAccount;
import com.giantdwarf.modules.study.Study;
import com.giantdwarf.modules.study.StudyFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;

    @Test
    @WithAccount("yang")
    void 선착순모임_참가신청_자동수락() throws Exception {
        Account admin = accountFactory.createAccount("admin");
        Study study = studyFactory.createStudy("test-study", admin);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, admin);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account yang = accountRepository.findByNickname("yang");
        isAccepted(yang, event);
    }

    @Test
    @WithAccount("yang")
    void 선착순모임_참가신청_대기중() throws Exception {
        Account admin = accountFactory.createAccount("admin");
        Study study = studyFactory.createStudy("test-study", admin);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, admin);

        Account may = accountFactory.createAccount("may");
        Account june = accountFactory.createAccount("june");
        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, june);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account yang = accountRepository.findByNickname("yang");
        isNotAccepted(yang, event);
    }

    @Test
    @WithAccount("yang")
    void 선착순모임_참가신청자_취소_다음참가자_자동신청() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Account admin = accountFactory.createAccount("admin");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", admin);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, admin);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, yang);
        eventService.newEnrollment(event, admin);

        isAccepted(may, event);
        isAccepted(yang, event);
        isNotAccepted(admin, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(admin, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, yang));
    }

    @Test
    @WithAccount("yang")
    void 선착순모임_대기자의_취소() throws Exception {
        Account yang = accountRepository.findByNickname("yang");
        Account admin = accountFactory.createAccount("admin");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", admin);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, admin);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, admin);
        eventService.newEnrollment(event, yang);

        isAccepted(may, event);
        isAccepted(admin, event);
        isNotAccepted(yang, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(admin, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, yang));
    }

    private void isNotAccepted(Account admin, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, admin).isAccepted());
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    @Test
    @WithAccount("yang")
    void 관리자확인모임_참가신청_대기중() throws Exception {
        Account admin = accountFactory.createAccount("admin");
        Study study = studyFactory.createStudy("test-study", admin);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, admin);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account yang = accountRepository.findByNickname("yang");
        isNotAccepted(yang, event);
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }
}