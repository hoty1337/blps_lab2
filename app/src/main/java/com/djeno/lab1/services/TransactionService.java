package com.djeno.lab1.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final JtaTransactionManager jtaTransactionManager;

    public <T> T execute(String txName, int timeout, TransactionCallback<T> action) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(txName);
        def.setTimeout(timeout);
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        return execute(def, action);
    }

    public <T> T execute(DefaultTransactionDefinition def, final TransactionCallback<T> action) {
        TransactionStatus status = jtaTransactionManager.getTransaction(def);
        String txName = def.getName();
        log.info("STARTED Transaction: {} | Timeout: {}", txName, def.getTimeout());
        try {
            T res = action.doInTransaction(status);
            jtaTransactionManager.commit(status);
            log.info("COMMITTED Transaction: {} | Timeout: {}", txName, def.getTimeout());
            return res;
        } catch (Exception e) {
            if (!status.isCompleted()) {
                jtaTransactionManager.rollback(status);
                log.warn("ROLLBACK Transaction: {} | Reason: {}", txName, e.getMessage());
            }
            throw e;
        }
    }
}
