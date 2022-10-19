package com.training.apparatus.view;

import com.training.apparatus.data.entity.StandardLayouts;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.exceptions.ExceedTextSizeException;
import com.training.apparatus.data.service.ResultService;
import com.training.apparatus.data.service.UserService;
import com.training.apparatus.secutiy.SecurityService;
import com.training.apparatus.view.components.HtmlText;
import com.training.apparatus.view.components.TypingWithCommandsBlock;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PermitAll
@Route(value = "external", layout = MainLayout.class)
@PageTitle("External text")
public class ExternalTextView extends VerticalLayout {

    public static final int MIN_LENGTH_PART = 1;
    public static final int MAX_LENGTH_PART = 1000;
    public static final int MAX_TEXT_LENGTH = 1000000;

    private final SecurityService securityService;
    private final UserService userService;
    private final User auth;
    private final ResultService resultService;
    private final TextField linkTextField = new TextField(getTranslation("external.linkToExternalText"), "http://lib.ru/...");
    private final IntegerField partLengthField;

    private final HtmlText htmlText;
    private final TypingWithCommandsBlock typingBlock = new TypingWithCommandsBlock();

    public ExternalTextView(SecurityService securityService, UserService userService, ResultService resultService) {
        this.securityService = securityService;
        this.userService = userService;
        this.resultService = resultService;
        auth = this.securityService.getAuthUser();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        setSizeFull();

        ComboBox<StandardLayouts> layoutComboBox = new ComboBox<>(getTranslation("external.keyboardLayout"), StandardLayouts.values());
        layoutComboBox.setValue(StandardLayouts.Russian);
        htmlText = new HtmlText();
        partLengthField = new IntegerField(getTranslation("external.portionOfText"), 100, event -> nextPart(event.getValue()));
        partLengthField.setMin(MIN_LENGTH_PART);
        partLengthField.setMax(MAX_LENGTH_PART);
        partLengthField.setValue(100);

        add(new HorizontalLayout(linkTextField, layoutComboBox, partLengthField));
        add(new HorizontalLayout(new Button(getTranslation("external.prev"), event1 -> prevPart(partLengthField.getValue())),
                new Button(getTranslation("external.next"), event2 -> nextPart(partLengthField.getValue()))));



        linkTextField.addValueChangeListener(event -> {
            if (event.getValue() != null && event.getValue().startsWith("http")) {
                String link = event.getValue();
                setLink(userService, link);
            } else {
                linkTextField.setValue(event.getOldValue());
            }
        });
        layoutComboBox.addValueChangeListener(event -> typingBlock.setCommandLanguage(event.getValue().getLayout()));
        typingBlock.addResultListener(typingResult -> {
            try {
                String text = htmlText.getNextPart(partLengthField.getValue());
                typingBlock.setText(text);
                this.userService.moveCursor(auth, htmlText.getCursor());
            } catch (ExceedTextSizeException e) {
                Notification.show(getTranslation(e.getCode()), 3000, Notification.Position.MIDDLE);
            }
        });
        typingBlock.addResultListener(typingResult -> this.resultService.save(typingResult, auth));
        add(typingBlock);

        Map<User.Settings, String> settingsMap = auth.getSettings();
        if (settingsMap != null) {
            String link = settingsMap.get(User.Settings.ExternalTextLink);
            if (link != null) {
                String decodedLink = URLDecoder.decode(link, StandardCharsets.UTF_8);
                linkTextField.setValue(decodedLink);
                String cursor = settingsMap.get(User.Settings.CursorInExternalText);
                if (cursor != null) {
                    try {
                        setCursor(Integer.parseInt(cursor), partLengthField.getValue());
                    } catch (Exception e) {
                        log.error("Impossible parse cursor value from string " + cursor, e);
                    }
                }
            }
        }
    }

    private void setLink(UserService userService, String link) {
        try {
            String text = htmlText.loadText(link, MAX_TEXT_LENGTH, partLengthField.getValue());
            typingBlock.setText(text);
            userService.setUserText(auth, link);
            final String showText = (text.length() > 10)?text.substring(0, 10): text;
            Notification.show(getTranslation("external.textIsLoaded", showText), 3000, Notification.Position.MIDDLE);
        } catch (ExceedTextSizeException e) {
            Notification.show(getTranslation(e.getCode()), 3000, Notification.Position.MIDDLE);
        }
    }

    private void prevPart(Integer length) {
        try {
            String text = htmlText.getPrevPart(length);
            this.userService.moveCursor(auth, htmlText.getCursor());
            typingBlock.setText(text);
        } catch (ExceedTextSizeException e) {
            Notification.show(getTranslation(e.getCode()), 3000, Notification.Position.MIDDLE);
        }
    }

    private void nextPart(Integer length) {
        try {
            String text = htmlText.getNextPart(length);
            this.userService.moveCursor(auth, htmlText.getCursor());
            typingBlock.setText(text);
        } catch (ExceedTextSizeException e) {
            Notification.show(getTranslation(e.getCode()), 3000, Notification.Position.MIDDLE);
        }
    }

    private void setCursor(Integer cursor, Integer length) {
        try {
            String text = htmlText.getNextPart(cursor, length);
            typingBlock.setText(text);
        } catch (ExceedTextSizeException e) {
            Notification.show(getTranslation(e.getCode()), 3000, Notification.Position.MIDDLE);
        }
    }

}
