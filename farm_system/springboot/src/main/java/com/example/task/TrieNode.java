package com.example.task;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    public boolean isEnd = false;
    public Map<Character, TrieNode> children = new HashMap<>();
}
