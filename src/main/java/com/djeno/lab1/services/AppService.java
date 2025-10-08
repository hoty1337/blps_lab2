package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.*;
import com.djeno.lab1.persistence.DTO.app.AppDetailsDto;
import com.djeno.lab1.persistence.DTO.app.AppListDto;
import com.djeno.lab1.persistence.DTO.app.CreateAppRequest;
import com.djeno.lab1.persistence.DTO.review.ReviewDTO;
import com.djeno.lab1.persistence.enums.Role;
import com.djeno.lab1.persistence.models.*;
import com.djeno.lab1.persistence.repositories.AppRepository;
import com.djeno.lab1.persistence.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppService {

    private final AppRepository appRepository;
    private final CategoryRepository categoryRepository;
    private final MinioService minioService;
    private final UserService userService;
    private final PurchaseService purchaseService;

    public App getAppById(Long id) {
        return appRepository.findById(id)
                .orElseThrow(() -> new AppNotFoundException(id));
    }

    public void createApp(
            CreateAppRequest appData,
            MultipartFile icon,
            MultipartFile file,
            List<MultipartFile> screenshots) {

        User owner = userService.getCurrentUser();

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("APK файл не загружен");
        }

        // Загружаем файлы в MinIO
        String iconId = null;
        if (icon != null && !icon.isEmpty()) {
            try {
                iconId = minioService.uploadFile(icon, MinioService.ICONS_BUCKET);
            } catch (Exception e) {
                throw new FileUploadException("Не удалось загрузить иконку: " + e.getMessage());
            }
        }

        String fileId;
        try {
            fileId = minioService.uploadFile(file, MinioService.APK_BUCKET);
        } catch (Exception e) {
            throw new FileUploadException("Не удалось загрузить APK файл: " + e.getMessage());
        }

        List<String> screenshotsIds = new ArrayList<>();
        if (screenshots != null) {
            for (MultipartFile screenshot : screenshots) {
                if (!screenshot.isEmpty()) {
                    try {
                        screenshotsIds.add(minioService.uploadFile(screenshot, MinioService.SCREENSHOTS_BUCKET));
                    } catch (Exception e) {
                        throw new FileUploadException("Не удалось загрузить скриншоты: " + e.getMessage());
                    }
                }
            }
        }

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
        app.setScreenshotsIds(screenshotsIds);
        app.setCategories(categories);

        appRepository.save(app);
    }

    public Page<AppListDto> getApps(Long categoryId, Pageable pageable) {
        Page<App> appsPage;

        if (categoryId != null) {
            appsPage = appRepository.findByCategories_Id(categoryId, pageable);
        } else {
            appsPage = appRepository.findAll(pageable);
        }

        return appsPage.map(this::convertToAppListDto);
    }

    public Page<AppListDto> getPurchasedApps(User user, Pageable pageable) {
        Page<Purchase> purchasesPage = purchaseService.getPurchasesByUser(user, pageable);
        return purchasesPage.map(purchase ->
                convertToAppListDto(purchase.getApp()));
    }

    public AppDetailsDto getAppDetails(Long id) {
        App app = getAppById(id);

        return convertToAppDetailsDto(app);
    }

    public void deleteApp(Long id) {
        User currentUser = userService.getCurrentUser();
        App app = getAppById(id);

        if (!app.getOwner().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new NotEnoughPrivileges("Недостаточно прав для удаления приложения");
        }

        if (app.getIconId() != null) {
            try {
                minioService.deleteFile(app.getIconId(), MinioService.ICONS_BUCKET);
            } catch (Exception e) {
                log.warn("Не удалось удалить иконку приложения из Minio: {}", e.getMessage());
            }
        }

        if (app.getFileId() != null) {
            try {
                minioService.deleteFile(app.getFileId(), MinioService.APK_BUCKET);
            } catch (Exception e) {
                log.warn("Не удалось удалить APK файл из Minio: {}", e.getMessage());
            }
        }

        if (app.getScreenshotsIds() != null) {
            for (String screenId : app.getScreenshotsIds()) {
                try {
                    minioService.deleteFile(screenId, MinioService.SCREENSHOTS_BUCKET);
                } catch (Exception e) {
                    log.warn("Не удалось удалить скриншот из Minio: {}", e.getMessage());
                }
            }
        }

        appRepository.delete(app);
    }

    public ResponseEntity<?> downloadApp(Long id) {
        User currentUser = userService.getCurrentUser();
        App app = getAppById(id);

        if (!isFreeOrOwnedOrPurchased(app, currentUser)) {
            throw new PaymentRequiredException("Для скачивания необходимо приобрести приложение");
        }

        app.setDownloads(app.getDownloads() + 1);
        appRepository.save(app);

        return buildDownloadResponse(app);

    }

    private boolean isFreeOrOwnedOrPurchased(App app, User user) {
        return isFreeApp(app) || isAppOwner(app, user) || isAppPurchasedByUser(app, user);
    }

    private boolean isFreeApp(App app) {
        return app.getPrice().compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean isAppOwner(App app, User user) {
        return app.getOwner().getId().equals(user.getId());
    }

    private boolean isAppPurchasedByUser(App app, User user) {
        return purchaseService.hasUserPurchasedApp(user, app);
    }

    private ResponseEntity<?> buildDownloadResponse(App app) {
        String downloadUrl = minioService.buildPublicUrl(app.getFileId(), MinioService.APK_BUCKET);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + app.getName() + ".apk\"")
                .body(downloadUrl);
    }

    private AppDetailsDto convertToAppDetailsDto(App app) {
        return AppDetailsDto.builder()
                .id(app.getId())
                .name(app.getName())
                .description(app.getDescription())
                .iconUrl(app.getIconId() != null ?
                                minioService.buildPublicUrl(app.getIconId(), MinioService.ICONS_BUCKET) : null)
                .screenshotUrls(app.getScreenshotsIds() == null || app.getScreenshotsIds().isEmpty() ?
                        Collections.emptyList() :
                        app.getScreenshotsIds().stream()
                                .filter(Objects::nonNull)
                                .map(id -> minioService.buildPublicUrl(id, MinioService.SCREENSHOTS_BUCKET))
                                .collect(Collectors.toList()))
                .price(app.getPrice())
                .averageRating(app.getAverageRating())
                .downloads(app.getDownloads())
                .createdAt(app.getCreatedAt())
                .ownerUsername(app.getOwner().getUsername())
                .categories(app.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toList()))
                .reviews(convertReviewsToDtos(app.getReviews()))
                .build();
    }

    private List<ReviewDTO> convertReviewsToDtos(List<Review> reviews) {
        if (reviews == null) {
            return Collections.emptyList();
        }

        return reviews.stream()
                .map(review -> ReviewDTO.builder()
                        .id(review.getId())
                        .userUsername(review.getUser().getUsername())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private AppListDto convertToAppListDto(App app) {
        return AppListDto.builder()
                .id(app.getId())
                .name(app.getName())
                .iconUrl(app.getIconId() != null ?
                        minioService.buildPublicUrl(app.getIconId(), MinioService.ICONS_BUCKET) : null)
                .price(app.getPrice())
                .averageRating(app.getAverageRating())
                .downloads(app.getDownloads())
                .build();
    }
}
