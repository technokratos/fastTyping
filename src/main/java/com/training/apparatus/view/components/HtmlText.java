package com.training.apparatus.view.components;

import com.training.apparatus.data.exceptions.ExceedTextSizeException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * @author Kulikov Denis
 * @since 17.10.2022
 */
@Slf4j
public class HtmlText {

    private String text = "";
    private Integer cursor = 0;

    public String getNextPart(Integer partLength) throws ExceedTextSizeException {
        if (cursor + partLength > text.length()) {
            throw new ExceedTextSizeException("htmlText.positionOutOfText");
        }
        cursor += partLength;
        int end = (cursor + partLength > text.length()) ? (text.length() - 1) : cursor + partLength;
        return text.substring(cursor, end);
    }

    public String loadText(String url, int maxLength, Integer partLength) throws ExceedTextSizeException {
        try {//todo if url empty throw exep
            Document doc = Jsoup.connect(url).get();
            StringBuffer buffer = new StringBuffer();
            Element body = doc.body();
            parse(body, buffer, maxLength);
            text = buffer.toString().replaceAll("[«»]","\"").replaceAll("—","-").replaceAll("\n", "").replaceAll("\r", "");
            cursor = 0;
        } catch (IOException e) {
            log.error("Problem with load text", e);
            throw new ExceedTextSizeException("htmlText.problemWithLoadText");
        }
        int end = (cursor + partLength > text.length()) ? (text.length() - 1) : cursor + partLength;
        return text.substring(0, end);
    }

    public String getPrevPart(Integer partLength) throws ExceedTextSizeException {
        if (cursor - partLength <= 0) {
            cursor = 0;
        } else {
            cursor -= partLength;
        }
        int end = (cursor + partLength > text.length()) ? (text.length() - 1) : cursor + partLength;
        return text.substring(cursor, end);
    }

    private void parse(Element element, StringBuffer buffer, int maxLength) {
        if (buffer.length() > maxLength) {
            return;
        }
        int childrenSize = element.childNodeSize();
        if (childrenSize == 0) {
            append(element, buffer);
            return;
        }
        Element childElement;
        for (int i = 0; i < childrenSize; i++) {
            Node node = element.childNode(i);
            if (node instanceof Element) {
                childElement = (Element) node;
                switch (childElement.tagName().toLowerCase()) {
                    case "h1", "h2", "h3", "h4", "h5", "p", "i", "sup", "br", "b" -> append(element, buffer);
                    case "div", "span" -> parse(childElement, buffer, maxLength);
                    case "img" -> {
                        //skip
                    }
                    default -> {
                        append(element, buffer);
                        log.warn("Don't support tag {}", childElement.tagName());
                    }
                }
            } else {
                append(element, buffer);
            }
            if (buffer.length() > maxLength) {
                return;
            }
        }

    }

    private static StringBuffer append(Element element, StringBuffer buffer) {
        return buffer.append(element.text());
    }


    public String getNextPart(Integer cursor, Integer length) throws ExceedTextSizeException {
        this.cursor = cursor - length;
        return getNextPart(length);
    }

    public int getCursor() {
        return cursor;
    }
}
