package com.djeno.lab1.persistence.enums;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import lombok.Getter;

import java.util.Set;

@Getter
@XmlEnum
public enum Role {
    @XmlEnumValue("ROLE_USER")
    ROLE_USER(Set.of(
            Privilege.DOWNLOAD_APP,
            Privilege.PURCHASE_APP,
            Privilege.VIEW_PURCHASED_APP_LIST,
            Privilege.CREATE_REVIEW,
            Privilege.DELETE_REVIEW,
            Privilege.ADD_PAYMENT_METHOD,
            Privilege.VIEW_PAYMENT_METHOD,
            Privilege.DELETE_PAYMENT_METHOD,
            Privilege.SET_PRIMARY_PAYMENT_METHOD
    )),

    @XmlEnumValue("ROLE_DEVELOPER")
    ROLE_DEVELOPER(Set.of(
            Privilege.PUBLISH_APP,
            Privilege.DELETE_APP,
            Privilege.DOWNLOAD_APP
    )),

    @XmlEnumValue("ROLE_ADMIN")
    ROLE_ADMIN(Set.of(
            Privilege.DOWNLOAD_APP,
            Privilege.DELETE_APP,
            Privilege.DELETE_REVIEW
    ));

    private final Set<Privilege> privileges;

    Role(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

}
