package com.example.chatbot.Service;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class VisionService {

    private final ChatLanguageModel chatModel;

    public VisionService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    public record AnimalAnalysis(String animalName, String description) {}

    // ✅ بيستقبل اللغة دلوقتي
    public AnimalAnalysis analyzeAnimal(byte[] imageBytes, String language) {

        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        UserMessage message = UserMessage.from(
                ImageContent.from(base64, "image/jpeg", ImageContent.DetailLevel.LOW),
                dev.langchain4j.data.message.TextContent.from("""
                        Look at this animal image and answer in this exact format:
                        NAME: [animal name in one word in English]
                        DESCRIPTION: [Describe the animal: gender, estimated age, health condition, appearance]
                        Answer the DESCRIPTION in %s.
                        """.formatted(language))
        );

        String response = chatModel.generate(message).content().text().trim();

        String name = "unknown";
        String description = response;

        for (String line : response.split("\n")) {
            if (line.startsWith("NAME:")) {
                name = line.replace("NAME:", "").trim().toLowerCase();
            }
            if (line.startsWith("DESCRIPTION:")) {
                description = line.replace("DESCRIPTION:", "").trim();
            }
        }

        return new AnimalAnalysis(name, description);
    }

}