package xyz.gghost.jskype.message;


import org.apache.commons.lang3.StringEscapeUtils;

public final class FormatUtils {

    private FormatUtils() {
    }

    /**
     * Format text
     *
     * @param text to format
     * @return Formatted text
     */
    public static String bold(String text) {
        return "<b>" + text + "</b>";
    }

    /**
     * Format text
     *
     * @param text to format
     * @return Formatted text
     */
    public static String italic(String text) {
        return "<i>" + text + "</i>";
    }

    /**
     * Format text
     *
     * @param text to format
     * @return Formatted text
     */
    public static String strikethrough(String text) {
        return "<s>" + text + "</s>";
    }

    /**
     * Format text
     *
     * @param text to format
     * @return Formatted text
     */
    public static String blink(String text) {
        return "<blink>" + text + "</blink>";
    }

    /**
     * Format text
     *
     * @param text to format
     * @return Formatted text
     */
    public static String underline(String text) {
        return "<u>" + text + "</u>";
    }

    /**
     * Format text
     *
     * @param text to format
     * @return Formatted text
     */
    public static String code(String text) {
        return "<pre>" + text + "</pre>";
    }

    /**
     * Format url
     *
     * @param url to link
     * @return Formatted url
     */
    public static String link(String url) {
        return "<a href=\"" + url + "\">" + url + "</a>";
    }

    /**
     * Format text
     *
     * @param text to format
     * @param size the size of the desired string
     * @return Formatted text
     */
    public static String size(String text, int size) {
        return "<font size=\"" + size + "\">" + String.valueOf(text) + "</font>";
    }

    /**
     * Format text
     *
     * @param text to format
     * @return Formatted text
     */
    public static String encodeRawText(String text) {
        return StringEscapeUtils.escapeXml11((StringEscapeUtils.escapeJson(text)));
    }

    /**
     * Add emoji to text
     *
     * @param emoji the emoticon to add to the string
     * @return string with the emoji
     */
    public static String emoji(String emoji) {
        return "<ss type=\"" + emoji.replace("(", "").replace(")", "") + "\">" + emoji + "</ss>";
    }

    public static String decodeText(String text) {
        text = StringEscapeUtils.unescapeHtml3(text);
        text = StringEscapeUtils.unescapeHtml4(text);
        text = StringEscapeUtils.unescapeXml(text); //skype is stupid
        text = StringEscapeUtils.unescapeJson(text);
        return text;
    }

}
