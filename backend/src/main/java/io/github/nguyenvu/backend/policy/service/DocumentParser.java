package io.github.nguyenvu.backend.policy.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DocumentParser {
    private final Tika tika = new Tika();

    public String parserBytes(InputStream inputStream) throws Exception{
        var handler = new BodyContentHandler(-1);
        var metadata = new Metadata();
        var parser = new AutoDetectParser();
        var context = new ParseContext();

        parser.parse(inputStream, handler, metadata, context);
        return normalizeText(handler.toString());
    }

    public String parserUrl(String url) throws Exception{
        // if the url ends with .html or starts with http, use Jsoup to parse
        // else download the file and use Tika to parse
        if(url.toLowerCase().endsWith(".html") || url.toLowerCase().startsWith("http")) {
            var doc = Jsoup.connect(url).get();
            doc.select("script, style, nav, header, footer").remove();
            StringBuilder stringBuilder = new StringBuilder();
            for(Element e: doc.body().select("*")) {
                e.ownText();
                if(!e.ownText().isBlank()) {
                    stringBuilder.append(e.ownText()).append("\n");
                }
            }
            return normalizeText(stringBuilder.toString());
        }

        try (var input = URI.create(url).toURL().openStream()) {
            return parserBytes(input);
        }
    }

    public String parseHtmlString(String html) {
        var doc = Jsoup.parse(html);
        doc.select("script, style, nav, header, footer").remove();
        String text = doc.text();
        return normalizeText(text);
    }

    public String normalizeText(String text) {
        if(text == null) {
            return "";
        }
        // replace multiple spaces with single space
        String normalized = new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        normalized = normalized.replaceAll("\\r\\n?", "\n");
        normalized = normalized.replaceAll("\\n{2,}", "\n");
        normalized = normalized.replaceAll("[ \\t]{2,}", " ");
        return normalized.trim();
    }
}
