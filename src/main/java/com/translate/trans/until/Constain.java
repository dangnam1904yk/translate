package com.translate.trans.until;

public interface Constain {
    public final String ASSERT_KEY = "Dịch cho tôi đoạn văn sau từ ";
    public final int GEMINI_2 = 2;
    public final String GOOGLE_TRANSLATE = "1";

    public interface MODEL_GEMINI {
        public final String GEMINI_2_0_PRO_EXP_02_05 = "gemini-2.0-pro-exp-02-05:generateContent?key=";
        public final String GEMINI_2_0_FLASH_THINK_EXP_01_21 = "gemini-2.0-flash-thinking-exp-01-21:generateContent?key=";
        public final String GEMINI_2_0_FLASH_EXP = "gemini-2.0-flash-exp:generateContent?key=";
    }

    public interface ContenType {
        public final String DOC = "application/msword";
        public final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }
}
