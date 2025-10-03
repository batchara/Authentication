package com.raoudate.Authentification.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("activate_acount")

    ;
    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
