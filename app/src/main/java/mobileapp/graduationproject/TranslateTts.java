package mobileapp.graduationproject;

public class TranslateTts {

    private String text;
    private int from_language;
    private int to_language;
    private String flag; // 伺服器提取特定檔案用的

    public TranslateTts(String text, int from_language, int to_language, String flag) {
        this.text = text;
        this.from_language = from_language;
        this.to_language = to_language;
        this.flag = flag;
    }

    public void setLanguage_flag(int from_language, int to_language) {
        this.from_language = from_language;
        this.to_language = to_language;
    }
}
