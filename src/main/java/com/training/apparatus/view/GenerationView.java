package com.training.apparatus.view;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.service.GeneratorType;
import com.training.apparatus.data.service.ResultService;
import com.training.apparatus.data.service.TextGeneratorFactory;
import com.training.apparatus.data.service.UserService;
import com.training.apparatus.data.text.TextGenerator;
import com.training.apparatus.data.text.data.BaseText;
import com.training.apparatus.data.text.data.GenerationParameter;
import com.training.apparatus.secutiy.SecurityService;
import com.training.apparatus.view.components.TypingWithCommandsBlock;
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
@Route(value = "", layout = MainLayout.class)
@PageTitle("Generation")
public class GenerationView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserService userService;
    private final User auth;
    private final ResultService resultService;

    private final TextGeneratorFactory generatorFactory;
    private final  TypingWithCommandsBlock typingBlock;
    private final ComboBox<GeneratorType> generatorTypeComboBox;
    private final ComboBox<Integer> ngramCombox;

    private final IntegerField lengthTextField;
    private final static Integer MAX_LIMIT = 10000;
    private final static Integer MIN_LIMIT = 5;

    public GenerationView(SecurityService securityService, UserService userService, ResultService resultService, TextGeneratorFactory generatorFactory) {
        auth = securityService.getAuthUser();
        this.securityService = securityService;
        this.userService = userService;
        this.resultService = resultService;
        this.generatorFactory = generatorFactory;
        addClassName("generation-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        typingBlock = new  TypingWithCommandsBlock();
        typingBlock.setSizeFull();

        generatorTypeComboBox = new ComboBox<>(getTranslation("generation.typeOfGeneration"), GeneratorType.values());
        generatorTypeComboBox.setItemLabelGenerator(item -> getTranslation(item.getKey()));
        generatorTypeComboBox.setValue(GeneratorType.LETTERS);
        generatorTypeComboBox.addValueChangeListener(event -> this.regenerateText());
        ngramCombox = new ComboBox<>(getTranslation("generation.deepOfCorrelation"), IntStream.range(1, 6).boxed().toList());
        ngramCombox.setValue(2);
        ngramCombox.addValueChangeListener(event -> this.regenerateText());
        lengthTextField = new IntegerField( getTranslation("generation.length"), 100, event -> this.regenerateText());
        lengthTextField.setMin(MIN_LIMIT);
        lengthTextField.setMin(MAX_LIMIT);
        lengthTextField.setValue(100);


        Button refreshButton = new Button(getTranslation("generation.refresh"));
        refreshButton.addClickListener(e -> this.regenerateText());
        HorizontalLayout parametersLayout = new HorizontalLayout(generatorTypeComboBox, ngramCombox, lengthTextField);
        typingBlock.addResultListener(typingResult -> this.regenerateText());
        typingBlock.addResultListener(typingResult -> resultService.save(typingResult, auth));
        add(parametersLayout, refreshButton, typingBlock);
        HorizontalLayout layout = new HorizontalLayout(typingBlock);
        layout.setSizeFull();
        add(layout);
        this.regenerateText();
    }

    private void regenerateText() {
        GeneratorType generatorTypeComboBoxValue = generatorTypeComboBox.getValue();
        if (generatorTypeComboBoxValue == null) {
            Notification.show(getTranslation("view.emptyOf", generatorTypeComboBox.getLabel()), 3, Notification.Position.MIDDLE);
            return;
        }
        Integer ngram = ngramCombox.getValue();
        if (ngram == null) {
            Notification.show(getTranslation("view.emptyOf", ngramCombox.getLabel()), 3, Notification.Position.MIDDLE);
            return;
        }

        Integer length = lengthTextField.getValue();
        if (length == null) {
            Notification.show(getTranslation("view.emptyOf",  lengthTextField.getLabel()), 3, Notification.Position.MIDDLE);
            return;
        }
//        if (length < MIN_LIMIT || length > MAX_LIMIT) {
//            Notification.show("Exceed limits %s. It should be in range %d..%d".formatted(lengthTextField.getLabel(), MIN_LIMIT, MAX_LIMIT), 3, Notification.Position.MIDDLE);
//            return;
//        }

        GenerationParameter generationParameter = new GenerationParameter(generatorTypeComboBoxValue, ngram, BaseText.Tolstoy);
        TextGenerator textGenerator = generatorFactory.textGenerator(generationParameter);
        String text = textGenerator.generateString(length);
        typingBlock.setText(text);
    }


}
