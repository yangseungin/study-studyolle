package com.giantdwarf.event;

import com.giantdwarf.domain.Account;
import com.giantdwarf.domain.Event;
import com.giantdwarf.domain.Study;
import com.giantdwarf.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreateDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm,event);
        //TODO 선착순 모임은 추가인원을 자동으로 수락으로 올려줄것
    }

    public void deleteEvent(Event event) {
         eventRepository.delete(event);
    }
}
