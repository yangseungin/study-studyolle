package com.giantdwarf.event;

import com.giantdwarf.domain.Account;
import com.giantdwarf.domain.Event;
import com.giantdwarf.domain.EventType;
import com.giantdwarf.domain.Study;
import com.giantdwarf.settings.WithAccount;
import com.giantdwarf.study.StudyControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest extends StudyControllerTest {

    @Autowired
    EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;

    @Test
    @WithAccount("yang")
    void 선착순모임_참가신청_자동수락() throws Exception {
        Account admin = createAccount("admin");
        Study study = createStudy("test-study", admin);
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
        Account admin = createAccount("admin");
        Study study = createStudy("test-study", admin);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, admin);

        Account may = createAccount("may");
        Account june = createAccount("june");
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
        Account admin = createAccount("admin");
        Account may = createAccount("may");
        Study study = createStudy("test-study", admin);
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
        Account admin = createAccount("admin");
        Account may = createAccount("may");
        Study study = createStudy("test-study", admin);
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
        Account admin = createAccount("admin");
        Study study = createStudy("test-study", admin);
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