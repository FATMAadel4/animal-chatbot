package com.example.chatbot.Config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;

@Configuration
public class AiConfig {

    @Value("${openai.api-key}")
    private String apikey;

    @Bean
    public ChatLanguageModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apikey)
                .modelName("gpt-4o-mini")
                .temperature(0.2)
                .maxTokens(300)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apikey)
                .modelName("text-embedding-3-small")
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(@Value("${chroma.url}") String chromaUrl) {
        return ChromaEmbeddingStore.builder()
                .baseUrl(chromaUrl)
                .collectionName("animals-db")
                .timeout(java.time.Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}