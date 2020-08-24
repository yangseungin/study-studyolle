package com.giantdwarf.modules.main;

import com.giantdwarf.modules.account.Account;
import com.giantdwarf.modules.account.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest req, RuntimeException e) {
        if (!Objects.isNull(account)) {
            log.info("{} requested '{}'", account.getNickname(), req.getRequestURI());
        } else {
            log.info("requested '{}'", req.getRequestURI());
        }
        log.error("bad request", e);
        return "error";
    }
}
