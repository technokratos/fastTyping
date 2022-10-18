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
            throw new ExceedTextSizeException("Position is out text");
        }
        cursor += partLength;
        int end = (cursor + partLength > text.length()) ? (text.length() - 1) : cursor + partLength;
        return text.substring(cursor, end);
    }

    public String loadText(String url, int maxLength, Integer partLength) throws ExceedTextSizeException {
        try {
            Document doc = Jsoup.connect(url).get();
            StringBuffer buffer = new StringBuffer();
            Element body = doc.body();
            parse(body, buffer, maxLength);
            text = buffer.toString().replaceAll("[«»]","\"").replaceAll("—","-");
            cursor = 0;
        } catch (IOException e) {
            throw new ExceedTextSizeException("Problem with load text " + e.getMessage());
        }
        int end = (cursor + partLength > text.length()) ? (text.length() - 1) : cursor + partLength;
        return text.substring(0, end);
    }

    public String getPrevPart(Integer partLength) throws ExceedTextSizeException {
        if (cursor - partLength <= 0) {
            throw new ExceedTextSizeException("Position is out text");
        }
        cursor -= partLength;
        int end = (cursor + partLength > text.length()) ? (text.length() - 1) : cursor + partLength;
        return text.substring(cursor, end);
    }

    private void parse(Element element, StringBuffer buffer, int maxLength) {
        if (buffer.length() > maxLength) {
            return;
        }
        int childrenSize = element.childNodeSize();
        if (childrenSize == 0) {
            buffer.append(element.text());
            return;
        }
        Element childElement;
        for (int i = 0; i < childrenSize; i++) {
            Node node = element.childNode(i);
            if (node instanceof Element) {
                childElement = (Element) node;
                switch (childElement.tagName().toLowerCase()) {
                    case "h1", "h2", "h3", "h4", "h5", "p", "i", "sup", "br", "b" -> buffer.append(element.text());
                    case "div", "span" -> parse(childElement, buffer, maxLength);
                    case "img" -> {
                        //skip
                    }
                    default -> {
                        buffer.append(element.text());
                        log.warn("Don't support tag {}", childElement.tagName());
                    }
                }
            } else {
                buffer.append(element.text());
            }
            if (buffer.length() > maxLength) {
                return;
            }
        }

    }
}
