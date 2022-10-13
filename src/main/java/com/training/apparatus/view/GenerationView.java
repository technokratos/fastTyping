package com.training.apparatus.view;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.service.GeneratorType;
import com.training.apparatus.data.service.TextGeneratorFactory;
import com.training.apparatus.data.service.UserService;
import com.training.apparatus.data.text.TextGenerator;
import com.training.apparatus.data.text.data.BaseText;
import com.training.apparatus.data.text.data.GenerationParameter;
import com.training.apparatus.secutiy.SecurityService;
import com.training.apparatus.view.components. TypingWithCommandsBlock;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.stream.IntStream;
import javax.annotation.security.PermitAll;

@PermitAll
@Route(value = "generation", layout = MainLayout.class)
@PageTitle("Generation")
public class GenerationView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserService userService;
    private final User auth;

    private final TextGeneratorFactory generatorFactory;
    private final  TypingWithCommandsBlock typingBlock;
    private final ComboBox<GeneratorType> generatorTypeComboBox;
    private final ComboBox<Integer> ngramCombox;

    private final IntegerField lengthTextField;
    private final static Integer MAX_LIMIT = 10000;
    private final static Integer MIN_LIMIT = 5;

    public GenerationView(SecurityService securityService, UserService userService, TextGeneratorFactory generatorFactory) {
        auth = securityService.getAuthUser();
        this.securityService = securityService;
        this.userService = userService;
        this.generatorFactory = generatorFactory;
        addClassName("generation-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        typingBlock = new  TypingWithCommandsBlock();
        typingBlock.setSizeFull();

        generatorTypeComboBox = new ComboBox<>("Type of generation", GeneratorType.values());
        generatorTypeComboBox.setValue(GeneratorType.LETTERS);
        generatorTypeComboBox.addValueChangeListener(event -> this.regenerateText());
        ngramCombox = new ComboBox<>("Deep of correlation", IntStream.range(1, 6).boxed().toList());
        ngramCombox.setValue(2);
        ngramCombox.addValueChangeListener(event -> this.regenerateText());
        lengthTextField = new IntegerField( "Length", "Fill length of objects for generation");
        lengthTextField.setValue(100);
        lengthTextField.addValueChangeListener(event -> this.regenerateText());

//        ComboBox<BaseText> baseTextComboBox = new ComboBox<>("Text style", BaseText.values());
        Button refreshButton = new Button("Refresh");
        refreshButton.addClickListener(e -> this.regenerateText());
        HorizontalLayout parametersLayout = new HorizontalLayout(generatorTypeComboBox, ngramCombox, lengthTextField,  refreshButton);
        typingBlock.addResultListener(typingResult -> this.regenerateText());
        add(parametersLayout, typingBlock);
        HorizontalLayout layout = new HorizontalLayout(typingBlock);
        layout.setSizeFull();
        add(layout);
        this.regenerateText();
    }

    private void regenerateText() {
        GeneratorType generatorTypeComboBoxValue = generatorTypeComboBox.getValue();
        if (generatorTypeComboBoxValue == null) {
            Notification.show("Empty of " + generatorTypeComboBox.getLabel(), 3, Notification.Position.MIDDLE);
            return;
        }
        Integer ngram = ngramCombox.getValue();
        if (ngram == null) {
            Notification.show("Empty of " + ngramCombox.getLabel(), 3, Notification.Position.MIDDLE);
            return;
        }

        Integer length = lengthTextField.getValue();
        if (length == null) {
            Notification.show("Empty of " + lengthTextField.getLabel(), 3, Notification.Position.MIDDLE);
            return;
        }
        if (length < MIN_LIMIT || length > MAX_LIMIT) {
            Notification.show("Exceed limits %s. It should be in range %d..%d".formatted(lengthTextField.getLabel(), MIN_LIMIT, MAX_LIMIT), 3, Notification.Position.MIDDLE);
            return;
        }

        GenerationParameter generationParameter = new GenerationParameter(generatorTypeComboBoxValue, ngram, BaseText.Tolstoy);
        TextGenerator textGenerator = generatorFactory.textGenerator(generationParameter);
        String text = textGenerator.generateString(length);
        typingBlock.setText(text);
    }


}
