package com.translate.trans.model;

import java.util.ArrayList;
import java.util.List;

public class LanguageConfig {
    private static final List<LanguageOption> languages = new ArrayList<>();

    static {
        languages.add(new LanguageOption("auto", "Tự phát hiện ngôn ngữ"));
        languages.add(new LanguageOption("vi-VN", "Tiếng Việt (vi-VN)"));
        languages.add(new LanguageOption("en-US", "Tiếng Anh (Hoa Kỳ) (en-US)"));
        languages.add(new LanguageOption("en-GB", "Tiếng Anh (Anh) (en-GB)"));
        languages.add(new LanguageOption("ja-JP", "Tiếng Nhật (ja-JP)"));
        languages.add(new LanguageOption("es-ES", "Tiếng Tây Ban Nha (es-ES)"));
        languages.add(new LanguageOption("fr-FR", "Tiếng Pháp (fr-FR)"));
        languages.add(new LanguageOption("de-DE", "Tiếng Đức (de-DE)"));
        languages.add(new LanguageOption("it-IT", "Tiếng Ý (it-IT)"));
        languages.add(new LanguageOption("ko-KR", "Tiếng Hàn (ko-KR)"));
        languages.add(new LanguageOption("zh-CN", "Tiếng Trung Quốc (zh-CN)"));
        languages.add(new LanguageOption("pt-BR", "Tiếng Bồ Đào Nha (Brazil) (pt-BR)"));
        languages.add(new LanguageOption("hi-IN", "Tiếng Hindi (hi-IN)"));
        languages.add(new LanguageOption("ar-SA", "Tiếng Ả Rập (ar-SA)"));
        languages.add(new LanguageOption("ru-RU", "Tiếng Nga (ru-RU)"));
        languages.add(new LanguageOption("pl-PL", "Tiếng Ba Lan (pl-PL)"));
        languages.add(new LanguageOption("tr-TR", "Tiếng Thổ Nhĩ Kỳ (tr-TR)"));
        languages.add(new LanguageOption("sv-SE", "Tiếng Thụy Điển (sv-SE)"));
        languages.add(new LanguageOption("no-NO", "Tiếng Na Uy (no-NO)"));
        languages.add(new LanguageOption("ms-MY", "Tiếng Malay (ms-MY)"));
    }

    public static List<LanguageOption> getLanguages() {
        return languages;
    }
}
