package com.example.youtube_recommender.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embeddings Controller (Ollama backend via Spring AI).
 * Optimized for YouTube Data Ingestion.
 */
@RestController
@RequestMapping(path = "/api/v1/embeddings", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmbeddingsController {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingsController.class);
    private final EmbeddingModel embeddingModel;

    // ---- Tunables -----------------------------------------------------------------------------
    private static final int MAX_BATCH = 64;
    private static final int MAX_CHARS_PER_TEXT = 8_000;
    private static final int PREVIEW_DIMS = 8;

    public EmbeddingsController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    // ---- GET: Preview -------------------------------------------------------------------------
    @GetMapping
    public Map<String, Object> embedPreview(@RequestParam("text") String text) {
        final long t0 = System.nanoTime();

        // 1. Prepare text
        String cleaned = sanitizeForRag(text);
        cleaned = enforceMaxChars(cleaned, MAX_CHARS_PER_TEXT);

        // 2. Embed
        float[] vec = embeddingModel.embed(cleaned);

        // 3. Slice for preview
        int dims = Math.min(PREVIEW_DIMS, vec.length);
        List<Float> head = new ArrayList<>(dims);
        for (int i = 0; i < dims; i++) {
            head.add(vec[i]);
        }

        long tookMs = (System.nanoTime() - t0) / 1_000_000L;

        Map<String, Object> response = new HashMap<>();
        response.put("dims", vec.length); // Return real full dimension info
        response.put("previewVector", head);
        response.put("tookMs", tookMs);
        return response;
    }

    // ---- POST: Batch --------------------------------------------------------------------------
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> embedBatch(@RequestBody Map<String, Object> requestBody) {
        final long t0 = System.nanoTime();

        // 1. Safe Extraction
        List<String> texts = extractTextsSafely(requestBody);
        Integer truncateTo = (Integer) requestBody.get("truncateTo");

        if (texts.isEmpty()) {
            throw badRequest("'texts' list must not be empty.");
        }
        if (texts.size() > MAX_BATCH) {
            throw badRequest("Too many texts. Max batch size is " + MAX_BATCH + ".");
        }

        final int maxChars = (truncateTo != null && truncateTo > 0)
                ? Math.min(truncateTo, MAX_CHARS_PER_TEXT)
                : MAX_CHARS_PER_TEXT;

        // 2. Pre-processing (Sanitize + Truncate)
        List<String> cleaned = new ArrayList<>(texts.size());
        for (String s : texts) {
            String c = sanitizeForRag(s);
            cleaned.add(enforceMaxChars(c, maxChars));
        }

        // 3. Call Model (The heavy lifting)
        log.info("Embedding batch of {} texts...", cleaned.size());
        EmbeddingResponse resp = embeddingModel.embedForResponse(cleaned);

        // 4. Map Results
        // Determine dimension from the first result if available
        int dimension = !resp.getResults().isEmpty()
                ? resp.getResults().getFirst().getOutput().length
                : 0;

        List<Map<String, Object>> embeddings = new ArrayList<>(resp.getResults().size());

        for (int i = 0; i < resp.getResults().size(); i++) {
            float[] outputArray = resp.getResults().get(i).getOutput();

            // Convert float[] to List<Float> for JSON serialization
            List<Float> vector = toFloatList(outputArray);

            Map<String, Object> embeddingItem = new HashMap<>();
            embeddingItem.put("index", i);
            embeddingItem.put("vector", vector);
            embeddings.add(embeddingItem);
        }

        long tookMs = (System.nanoTime() - t0) / 1_000_000L;
        log.info("Batch finished. Count: {}, Time: {}ms", embeddings.size(), tookMs);

        // 5. Build Final Response
        Map<String, Object> response = new HashMap<>();
        response.put("count", embeddings.size());
        response.put("dimension", dimension);
        response.put("tookMs", tookMs);
        response.put("createdAt", Instant.now().toString());
        response.put("embeddings", embeddings);

        return response;
    }

    // ---- Helpers ------------------------------------------------------------------------------

    private static ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    /**
     * Extracts and validates the 'texts' list from the map.
     */
    private List<String> extractTextsSafely(Map<String, Object> body) {
        Object textsObj = body.get("texts");
        if (textsObj == null) {
            throw badRequest("Body must contain 'texts' key.");
        }
        if (!(textsObj instanceof List<?> list)) {
            throw badRequest("'texts' must be a JSON array.");
        }

        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof String s) {
                result.add(s);
            } else {
                throw badRequest("All items in 'texts' must be strings.");
            }
        }
        return result;
    }

    /**
     * Sanitizes text specifically for RAG purposes.
     * Removes binary control characters but PRESERVES semantic whitespace (newlines, tabs).
     */
    private static String sanitizeForRag(String s) {
        if (!StringUtils.hasText(s)) {
            // Depending on use case, you might want to allow empty strings or throw error.
            // For YouTube embeddings, an empty description is useless, so we throw.
            throw badRequest("'text' must not be blank.");
        }

        // Remove control characters (ASCII 0-31) EXCEPT: \r, \n, \t.
        // We want to keep paragraphs structure for the LLM.
        String normalized = s.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        normalized = normalized.trim();

        if (!StringUtils.hasText(normalized)) {
            throw badRequest("'text' contains no meaningful content after cleaning.");
        }

        // Byte-size guardrail (Postgres limitation safe-guard)
        byte[] bytes = normalized.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 2_000_000) {
            throw badRequest("'text' payload is too large (>2MB).");
        }

        return normalized;
    }

    private static String enforceMaxChars(String s, int maxChars) {
        if (s.length() <= maxChars) return s;
        // Optimization: try to cut at a space if possible, but hard cut is safer for strictly maxChars
        return s.substring(0, maxChars);
    }

    private static List<Float> toFloatList(float[] array) {
        if (array == null) return List.of();
        List<Float> out = new ArrayList<>(array.length);
        for (float f : array) {
            out.add(f);
        }
        return out;
    }
}