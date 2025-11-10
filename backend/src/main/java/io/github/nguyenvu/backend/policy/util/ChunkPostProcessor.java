package io.github.nguyenvu.backend.policy.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@AllArgsConstructor
@Slf4j
public class ChunkPostProcessor {
    private final int minWords;
    private final int maxMerges;
    private final ContextMatcher contextMatcher;

    public List<Document> mergeAndFilter(List<Document> chunks,
                                         Predicate<String> isGarbageLine) {
        List<Document> out = new ArrayList<>();
        int i = 0;
        while(i < chunks.size()) {
            Document doc = chunks.get(i);
            String text = safe(doc.getText());

            // Skip garbage lines
            if(isGarbageLine.test(text)) {
                i++;
                continue;
            }

            int merges = 0;
            int j = i;

            // Merge with next chunks if same content and below minWords
            while(wordCount(text) < minWords && (j + 1) < chunks.size() && merges < maxMerges) {
                Document next = chunks.get(j + 1);

                // Merge only if same page/section
                if(!contextMatcher.isSame(doc.getMetadata(), next.getMetadata())) break;

                String nextText = safe(next.getText());
                if(isGarbageLine.test(nextText)) {

                    j++;
                    continue;
                }

                // Merge
                text = text.trim() + "\n\n" + next.getText().trim();
                Map<String, Object> mergedMeta = new HashMap<>(doc.getMetadata());
                mergedMeta.put("merge", true);
                doc = new Document(text, mergedMeta);

                j++;
                merges++;
            }

            if(wordCount(text) < minWords && isLikeGarbage(text)) {
                log.debug("Dropped short garbage chunk at index {}: '{}'", i, text);
                i = j + 1;
                continue;
            }
            out.add(doc);
            i = j + 1;
        }

        return out;
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

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
