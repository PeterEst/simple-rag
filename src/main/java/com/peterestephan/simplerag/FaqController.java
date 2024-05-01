package com.peterestephan.simplerag;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/faq")
public class FaqController {
    private static final String ragPromptTemplate = """
            You are a helpful assistant, conversing with a user about the subjects contained in a set of documents.
            Use the information from the DOCUMENTS section to provide accurate answers. If unsure or if the answer
            isn't found in the DOCUMENTS section, simply state that you don't know the answer.
                       
            QUESTION:
            {input}
                       
            DOCUMENTS:
            {documents}
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public FaqController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @PostMapping
    public ResponseEntity<String> faq(
            @RequestBody String message
    ) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(2));
        List<String> contentList = similarDocuments.stream().map(Document::getContent).toList();

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);

        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", message);
        promptParameters.put("documents", String.join("\n", contentList));

        Prompt prompt = promptTemplate.create(promptParameters);

        String content = chatClient.call(prompt).getResult().getOutput().getContent();
        return ResponseEntity.ok(content);
    }
}
