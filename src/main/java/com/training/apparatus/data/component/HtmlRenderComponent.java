package com.training.apparatus.data.component;

import com.training.apparatus.view.LoginView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * @author Kulikov Denis
 * @since 14.10.2022
 */
@Slf4j
public class HtmlRenderComponent extends Div {


    private final String base;

    private HtmlRenderComponent(String text, String base) {
        this.base = base;
        parse(text);
    }

    public void setHtml(String text) {
        removeAll();
        parse(text);
    }

    private void parse(String text) {
        Document document = Jsoup.parse(text);
        Element body = document.body();
        parse(body, this);
    }

    public static HtmlRenderComponent byFileName(String base, String fileName) {
        try {
            ClassLoader classLoader = HtmlRenderComponent.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(base + fileName);
            return new HtmlRenderComponent(IOUtils.toString(inputStream, StandardCharsets.UTF_8), base);
        } catch (Exception e) {
            log.error("Error in creating HtmlView", e);
        }
        return new HtmlRenderComponent("", base);
    }

    private void parse(Element element, HtmlContainer parent) {
        int childrenSize = element.childNodeSize();
        if (childrenSize == 0) {
            parent.add(new Text(element.text()));
            return;
        }
        Element childElement;
        Component childComponent;
        for (int i = 0; i < childrenSize; i++) {
            Node node = element.childNode(i);
            if (node instanceof Element) {
                childElement = (Element) node;
                switch (childElement.tagName().toLowerCase()) {
                    case "h1" -> parent.add(new H1(childElement.text()));
                    case "h2" -> parent.add(new H2(childElement.text()));
                    case "h3" -> parent.add(new H3(childElement.text()));
                    case "h4" -> parent.add(new H4(childElement.text()));
                    case "h5" -> parent.add(new H5(childElement.text()));
                    case "p" -> parent.add(new Paragraph(childElement.text()));
                    case "i", "sup", "br", "b" -> {
                        Text text = new Text(childElement.text());
                        parent.add(text);
                    }
                    case "div" -> {
                        childComponent = new Div();
                        parse(childElement, (Div) childComponent);
                        parent.add(childComponent);
                    }
                    case "span" -> {
                        childComponent = new Span();
                        parse(childElement, (Span) childComponent);
                        parent.add(childComponent);
                    }
                    case "img" -> {
                        String src = childElement.attr("src");
                        String alt = childElement.attr("alt");
                        parent.add(new Image(base64(base + src), (alt.equals(""))? src: alt ));
                    }
                    default -> {
                        parent.add(new Text(childElement.text()));
                        log.warn("Don't support tag {}", childElement.tagName());
                    }
                }
            } else {
                parent.add(new Text(node.outerHtml()));
            }
        }

    }

    private String base64(String src) {


        try {
            final byte[] imageBytes;
            if (src.startsWith("http")) {
                imageBytes = IOUtils.toByteArray(new URL(src));
            } else {
                ClassLoader classLoader = LoginView.class.getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream(URLDecoder.decode(src, StandardCharsets.UTF_8));
                if (inputStream == null) {
                    log.error("Not found resource {}", src);
                    return "";
                }
                imageBytes = IOUtils.toByteArray(inputStream);

            }
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            return "data:image/png;base64, %s".formatted(base64);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return "";
        }

    }

}
