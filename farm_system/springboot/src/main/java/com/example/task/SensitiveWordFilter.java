package com.example.task;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@Component
public class SensitiveWordFilter {

    private final TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(this.getClass().getResourceAsStream("/static/sensitive_words.txt"))))) {
            String word;
            while ((word = reader.readLine()) != null) {
                addWordToTrie(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addWordToTrie(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (!node.children.containsKey(c)) {
                node.children.put(c, new TrieNode());
            }
            node = node.children.get(c);
        }
        node.isEnd = true;
    }

    // 检查是否包含敏感词
    public boolean containsSensitiveWords(String text) {
        for (int i = 0; i < text.length(); i++) {
            int matchLength = checkFromIndex(text, i);
            if (matchLength > 0) {
                return true;
            }
        }
        return false;
    }

    private int checkFromIndex(String text, int start) {
        TrieNode node = root;
        int matched = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!node.children.containsKey(c)) {
                break;
            }
            node = node.children.get(c);
            matched++;
            if (node.isEnd) {
                return matched;
            }
        }
        return 0;
    }
}

