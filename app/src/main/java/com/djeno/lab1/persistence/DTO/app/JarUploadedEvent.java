package com.djeno.lab1.persistence.DTO.app;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "name",
        "email",
        "appId",
        "bucket"
})
public class JarUploadedEvent {
    public String name;
    public String email;
    public String appId;
    public String bucket;
}