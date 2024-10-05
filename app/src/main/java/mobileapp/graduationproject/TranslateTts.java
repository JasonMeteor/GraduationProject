package mobileapp.graduationproject;

public class TranslateTts {

    private String text;
    private int language_flag;
    private String flag; // 伺服器提取特定檔案用的

    public TranslateTts(String text, int language_flag, String file_flag) {
        this.text = text;
        this.language_flag = language_flag;
        this.flag = file_flag;
    }

    public void setLanguage_flag(int language_flag) {
        this.language_flag = language_flag;
    }
}
