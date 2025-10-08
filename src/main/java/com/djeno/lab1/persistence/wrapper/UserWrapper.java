package com.djeno.lab1.persistence.wrapper;

import com.djeno.lab1.persistence.models.User;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name="users")
public class UserWrapper {
    private List<User> userList;

    public UserWrapper(List<User> userList) {
        this.userList = userList;
    }

    @XmlElement(name="user")
    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}
