package com.djeno.lab1.bpmn;

import com.djeno.lab1.persistence.DTO.app.CreateAppRequest;
import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.repositories.AppRepository;
import com.djeno.lab1.persistence.repositories.CategoryRepository;
import com.djeno.lab1.services.MinioService;
import com.djeno.lab1.services.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersistAppDelegate implements JavaDelegate {
    private final AppRepository appRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Transactional
    @Override
    public void execute(DelegateExecution ex) {
        var req = (CreateAppRequest) ex.getVariable("appData");
        var owner = userService.getCurrentUser();
        var app = new App();
        app.setName(req.getName());
        app.setDescription(req.getDescription());
        app.setPrice(req.getPrice());
        app.setOwner(owner);
        app.setIconId((String)ex.getVariable("iconId"));
        app.setFileId((String)ex.getVariable("fileId"));
        if (req.getCategoryIds()!=null && !req.getCategoryIds().isEmpty()) {
            app.setCategories(categoryRepository.findAllById(req.getCategoryIds()));
        }
        appRepository.save(app);
        ex.setVariable("appDbId", app.getId());
    }
}
