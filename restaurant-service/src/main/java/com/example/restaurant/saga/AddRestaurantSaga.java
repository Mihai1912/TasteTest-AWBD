package com.example.restaurant.saga;

import com.example.restaurant.entity.Restaurant;
import com.example.restaurant.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class AddRestaurantSaga {

    private static final Logger log = LoggerFactory.getLogger(AddRestaurantSaga.class);

    private final RestaurantRepository restaurantRepository;
    private final NotificationClient notificationClient;
    private final TransactionTemplate stepTx;

    public AddRestaurantSaga(RestaurantRepository restaurantRepository,
                             NotificationClient notificationClient,
                             PlatformTransactionManager txManager) {
        this.restaurantRepository = restaurantRepository;
        this.notificationClient = notificationClient;
        this.stepTx = new TransactionTemplate(txManager);
        this.stepTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public Restaurant execute(Restaurant restaurant) {
        List<Runnable> compensations = new ArrayList<>();
        try {
            Restaurant saved = stepPersistRestaurant(restaurant);
            compensations.add(() -> compensateDeleteRestaurant(saved.getId()));

            stepNotifyOwner(saved);

            log.info("[SAGA] AddRestaurant completed id={}", saved.getId());
            return saved;
        } catch (Exception ex) {
            log.error("[SAGA] AddRestaurant failed: {} - running {} compensation(s)",
                    ex.getMessage(), compensations.size());
            for (int i = compensations.size() - 1; i >= 0; i--) {
                try {
                    compensations.get(i).run();
                } catch (Exception compEx) {
                    log.error("[SAGA] compensation step failed", compEx);
                }
            }
            throw new SagaFailedException("AddRestaurant saga failed: " + ex.getMessage(), ex);
        }
    }

    private Restaurant stepPersistRestaurant(Restaurant restaurant) {
        Restaurant saved = Objects.requireNonNull(
                stepTx.execute(status -> restaurantRepository.save(restaurant)));
        log.info("[SAGA] step persistRestaurant ok id={}", Objects.requireNonNull(saved.getId()));
        return saved;
    }

    private void stepNotifyOwner(Restaurant restaurant) {
        notificationClient.send(
                "Restaurant created",
                "Restaurant '" + restaurant.getName() + "' is now live."
        );
        log.info("[SAGA] step notifyOwner ok");
    }

    private void compensateDeleteRestaurant(UUID id) {
        stepTx.executeWithoutResult(status -> restaurantRepository.deleteById(id));
        log.warn("[SAGA] compensation deleteRestaurant id={}", id);
    }

    public static class SagaFailedException extends RuntimeException {
        public SagaFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
