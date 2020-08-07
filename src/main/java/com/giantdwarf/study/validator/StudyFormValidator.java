package com.giantdwarf.study.validator;

import com.giantdwarf.study.StudyRepository;
import com.giantdwarf.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepository studyRepository;
    @Override
    public boolean supports(Class<?> clazz) {
        return StudyForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;
        if(studyRepository.existsByPath(studyForm.getPath())) {
            errors.rejectValue("path", "wrong.path", "입력하신 스터디 경로를 사용할 수 없습니다.");
        }
    }
}
