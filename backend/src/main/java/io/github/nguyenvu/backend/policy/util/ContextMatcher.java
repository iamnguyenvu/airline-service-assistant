package io.github.nguyenvu.backend.policy.util;

import java.util.Map;

@FunctionalInterface
public interface ContextMatcher {
    boolean isSame(Map<String, Object> meta1, Map<String, Object> meta2);
}
