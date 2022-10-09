package com.training.apparatus.view.components;

import com.training.apparatus.data.widget.Stopwatch;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TypingBlock extends VerticalLayout {


    private final TextField textField;
    private final TextArea textArea;

    private final Label resultLabel;
    private final Div imageContainer;
    private final Stopwatch stopwatch = new Stopwatch();
    private Integer count = 0;
    private String text = "";

    private final List<Consumer<TypingResult>> resultListeners = new ArrayList<>();

    public TypingBlock() {
        setSizeFull();
        imageContainer = new Div();
        initMap();

        textArea = new TextArea();
        textArea.setReadOnly(true);
        textArea.setValue(text);
        textArea.setWidth("60%");
        textField = new TextField("start typing");
        textField.setWidth("60%");
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        resultLabel = new Label();

        //textField.addThemeVariants();
        add(imageContainer, textArea, textField, resultLabel);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        Key key = Key.of("f");
        textField.addKeyUpListener(key, null, KeyModifier.SHIFT);

        textField.addKeyPressListener(e -> {
            startTimer();
            incCount();
            if (textField.getValue().length() != 0)
                checkForCorrection(1);
            updateResult();
            stopTimer();
        });
    }

    public void addResultListener(Consumer<TypingResult> action) {
        resultListeners.add(action);
    }
    public void removeResultListener(Consumer<TypingResult> action) {
        resultListeners.remove(action);
    }

    public void setText(String text) {
        this.text = text;
        textArea.setValue(text);
        textField.setValue("");
    }

    private void initMap() {
        List<KeyboardImageMap> maps = List.of(
                new KeyboardImageMap("Плоская карта(рус.)", "keyboard_maps/map_rus.png"),
                new KeyboardImageMap("Пирамида(рус.)", "keyboard_maps/piramid_rus.png"),
                new KeyboardImageMap("Пирамида(англ.)", "keyboard_maps/piramid_eng.png"),
                new KeyboardImageMap("Симметричная пирамида(рус.)", "keyboard_maps/piramid_sym_rus.png"),
                new KeyboardImageMap("Симметричная пирамида(англ.)", "keyboard_maps/piramid_sym_eng.png"));



        ContextMenu menu = new ContextMenu();
        menu.setTarget(imageContainer);
        ComboBox<KeyboardImageMap> mapComboBox = new ComboBox<>("Карта клавиатуры", maps);
        menu.add(mapComboBox);
        imageContainer.add(maps.iterator().next().image);
        mapComboBox.setValue(maps.iterator().next());

        mapComboBox.addValueChangeListener(e -> {
            imageContainer.removeAll();
            imageContainer.add(e.getValue().image);
        });
    }

    private void updateResult() {
        double currentTimeSec = stopwatch.getCurrentTimeSec();
        if (stopwatch.isStarted() && currentTimeSec > 0) {
            int length = textField.getValue().length();
            double charInMinutes = length / currentTimeSec / 60.0;
            int baseTextLength = textArea.getValue().length();
            int errors = count - length;
//            double errorRate = 100.0 * errors / baseTextLength; //, %.2f %%
            resultLabel.setText("%.2f зн/мин, %d из %d, ошибок %d".formatted(charInMinutes, length, baseTextLength, errors));
        }
    }

    public void checkForCorrection(int ind) {
        String filed = textField.getValue();
        char chInput = filed.charAt(filed.length() - ind);
        char chComp = text.charAt(filed.length() - ind);
        if (chInput == chComp) {
            String sub = filed.substring(0, filed.length() - ind + 1);
            textField.setValue(sub);
        } else {
            if (filed.length() - ind == 0) {
                String sub = filed.substring(0, filed.length() - ind);
                textField.setValue(sub);
                return;
            }
            checkForCorrection(ind + 1);
        }
    }

    public void startTimer() {
        stopwatch.start();
    }

    public void stopTimer() {
        String str = textField.getValue();
        if (text.length() == str.length() &&
                text.charAt(text.length() - 1) == str.charAt(str.length() - 1)) {
            stopwatch.stop();
            textField.setValue("");
            generateResult();
            count = 0;
        }
    }

    public void generateResult() {
        resultLabel.setText("");
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(5000);
        double timeInMinutes = stopwatch.getResultMin();
        int length = text.length();
        int speed = ((int)(count / timeInMinutes));
        double mistakes = (double)length / count * 100;
        TypingResult typingResult = new TypingResult(timeInMinutes, length, speed, mistakes);
        resultListeners.forEach(it -> it.accept(typingResult));

        String str = "Скорость: %d зн/мин. Ваша точность:%.2f %%".formatted(speed, mistakes);
        Div div = new Div(new Text(str));

        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.getElement().setAttribute("aria-label", "Close");
        closeButton.addClickListener(event -> notification.close());

        HorizontalLayout layout = new HorizontalLayout(div, closeButton);
        layout.setAlignItems(Alignment.CENTER);
        notification.setPosition(Notification.Position.MIDDLE);

        notification.add(layout);
        notification.open();
    }

    public void incCount() {
        count++;
    }

    public void setEditable(boolean editable) {
        textArea.setReadOnly(!editable);
    }

    public String getText() {
        return text;
    }

    static class KeyboardImageMap {
        private final String name;
        private final Image image;


        KeyboardImageMap(String name, String fileName) {
            this.name = name;
            //StreamResource res = new StreamResource("keyboard-map.png", () -> Simulator.class.getClassLoader().getResourceAsStream("keyboard_maps/map_rus.png"));
            StreamResource res = new StreamResource(fileName.substring(fileName.indexOf("/") + 1), () -> TypingBlock.class.getClassLoader().getResourceAsStream(fileName));
            image = new Image(res, "Карта клавиатуры");
            image.setWidth(1024, Unit.PIXELS);

        }

        @Override
        public String toString() {
            return name;
        }
    }


    public record TypingResult(double timeInMinutes, int length, int speed, double mistakes) {}
}
