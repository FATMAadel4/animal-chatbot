package com.example.chatbot.Service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnimalChatService {

    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private ContentRetriever contentRetriever;

    public AnimalChatService(ChatLanguageModel chatModel,
                             EmbeddingModel embeddingModel,
                             EmbeddingStore<TextSegment> embeddingStore) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    @PostConstruct
    public void init() {
        try {
            // 1. بناء الـ Retriever أولاً لضمان عدم بقاء الكائن null في حال فشل الاتصال بقاعدة البيانات
            this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(2)
                    .build();

            ClassPathResource resource = new ClassPathResource("data/Animal_Data.txt");
            Path path = resource.getFile().toPath();
            Document document = FileSystemDocumentLoader.loadDocument(path, new TextDocumentParser());

            List<TextSegment> segments = DocumentSplitters.recursive(1000, 20).split(document);

            // 2. تنظيف النصوص وفلترة العربي والمسافات الزائدة
            List<TextSegment> cleanedSegments = segments.stream()
                    .map(segment -> segment.text()
                            .replaceAll("(?m)^.*[\\u0600-\\u06FF].*$", "")
                            .replaceAll("[\\u0600-\\u06FF]+", "")
                            .replace("\n", " ").replace("\r", " ")
                            .replaceAll("\\s{2,}", " ").trim())
                    .filter(text -> !text.isBlank() && text.length() > 5)
                    .map(TextSegment::from)
                    .distinct()
                    .collect(Collectors.toList());

            // 3. الإضافة التدريجية لتجنب الـ Timeout
            if (!cleanedSegments.isEmpty()) {
                for (TextSegment segment : cleanedSegments) {
                    try {
                        embeddingStore.add(embeddingModel.embed(segment).content(), segment);
                    } catch (Exception e) {
                        System.err.println("Error adding segment: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Init failed: " + e.getMessage());
        }
    }

    public String askAboutAnimal(String animal, String userQuestion) {
        if (contentRetriever == null) return "البحث غير متاح حالياً.";

        String inputText = userQuestion != null && !userQuestion.isBlank() ? userQuestion : animal;

        Query query = Query.from(inputText);
        List<Content> contents = contentRetriever.retrieve(query);

        if (contents.isEmpty()) return "لا توجد معلومات متوفرة عن " + animal;

        String context = contents.stream()
                .map(c -> c.textSegment().text())
                .distinct()
                .collect(Collectors.joining(" "))
                .replaceAll("\\s{2,}", " ")
                .trim();

        // كشف لغة السؤال بالكامل
        boolean isArabic = inputText.matches(".*[\\u0600-\\u06FF].*");
        String targetLanguage = isArabic ? "Arabic" : "English";

        String prompt = """
        Context: %s
        
        Task: Provide a detailed summary about (%s).
        
        STRICT INSTRUCTIONS:
        1. You MUST answer in %s.
        2. Translate context facts to %s if necessary.
        3. Use ONLY the provided context. Do not add any outside information.
        4. Do not use special symbols like \\n.
        """.formatted(context, animal, targetLanguage, targetLanguage);

        String response = chatModel.generate(prompt);
        return cleanFinalResponse(response);
    }

    public String askWithContext(String animal, String question) {
        if (contentRetriever == null) return "البحث غير متاح حالياً.";

        String inputText = animal + " " + question;

        Query query = Query.from(inputText);
        List<Content> contents = contentRetriever.retrieve(query);

        if (contents.isEmpty()) return "لا توجد معلومات متوفرة.";

        String contextText = contents.stream()
                .map(c -> c.textSegment().text())
                .distinct()
                .collect(Collectors.joining(" "))
                .replaceAll("\\s{2,}", " ")
                .trim();

        boolean isArabic = question.matches(".*[\\u0600-\\u06FF].*");
        String targetLanguage = isArabic ? "Arabic" : "English";

        String prompt = """
        Context: %s
        Question: %s

        Instruction: You MUST answer in %s.
        Translate all context facts to %s if needed.
        Use ONLY the provided context. Do not add external information.
        """.formatted(contextText, question, targetLanguage, targetLanguage);

        String response = chatModel.generate(prompt);
        return cleanFinalResponse(response);
    }
    // دالة موحدة لتنظيف الرد النهائي من أي رموز مزعجة
    private String cleanFinalResponse(String text) {
        if (text == null) return "";
        return text.replace("\\n", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
}