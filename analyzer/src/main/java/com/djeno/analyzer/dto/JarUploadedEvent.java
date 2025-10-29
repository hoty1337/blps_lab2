package com.djeno.analyzer.dto;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

@JsonbPropertyOrder({
        "name","email","appId","bucket"
})
public class JarUploadedEvent {
    public String name;
    public String email;
    public String appId;
    public String bucket;
}