package com.lrm;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException
{
    public NotFoundException() {
        super();
    }

    public NotFoundException(String s) {
        super(s);
    }

    public NotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
