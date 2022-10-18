package com.training.apparatus.view.components;

import com.training.apparatus.data.entity.Language;
import com.training.apparatus.data.widget.Stopwatch;
import com.training.apparatus.keyboards.Command;
import com.training.apparatus.keyboards.CommandMode;
import com.training.apparatus.keyboards.Finger;
import com.training.apparatus.keyboards.FingerCommandService;
import com.training.apparatus.keyboards.Hand;
import com.training.apparatus.keyboards.Position;
import com.training.apparatus.translation.TranslationProvider;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.StreamResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;

public class TypingWithCommandsBlock extends VerticalLayout {


    private final TextField textField;
    private final TextArea textArea;

    private final Label resultLabel;
    private final Div imageContainer;
    private final Stopwatch stopwatch = new Stopwatch();
    private Map<Character, Command> commandMapper;
    private Integer count = 0;
    private String text = "";

    private final HorizontalLayout commandPanel;

    private CommandMode commandMode = CommandMode.Full;

    private Locale currentLocale;

    private Character currentChar;

    private static final Map<Triple<Command, CommandMode, Locale>, CommandGroup> commandGroupMap = new HashMap<>();
    private final List<Consumer<TypingResult>> resultListeners = new ArrayList<>();

    private boolean showCommands = true;

    public TypingWithCommandsBlock() {
        imageContainer = new Div();
        initContextMenu();

        currentLocale = TranslationProvider.LOCALE_RU;
        commandMapper = new FingerCommandService().getCommandMapper(currentLocale);
        setSizeFull();
        commandPanel = new HorizontalLayout();
//        initContextMenuForCommandMode(commandPanel);




        textArea = new TextArea();
        textArea.getStyle().set("text-align","center");
        textArea.setReadOnly(true);
        textArea.setValue(text);
        textArea.setWidth("100%");
        textField = new TextField("Input text", "Start typing");
        textField.setWidth("60%");
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        resultLabel = new Label();

        add(imageContainer, textArea, commandPanel, textField, resultLabel);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        Key key = Key.of("f");
        textField.addKeyUpListener(key, null, KeyModifier.SHIFT);


        textField.addKeyPressListener(e -> {
            startTimer();
            incCount();
            if (textField.getValue().length() != 0) {
                String correctionChars = checkForCorrection(1);
                if (correctionChars != null) {
                    Notification notification = new Notification();
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setDuration(1000);
                    notification.setPosition(Notification.Position.BOTTOM_CENTER);
                    notification.add(correctionChars);
                    notification.open();
                }
            }
            updateResult();
            stopTimer();
        });
    }

    private void initContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        Checkbox checkbox = new Checkbox("Show commands", showCommands);
        checkbox.addValueChangeListener(e-> {
            showCommands = e.getValue();
            updateCommandPanel();
        });
        contextMenu.add(checkbox);
        ComboBox<CommandMode> formatCommandCombobox = new ComboBox<>("Format command", CommandMode.values());
        contextMenu.addItem(formatCommandCombobox);
        formatCommandCombobox.setValue(commandMode);
        formatCommandCombobox.addValueChangeListener(event -> {
            commandMode = event.getValue();
            updateCommandPanel();
        });

        ComboBox<KeyboardImageMap> keyboardImageMapComboBox = keyboardContextMenuItem();
        contextMenu.addItem(keyboardImageMapComboBox);
        contextMenu.setTarget(this);
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
        currentChar = text.charAt(0);
        updateCommandPanel();
    }

    private void updateCommandPanel() {
        commandPanel.removeAll();
        if (showCommands) {
            if (currentChar != null) {
                Command command = commandMapper.get(currentChar);
                if (command != null) {

                    CommandGroup commandGroup = commandGroupMap.computeIfAbsent(Triple.of(command, commandMode, currentLocale), key -> getCommandGroup(key.getLeft(), key.getMiddle(), key.getRight()));
                    String s = (currentChar == ' ') ? "<Space>" : currentChar.toString();

                    Label currentCharLabel = new Label(s + " - ");
                    currentCharLabel.getStyle().set("font-weight", "bold");
                    commandPanel.add(currentCharLabel);
                    commandGroup.create().forEach(commandPanel::add);
//                    if (command.getHand() == Hand.LeftHand) {
//                        commandPanel.getChildren()
//                                .filter(child -> child instanceof Icon)
//                                .map(child -> (Icon) child)
////                            .map(icon -> icon.)
                    /*
                    transform:scale(-1,1);
            -webkit-transform:scale(-1,1);
            -moz-transform:scale(-1,1);
            -o-transform:scale(-1,1)'
                     */
//                    }
                }
            }
        }
    }

    private CommandGroup getCommandGroup(Command command, CommandMode mode, Locale locale) {
        I18NProvider provider = new TranslationProvider();// VaadinService.getCurrent().getInstantiator().getI18NProvider();
        Hand hand = command.getHand();
        Finger finger = command.getFinger();
        Position[] position = command.getPosition();
        boolean shift = command.isShift();

        Function<String, CommandElement> elementInitiator = (mode == CommandMode.Icon) ? key -> iconElement(key, provider, locale) : key -> textElement(key, provider, locale, mode);

        List<CommandElement> elements = new ArrayList<>();
        if (shift) {
            elements.add(elementInitiator.apply("shift"));
        }
        elements.add(elementInitiator.apply(hand.name()));
        elements.add(elementInitiator.apply(finger.name()));
        Arrays.stream(position).map(Enum::name).map(elementInitiator).forEach(elements::add);
        return CommandGroup.of(elements);
    }

    private static CommandElement iconElement(String key, I18NProvider provider, Locale locale) {
        String value = provider.getTranslation(key + ".icon", locale);
        try {
            VaadinIcon icon = VaadinIcon.valueOf(value);
            CommandElement element = CommandElement.of(icon);
            if (key.equals("LeftHand")) {
                element.mirror = true;
            }
            return element;
        } catch (IllegalArgumentException e) {
            return CommandElement.of(value);
        }
    }

    private static CommandElement textElement(String key, I18NProvider provider, Locale locale, CommandMode mode) {
        final String postfix = mode.name().toLowerCase();
        return CommandElement.of(provider.getTranslation(key +"." + postfix, locale));
    }


    public void setCommandMode(CommandMode commandMode) {
        this.commandMode = commandMode;
        updateCommandPanel();
    }

    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
        commandMapper = new FingerCommandService().getCommandMapper(currentLocale);
        updateCommandPanel();
    }

    private ComboBox<KeyboardImageMap> keyboardContextMenuItem() {
        List<KeyboardImageMap> maps = List.of(
                new KeyboardImageMap("Плоская карта(рус.)", "keyboard_maps/map_rus.png"),
                new KeyboardImageMap("Пирамида(рус.)", "keyboard_maps/piramid_rus.png"),
                new KeyboardImageMap("Пирамида(англ.)", "keyboard_maps/piramid_eng.png"),
                new KeyboardImageMap("Симметричная пирамида(рус.)", "keyboard_maps/piramid_sym_rus.png"),
                new KeyboardImageMap("Симметричная пирамида(англ.)", "keyboard_maps/piramid_sym_eng.png"));




        ComboBox<KeyboardImageMap> mapComboBox = new ComboBox<>("Карта клавиатуры", maps);

        imageContainer.add(maps.iterator().next().image);
        mapComboBox.setValue(maps.iterator().next());

        mapComboBox.addValueChangeListener(e -> {
            imageContainer.removeAll();
            imageContainer.add(e.getValue().image);
        });
        return mapComboBox;
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

    public String checkForCorrection(int ind) {
        String filed = textField.getValue();
        char chInput = filed.charAt(filed.length() - ind);
        char chComp = text.charAt(filed.length() - ind);
        if (chInput == chComp) {
            String sub = filed.substring(0, filed.length() - ind + 1);
            textField.setValue(sub);
            if (textField.getValue().length() < textArea.getValue().length()) {
                currentChar = textArea.getValue().charAt(textField.getValue().length());
            } else {
                currentChar = null;
            }
            updateCommandPanel();
            return null;
        } else {
            if (filed.length() - ind == 0) {
                String sub = filed.substring(0, 0);
                textField.setValue(sub);
                return Character.toString(chInput);
            }
            String prevChars = checkForCorrection(ind + 1);
            if (prevChars != null) {
                return prevChars + chInput;
            } else {
                return Character.toString(chInput);
            }
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
        int speed = ((int) (count / timeInMinutes));
        double mistakes = (double) length / count * 100;
        TypingResult typingResult = new TypingResult(timeInMinutes, length, speed, mistakes);
        resultListeners.forEach(it -> it.accept(typingResult));

        String str = "Скорость: %d зн/мин. Ваша точность: %.2f %%".formatted(speed, mistakes);
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

    public void setCommandLanguage(Language value) {
        setCurrentLocale(value.getLocale());
    }


    static class KeyboardImageMap {
        private final String name;
        private final Image image;


        KeyboardImageMap(String name, String fileName) {
            this.name = name;
            //StreamResource res = new StreamResource("keyboard-map.png", () -> Simulator.class.getClassLoader().getResourceAsStream("keyboard_maps/map_rus.png"));
            StreamResource res = new StreamResource(fileName.substring(fileName.indexOf("/") + 1), () -> TypingWithCommandsBlock.class.getClassLoader().getResourceAsStream(fileName));
            image = new Image(res, "Карта клавиатуры");
            image.setWidth(1024, Unit.PIXELS);

        }

        @Override
        public String toString() {
            return name;
        }
    }


    public record TypingResult(double timeInMinutes, int length, int speed, double mistakes) {
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    static class CommandGroup {
        List<CommandElement> commandElements;


        List<Component> create() {
            return commandElements.stream().map(CommandElement::create).collect(Collectors.toList());
        }
    }

    @Data
    @AllArgsConstructor
    static class CommandElement {
        private final String text;
        private final VaadinIcon icon;

        private boolean mirror;

        public static CommandElement of(String text) {
            return new CommandElement(text, null, false);
        }

        public static CommandElement of(VaadinIcon icon) {
            return new CommandElement(null, icon, false);
        }

        Component create() {
            if (text != null) {
                Label label = new Label(text);
                label.getStyle().set("font-weight", "bold");
                return label;
            } else {
                Icon iconComponent = icon.create();
                if (isMirror()) {
                    iconComponent.getStyle().set("transform", "scale(-1, 1)");
                    iconComponent.getStyle().set("-webkit-transform", "scale(-1, 1)");
                    iconComponent.getStyle().set("-moz-transform", "scale(-1, 1)");
                    iconComponent.getStyle().set("-o-transform", "scale(-1, 1)");
                }
                return iconComponent;
            }
        }
    }
}
