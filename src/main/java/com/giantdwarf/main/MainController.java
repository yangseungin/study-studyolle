package com.giantdwarf.main;

import com.giantdwarf.account.CurrentUser;
import com.giantdwarf.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if(!Objects.isNull(account)){
            model.addAttribute(account);
        }

        return "index";
    }
}
