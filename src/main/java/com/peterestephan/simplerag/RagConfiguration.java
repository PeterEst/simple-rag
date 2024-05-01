package com.peterestephan.simplerag;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class RagConfiguration {
    @Value("classpath:/docs/real-madrid-faq.txt")
    private Resource faq;

    private static final String VECTOR_STORE_FILE_NAME = "vector-store.json";

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingClient embeddingClient) {
        SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embeddingClient);

        File vectorStoreFile = getVectorStoreFile();
        if (vectorStoreFile.exists()) {
            simpleVectorStore.load(vectorStoreFile);
        } else {
            List<Document> documents = retrieveVectorStoreDocuments();
            simpleVectorStore.add(documents);
            simpleVectorStore.save(vectorStoreFile);
        }

        return simpleVectorStore;
    }

    private File getVectorStoreFile() {
        Path path = Paths.get("src", "main", "resources", "data");
        String absolutePath = path.toFile().getAbsoluteFile() + "/" + VECTOR_STORE_FILE_NAME;
        return new File(absolutePath);
    }

    private List<Document> retrieveVectorStoreDocuments() {
        TextReader textReader = new TextReader(faq);
        textReader.getCustomMetadata().put("filename", faq.getFilename());
        List<Document> documents = textReader.get();
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        return textSplitter.apply(documents);
    }
}
