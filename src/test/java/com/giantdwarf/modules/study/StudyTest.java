package com.giantdwarf.modules.study;

import com.giantdwarf.modules.account.Account;
import com.giantdwarf.modules.account.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StudyTest {
    Study study;
    Account account;
    UserAccount userAccount;

    @BeforeEach
    void beforeEach() {
        study = new Study();
        account = new Account();
        account.setNickname("keesun");
        account.setPassword("123");
        userAccount = new UserAccount(account);

    }

    @Test
    void 스터디공개_인원모집중_멤버나관리자가아니면_스터디가입가능() {
        study.setPublished(true);
        study.setRecruiting(true);

        assertTrue(study.isJoinable(userAccount));
    }

    @Test
    void 스터디공개_인원모집중_관리자는가입불가() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addManager(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @Test
    void 스터디공개_인원모집중_멤버는가입불가() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addMember(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @Test
    void 스터디비공개_or인원모집중아님_가입불가() {
        study.setPublished(true);
        study.setRecruiting(false);

        assertFalse(study.isJoinable(userAccount));

        study.setPublished(false);
        study.setRecruiting(true);

        assertFalse(study.isJoinable(userAccount));
    }

    @Test
    void 관리자인지확인() {
        study.addManager(account);
        assertTrue(study.isManager(userAccount));
    }


    @Test
    void 멤버인지확인() {
        study.addMember(account);
        assertTrue(study.isMember(userAccount));
    }
}
