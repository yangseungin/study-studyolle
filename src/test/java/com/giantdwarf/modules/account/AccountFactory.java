package com.giantdwarf.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account yang = new Account();
        yang.setNickname(nickname);
        yang.setEmail(nickname + "@email.com");
        accountRepository.save(yang);
        return yang;
    }
}
