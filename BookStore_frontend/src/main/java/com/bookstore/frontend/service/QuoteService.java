package com.bookstore.frontend.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuoteService {

    // Áp dụng Singleton Pattern để chỉ giữ 1 bản copy duy nhất trên RAM
    private static QuoteService instance;
    private final List<String> quotes;
    private final Random random;

    private QuoteService() {
        quotes = new ArrayList<>();
        random = new Random();
        loadQuotes();
    }

    public static QuoteService getInstance() {
        if (instance == null) {
            instance = new QuoteService();
        }
        return instance;
    }

    private void loadQuotes() {
        try (InputStream is = getClass().getResourceAsStream("/com/bookstore/frontend/data/quotes.txt")) {
            if (is == null) {
                System.err.println("Không tìm thấy file quotes.txt. Sẽ dùng câu mặc định.");
                quotes.add("\"Chào mừng bạn đến với tiệm sách nhỏ của tôi.\" - Chủ quán <3");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        quotes.add(line.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải file quotes.txt: " + e.getMessage());
            quotes.add("\"Hệ thống quản lý nhà sách chuyên nghiệp.\" - Neth BookPoint");
        }
    }

    public String getRandomQuote() {
        if (quotes.isEmpty()) return "";
        return quotes.get(random.nextInt(quotes.size()));
    }
}