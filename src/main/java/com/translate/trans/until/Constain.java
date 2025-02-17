package com.translate.trans.until;

public interface Constain {
    public final String ASSERT_KEY = "Dịch cho tôi đoạn văn sau từ ";
    public final int GEMINI_2 = 2;
    public final String GOOGLE_TRANSLATE = "1";
    public final long WAIT_TIME = 300000; // 600000ms

    public interface MODEL_GEMINI {
        public final String GEMINI_2_0_FLASH_EXP = "gemini-2.0-flash-exp:generateContent?key=";
        public final String GEMINI_2_0_FLASH_THINK_EXP_01_21 = "gemini-2.0-flash-thinking-exp-01-21:generateContent?key=";
        public final String GEMINI_2_0_PRO_EXP_02_05 = "gemini-2.0-pro-exp-02-05:generateContent?key=";
        public final String GEMINI_2_0_FLASH = "gemini-2.0-flash:generateContent?key=";
        public final String GEMINI_2_0_FLASH_PRE_02_05 = "gemini-2.0-flash-lite-preview-02-05:generateContent?key=";
        public final String GEMINI_1_5_PRO = "gemini-1.5-pro:generateContent?key=";
        public final String GEMINI_1_5_FLASH = "gemini-1.5-flash:generateContent?key=";
        public final String GEMINI_1_5_FLASH_8B = "gemini-1.5-flash-8b:generateContent?key=";

    }

    public interface ContenType {
        public final String DOC = "application/msword";
        public final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }
}
