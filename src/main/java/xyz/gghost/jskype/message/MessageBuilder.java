package xyz.gghost.jskype.message;

/**
 * Created by Ghost on 25/09/2015.
 */
public class MessageBuilder {

    private String outHtml = "";

    public MessageBuilder(String s) {
        outHtml = s;
    }

    public MessageBuilder() {
    }


    public MessageBuilder addEmjoi(String s) {
        outHtml = outHtml + MessageBuilderUtils.emoji(s);
        return this;
    }

    public MessageBuilder addHtml(String s) {
        outHtml = outHtml + s;
        return this;
    }

    public MessageBuilder addLink(String link) {
        outHtml = outHtml + MessageBuilderUtils.link(MessageBuilderUtils.encodeRawText(link));
        return this;
    }

    public MessageBuilder addText(String s) {
        outHtml = outHtml + MessageBuilderUtils.encodeRawText(s);
        return this;
    }

    public MessageBuilder isBlinking() {
        outHtml = MessageBuilderUtils.blink(outHtml);
        return this;
    }

    public MessageBuilder isItalic() {
        outHtml = MessageBuilderUtils.italic(outHtml);
        return this;
    }

    public MessageBuilder isBold() {
        outHtml = MessageBuilderUtils.bold(outHtml);
        return this;
    }

    public MessageBuilder isUnderlined() {
        outHtml = MessageBuilderUtils.underline(outHtml);
        return this;
    }

    public String build() {
        return outHtml;
    }

}
