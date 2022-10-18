package com.training.apparatus.view;

import com.training.apparatus.data.entity.Language;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.exceptions.ExceedTextSizeException;
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
@Route(value = "", layout = MainLayout.class)
@PageTitle("External text")
public class ExternalTextView extends VerticalLayout {

    public static final int MIN_LENGTH_PART = 1;
    public static final int MAX_LENGTH_PART = 1000;
    public static final int MAX_TEXT_LENGTH = 100000;

    private final SecurityService securityService;
    private final UserService userService;
    private final User auth;
    private final TextField linkTextField = new TextField("Link to external html text", "http://lit.lib.ru/...");
    private final IntegerField partLengthField;

    private final HtmlText htmlText;
    private final TypingWithCommandsBlock typingBlock = new TypingWithCommandsBlock();

    public ExternalTextView(SecurityService securityService, UserService userService) {
        this.securityService = securityService;
        this.userService = userService;
        auth = this.securityService.getAuthUser();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        setSizeFull();
        //todo change language to keyboard layout
        ComboBox<Language> languageComboBox = new ComboBox<>("Language", Language.values());
        languageComboBox.setValue(Language.Russian);
        htmlText = new HtmlText();
        partLengthField = new IntegerField("Portion of text", 100, event -> nextPart(event.getValue()));
        partLengthField.setMin(MIN_LENGTH_PART);
        partLengthField.setMax(MAX_LENGTH_PART);

        add(new HorizontalLayout(linkTextField, partLengthField));
        add(new HorizontalLayout(new Button("Prev", event1 -> prevPart(partLengthField.getValue())), new Button("Next", event2 -> nextPart(partLengthField.getValue()))));



        linkTextField.addValueChangeListener(event -> {
            if (event.getValue() != null && event.getValue().startsWith("http")) {
                String link = event.getValue();
                setLink(userService, link);
            } else {
                linkTextField.setValue(event.getOldValue());
            }
        });
        languageComboBox.addValueChangeListener(event -> {
            typingBlock.setCommandLanguage(event.getValue());
        });
        typingBlock.addResultListener(typingResult -> {
            try {
                String text = htmlText.getNextPart(partLengthField.getValue());
                typingBlock.setText(text);
                this.userService.moveCursor(auth, typingBlock.getText().length());
            } catch (ExceedTextSizeException e) {
                Notification.show(e.getMessage(), 5, Notification.Position.MIDDLE);
            }
        });
        add(typingBlock);

        Map<User.Settings, String> settingsMap = auth.getSettings();
        if (settingsMap != null) {
            String link = settingsMap.get(User.Settings.ExternalTextLink);
            if (link != null) {
                String decodedLink = URLDecoder.decode(link, StandardCharsets.UTF_8);
                String cursor = settingsMap.get(User.Settings.CursorInExternalText);
                if (cursor != null) {
                    try {
                        partLengthField.setValue(Integer.parseInt(cursor));
                    } catch (Exception e) {
                        log.error("Impossible parse cursor value from string " + cursor, e);
                    }
                }
                linkTextField.setValue(decodedLink);
                setLink(userService, decodedLink);
            }

        }
    }

    private void setLink(UserService userService, String link) {
        try {
            String text = htmlText.loadText(link, MAX_TEXT_LENGTH, partLengthField.getValue());
            typingBlock.setText(text);
            userService.setUserText(auth, link);
            Notification.show("Text is loaded: '%s...'".formatted(text.substring(0, 10)));
        } catch (ExceedTextSizeException e) {
            Notification.show(e.getMessage(), 3, Notification.Position.MIDDLE);
        }
    }

    private void prevPart(Integer length) {
        try {
            String text = htmlText.getPrevPart(length);
            typingBlock.setText(text);
        } catch (ExceedTextSizeException e) {
            Notification.show(e.getMessage(), 3, Notification.Position.MIDDLE);
        }
    }

    private void nextPart(Integer length) {
        try {
            String text = htmlText.getNextPart(length);
            typingBlock.setText(text);
        } catch (ExceedTextSizeException e) {
            Notification.show(e.getMessage(), 3, Notification.Position.MIDDLE);
        }
    }

}
