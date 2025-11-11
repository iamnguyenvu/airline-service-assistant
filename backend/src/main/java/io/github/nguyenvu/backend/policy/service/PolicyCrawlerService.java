package io.github.nguyenvu.backend.policy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyCrawlerService {

    private final PolicyIngestionService ingestionService;

    @Value("${app.policy.crawler.vna.allowlist:}")
    private String vnaAllowlist;

    public int crawlVna(String docType) {
        List<String> urls = Arrays.stream(vnaAllowlist.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();
        int ingested = 0;
        for (String url : urls) {
            try {
                Document html = Jsoup.connect(url).get();
                String text = extractMainText(html);
                ingestionService.ingestRawTest(text, java.util.Map.of(
                        "airline_code", "VN",
                        "doc_type", docType != null ? docType : "policy",
                        "source", url
                ));
                ingested++;
            } catch (IOException e) {
                log.warn("Failed to crawl url={}: {}", url, e.getMessage());
            }
        }
        return ingested;
    }

    private String extractMainText(Document doc) {
        Elements main = doc.select("main, article, #content, .content");
        String text = main.isEmpty() ? doc.body().text() : main.text();
        return text.replaceAll("\\s+", " ").trim();
    }
}


