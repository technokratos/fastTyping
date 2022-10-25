package com.training.apparatus.data.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Kulikov Denis
 * @since 24.10.2022
 */
@Slf4j
@Service
public class EncodingService {

    private final ThreadLocal<MessageDigest> digestThreadLocal = new InheritableThreadLocal<>() {

        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance(
                        "SHA-256"
                );
            } catch (NoSuchAlgorithmException e) {
                log.error("Error in initiating SHA-256", e);
                throw new IllegalStateException(e);
            }
        }
    };

    public String encodingRestoreEmail(String email, Long id) {
        String text = email + id.toString();
        return encoding(text);
    }

    public String encoding(String text) {

        byte[] hash = digestThreadLocal.get().digest(text.getBytes(StandardCharsets.UTF_8));
        byte[] encoded = Base64.getEncoder().encode(hash);
        return new String(encoded);
    }

    public boolean checkHashRestoreEmail(String hash, String email, Long id) {
        String expected = encodingRestoreEmail(email, id);
        return expected.contains(hash);
    }

    public String encodingGroup(long groupId, long managerId) {
        return encoding(Long.toString(groupId) + managerId);
    }
    public boolean checkedGroupHash(String hash, long groupId, long managerId) {
        String expected = encodingGroup(groupId, managerId);
        return expected.contains(hash);
    }
}
