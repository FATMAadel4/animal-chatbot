package com.example.chatbot.Service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

@Service
public class TextAnimalService {

    private final ChatLanguageModel chatModel;

    public TextAnimalService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    public String extractAnimal(String question) {

        String prompt = """
                Extract only the animal name from this question.
                Return only one word in English.
                The question can be in Arabic or English.
                Question: %s
                """.formatted(question);

        return chatModel.generate(prompt).trim();
    }
}