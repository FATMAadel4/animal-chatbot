package com.example.chatbot.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AnimalChatFacade {

    private final VisionService visionService;
    private final TextAnimalService textService;
    private final AnimalChatService ragService;

    public AnimalChatFacade(VisionService visionService,
                            TextAnimalService textService,
                            AnimalChatService ragService) {
        this.visionService = visionService;
        this.textService = textService;
        this.ragService = ragService;
    }

    public String handle(String question, MultipartFile image) throws Exception {

        if (image != null && !image.isEmpty()) {

            // حدد اللغة من السؤال
            boolean isArabic = false;
            String language = "English";
            if (question != null && !question.isBlank()) {
                isArabic = question.matches(".*[\\u0600-\\u06FF].*");
                language = isArabic ? "Arabic" : "English";
            }

            VisionService.AnimalAnalysis analysis =
                    visionService.analyzeAnimal(image.getBytes(), language);

            if (question != null && !question.isBlank()) {
                String ragAnswer = ragService.askWithContext(analysis.animalName(), question);

                //  الـ labels حسب اللغة
                if (isArabic) {
                    return "🔍 تحليل الصورة:\n" + analysis.description() +
                            "\n\nإجابة سؤالك:\n" + ragAnswer;
                } else {
                    return "🔍 Image Analysis:\n" + analysis.description() +
                            "\n\nAnswer:\n" + ragAnswer;
                }
            }

            //  صورة بس بدون سؤال - default انجليزي
            String ragInfo = ragService.askAboutAnimal(analysis.animalName(), null);
            return "🔍 Image Analysis:\n" + analysis.description() +
                    "\n\nAbout " + analysis.animalName() + ":\n" + ragInfo;
        }

        //  نص فقط
        if (question != null && !question.isBlank()) {
            String animal = textService.extractAnimal(question);
            return ragService.askAboutAnimal(animal, question);
        }

        return "من فضلك أرسل سؤالاً أو صورة. / Please provide a question or an image.";
    }
}