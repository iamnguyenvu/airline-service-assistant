package io.github.nguyenvu.backend.policy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyCrawlerService {

    private final PolicyIngestionService ingestionService;

    @Value("${app.policy.crawler.vna.allowlist:}")
    private String vnaAllowlist;

    @Value("${app.policy.crawler.timeout-ms:30000}")
    private int timeoutMs;

    public int crawlVna(String docType) {
        if (vnaAllowlist == null || vnaAllowlist.isBlank()) {
            log.warn("VNA allowlist is empty, skipping crawl");
            return 0;
        }
        List<String> urls = Arrays.stream(vnaAllowlist.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        if (urls.isEmpty()) {
            log.warn("No valid URLs in VNA allowlist");
            return 0;
        }
        log.info("Starting VNA crawl for docType={}, urls={}", docType, urls.size());
        int ingested = 0;
        int failed = 0;
        for (String url : urls) {
            try {
                Document html = Jsoup.connect(url)
                        .timeout(timeoutMs)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .followRedirects(true)
                        .get();
                String text = extractMainText(html);
                if (text == null || text.isBlank()) {
                    log.warn("Extracted text is empty for url={}", url);
                    failed++;
                    continue;
                }
                Map<String, Object> metadata = extractMetadata(html, url, docType);
                ingestionService.ingestRawTest(text, metadata);
                ingested++;
                log.debug("Successfully crawled and ingested url={}, textLength={}", url, text.length());
            } catch (IOException e) {
                log.error("Failed to crawl url={}: {}", url, e.getMessage());
                failed++;
            } catch (Exception e) {
                log.error("Unexpected error crawling url={}: {}", url, e.getMessage(), e);
                failed++;
            }
        }
        log.info("VNA crawl completed: ingested={}, failed={}, total={}", ingested, failed, urls.size());
        return ingested;
    }

    private String extractMainText(Document doc) {
        Elements main = doc.select("main, article, #content, .content, .main-content, .post-content");
        String text;
        if (!main.isEmpty()) {
            text = main.first().text();
        } else {
            Elements body = doc.select("body");
            if (!body.isEmpty()) {
                body.select("script, style, nav, footer, header, aside").remove();
                text = body.first().text();
            } else {
                text = doc.text();
            }
        }
        if (text != null) {
            text = text.replaceAll("\\s+", " ").trim();
        }
        return text;
    }

    private Map<String, Object> extractMetadata(Document doc, String url, String docType) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("airline_code", "VN");
        metadata.put("doc_type", docType != null ? docType : "policy");
        metadata.put("source", url);
        
        String title = doc.title();
        if (title != null && !title.isBlank()) {
            metadata.put("title", title);
        }
        
        Elements metaTitle = doc.select("meta[property=og:title], meta[name=title]");
        if (!metaTitle.isEmpty()) {
            String ogTitle = metaTitle.first().attr("content");
            if (ogTitle != null && !ogTitle.isBlank()) {
                metadata.put("title", ogTitle);
            }
        }
        
        Elements metaDate = doc.select("meta[property=article:published_time], meta[name=date]");
        if (!metaDate.isEmpty()) {
            String dateStr = metaDate.first().attr("content");
            if (dateStr != null && !dateStr.isBlank()) {
                try {
                    LocalDate.parse(dateStr.substring(0, 10), DateTimeFormatter.ISO_DATE);
                    metadata.put("date", dateStr.substring(0, 10));
                } catch (Exception e) {
                    log.debug("Could not parse date from metadata: {}", dateStr);
                }
            }
        }
        
        metadata.put("version", "1");
        metadata.put("crawled_at", LocalDate.now().toString());
        
        return metadata;
    }
}


