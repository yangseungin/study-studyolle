package com.giantdwarf.modules.study.event;

import com.giantdwarf.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyCreatedEvent {

    private final Study study;

}
