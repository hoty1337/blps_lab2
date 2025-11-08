package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.EmailAlreadyExistsException;
import com.djeno.lab1.exceptions.UserNotFoundException;
import com.djeno.lab1.exceptions.UsernameAlreadyExistsException;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.repositories.UserRepository;
import com.djeno.lab1.persistence.wrapper.UserWrapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    @Value("${users.file.path}")
    private String usersFilePath;

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        return repository.save(user);
    }

    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        if (repository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User savedUser = repository.save(user);
        saveUsers();

        return savedUser;
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

    }

    /**
     * Получение пользователя по username или email пользователя
     *
     * @return пользователь
     */
    public User getByUsernameOrEmail(String identifier) {
        return repository.findByUsername(identifier)
                .orElseGet(() -> repository.findByEmail(identifier)
                        .orElseThrow(() -> new UserNotFoundException("Пользователь не найден")));
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsernameOrEmail;
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    public void saveUsers() {
        List<User> userList = repository.findAll();
        UserWrapper userWrapper = new UserWrapper(userList);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(UserWrapper.class, User.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(usersFilePath);

            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    System.err.println("Не удалось создать директорию: " + parentDir.getAbsolutePath());
                    return;
                }
            }
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) {
                    System.err.println("Не удалось создать файл: " + file.getAbsolutePath());
                    return;
                }
            }

            jaxbMarshaller.marshal(userWrapper, file);
            System.out.println("Пользователи сохранены в " + file.getAbsolutePath());

        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }
    }


    @Transactional
    public void touchLastLogin(String username, Duration throttle) {
        var now = LocalDateTime.now();
        var minAllowed = now.minus(throttle);
        repository.touchLastLogin(username, now, minAllowed);
    }
}
