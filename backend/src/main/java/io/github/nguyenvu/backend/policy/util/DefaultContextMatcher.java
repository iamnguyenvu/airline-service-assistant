package io.github.nguyenvu.backend.policy.util;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DefaultContextMatcher implements ContextMatcher {
    private final List<String> keys;

    @Override
    public boolean isSame(Map<String, Object> meta1, Map<String, Object> meta2) {
        for(String key : keys) {
            Object val1 = meta1.get(key);
            Object val2 = meta2.get(key);
            if(val1 == null && val2 == null) continue;
            if(val1 == null || val2 == null) return false;
            if(!val1.equals(val2)) return false;
        }
        return true;
    }
}
