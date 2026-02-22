package com.example.chatbot.Controller;

import com.example.chatbot.Service.AnimalChatFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    private final AnimalChatFacade facade;

    public ChatController(AnimalChatFacade facade) {
        this.facade = facade;
    }

    // Endpoint للصورة + سؤال
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    @Operation(summary = "Upload an image with optional question")
    public Map<String, String> chatWithImage(
            @RequestParam(value = "question", required = false)
            String question,
            @RequestParam("image")
            MultipartFile image
    ) throws Exception {
        String answer = facade.handle(question, image);
        return Map.of("answer", answer);
    }

    // Endpoint للسؤال النصي فقط
    @PostMapping(
            value = "/text",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    @Operation(summary = "Send a text question only")
    public Map<String, String> chatWithText(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON body containing the question",
                    required = true,
                    content = @Content(schema = @Schema(example = "{\"question\": \"What animal is this?\"}"))
            )
            @RequestBody Map<String, String> body
    ) throws Exception {
        String question = body.get("question");
        String answer = facade.handle(question, null);
        return Map.of("answer", answer);
    }
}