package com.loki.lochat.utils.text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AhoCorasick {

    private final Node root;

    public AhoCorasick(List<String> words) {
        this.root = new Node((char) 0);
        buildTrie(words);
        buildFailLinks();
    }

    private void buildTrie(List<String> words) {
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            Node node = root;
            for (char c : word.toCharArray()) {
                char normalized = normalize(c);
                Node current = node;
                node = node.children.computeIfAbsent(normalized, ch -> new Node(ch, current));
            }
            node.output = true;
            node.word = word;
        }
    }

    private void buildFailLinks() {
        Deque<Node> queue = new ArrayDeque<>();
        for (Node node : root.children.values()) {
            node.fail = root;
            queue.addLast(node);
        }
        while (!queue.isEmpty()) {
            Node current = queue.pollFirst();
            for (Node child : current.children.values()) {
                Node fail = current.fail;
                while (fail != root && !fail.children.containsKey(child.c)) {
                    fail = fail.fail;
                }
                if (fail != root || fail.children.containsKey(child.c)) {
                    Node f = fail.children.get(child.c);
                    child.fail = f != null ? f : root;
                } else {
                    child.fail = root;
                }
                child.output = child.output || child.fail.output;
                if (child.fail.output && child.word == null) {
                    child.word = child.fail.word;
                }
                queue.addLast(child);
            }
        }
    }

    public boolean matches(String text) {
        Node node = root;
        for (int i = 0; i < text.length(); i++) {
            char c = normalize(text.charAt(i));
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            node = node.children.getOrDefault(c, root);
            if (node.output) {
                return true;
            }
        }
        return false;
    }

    public String replaceAll(String text, String replacement) {
        Node node = root;
        int lastEnd = 0;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = normalize(text.charAt(i));
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            node = node.children.getOrDefault(c, root);
            if (node.output && node.word != null) {
                result.append(text, lastEnd, i - node.word.length() + 1);
                result.append(replacement);
                lastEnd = i + 1;
            }
        }
        result.append(text, lastEnd, text.length());
        return result.toString();
    }

    public List<String> findMatches(String text) {
        List<String> found = new ArrayList<>();
        Node node = root;
        for (int i = 0; i < text.length(); i++) {
            char c = normalize(text.charAt(i));
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            node = node.children.getOrDefault(c, root);
            if (node.output && node.word != null) {
                found.add(node.word);
            }
        }
        return found;
    }

    private static char normalize(char c) {
        return SwearNormalizer.normalize(c);
    }

    private static final class Node {
        final char c;
        final Map<Character, Node> children = new HashMap<>();
        Node fail;
        boolean output;
        String word;

        Node(char c) {
            this.c = c;
            this.fail = this;
        }

        Node(char c, Node parent) {
            this.c = c;
        }
    }
}
