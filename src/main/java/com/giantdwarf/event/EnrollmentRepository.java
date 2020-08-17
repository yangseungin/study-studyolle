package com.giantdwarf.event;

import com.giantdwarf.domain.Account;
import com.giantdwarf.domain.Enrollment;
import com.giantdwarf.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
