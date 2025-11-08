package io.github.nguyenvu.backend.policy.util;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ChunkPostProcessor {
    private final int minWords;
    private final int maxMerges;

    public List<Document> mergeAndFilter(List<Document> chunks,
                                         Predicate<String> isGarbageLine) {
        List<Document> out = new ArrayList<>();
        int i = 0;
        while(i++ < chunks.size()) {
            Document doc = chunks.get(i);
            String text = doc.getFormattedContent();

            // Skip garbage lines
            if(isGarbageLine.test(text)) continue;

            int merges = 0;
            // Merge with next chunks if same content and below minWords
            while(wordCount(text) < minWords && (i + 1) < chunks.size() && merges++ < maxMerges) {
                Document next = chunks.get(i + 1);

                // Merge only if same page/section
                if(!sameContext(doc.getMetadata(), next.getMetadata())) break;

                text = text.trim() + "\n\n" + next.getFormattedContent().trim();
                Map<String, Object> mergedMeta = new HashMap<>();
                mergedMeta.put("merge", true);
                doc = new Document(text, mergedMeta);
            }

            if(wordCount(text) < minWords && isLikeGarbage(text)) continue;
            else out.add(doc);
        }

        return out;
    }

    private static boolean sameContext(Map<String, Object> m1, Map<String, Object> m2) {
        for(String key: List.of("page", "section")) {
            Object v1 = m1.get(key);
            Object v2 = m2.get(key);
            if(v1 == null && v2 == null) continue;
            if(v1 == null || v2 == null) return false;
            if(!v1.equals(v2)) return false;
        }
        return true;
    }

    private static int wordCount(String s) {
        String t = s.trim();
        if (t.isEmpty()) return 0;
        return t.split("\\s+").length;
    }

    private static boolean isLikeGarbage(String s) {
        String t = s.trim();
        // Too short and meaningless
        if (t.length() <= 4) return true;
        if (t.matches("(?i)^(page|trang)\\s*\\d+$")) return true;
        if (t.matches("^[\\p{Punct}\\d\\s]{1,10}$")) return true; // only punctuation and digits
        return false;
    }
}
