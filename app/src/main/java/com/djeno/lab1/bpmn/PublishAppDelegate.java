package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.DTO.app.CreateAppRequest;
import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Category;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.repositories.AppRepository;
import com.djeno.lab1.persistence.repositories.CategoryRepository;
import com.djeno.lab1.services.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("publishAppDelegate")
@RequiredArgsConstructor
public class PublishAppDelegate implements JavaDelegate {
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final AppRepository appRepository;

    @Override
    @Transactional
    public void execute(DelegateExecution ex) {
        String username = (String) ex.getVariable("username");
        CreateAppRequest appData = (CreateAppRequest) ex.getVariable("appData");
        String fileId = (String) ex.getVariable("fileId");
        String iconId = (String) ex.getVariable("iconId");

        User owner = userService.getByUsername(username);

        List<Category> categories = new ArrayList<>();
        if (appData.getCategoryIds() != null && !appData.getCategoryIds().isEmpty()) {
            categories = categoryRepository.findAllById(appData.getCategoryIds());
        }

        App app = new App();
        app.setName(appData.getName());
        app.setDescription(appData.getDescription());
        app.setPrice(appData.getPrice());
        app.setOwner(owner);
        app.setIconId(iconId);
        app.setFileId(fileId);
        app.setCategories(categories);

        appRepository.save(app);
    }
}
