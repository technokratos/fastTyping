package com.training.apparatus.view;

import com.training.apparatus.data.entity.TheoreticalTopic;
import com.training.apparatus.data.service.TheoreticalTopicService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.List;
import java.util.Optional;

//@PermitAll
//@Route(value = "theoreticalEditor", layout =  MainLayout.class)
//@PageTitle("Theoretical Background")
public class TheoreticalBackgroundView  extends VerticalLayout {

    TheoreticalTopicService theoreticalTopicService;
    TextArea textArea = new TextArea();
    ComboBox<TheoreticalTopic> comboBox = new ComboBox<>();
    HorizontalLayout horizontalLayout = new HorizontalLayout();
    HorizontalLayout horizontalButtons = new HorizontalLayout();
    Long currentTopicId = 0L;
    Integer currentNumberPage = 0;

    public TheoreticalBackgroundView(TheoreticalTopicService theoreticalTopicService) {
        this.theoreticalTopicService = theoreticalTopicService;
        addClassName("list-view");
        setSizeFull();
        configurePage();

    }

    private void configurePage() {
        ComboBoxListDataView<TheoreticalTopic> topics = comboBox.setItems(theoreticalTopicService.getTheoreticalTopics());
        comboBox.setItemLabelGenerator(TheoreticalTopic::getNameTopic);
        Optional<TheoreticalTopic> theoreticalTopicOptional = theoreticalTopicService.getTheoreticalTopics().stream()
                .findFirst();
        currentTopicId = theoreticalTopicOptional.map(TheoreticalTopic::getId).orElse(0L);
        configureDownLayout();



        comboBox.setWidth("60%");
        textArea.setWidth("60%");
        textArea.setHeight("50%");
        textArea.setValueChangeMode(ValueChangeMode.EAGER);
        horizontalLayout.setWidth("60%");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        comboBox.addFocusListener(event -> {
            TheoreticalTopic topic = comboBox.getValue();
            Long idTopic = theoreticalTopicService.findTopicByName(topic.getNameTopic()).getId();
            updateButton(idTopic);
        });

        Optional<TheoreticalTopic> firstTopicOptional = topics.getItems().findFirst();
        if(firstTopicOptional.isPresent()) {
            TheoreticalTopic firstTopic = firstTopicOptional.get();
            comboBox.setValue(firstTopic);
            updateButton(firstTopic.getId());
        }
        add(comboBox, textArea, horizontalLayout);

    }

    private void configureDownLayout() {
        Button buttonSave = new Button("Save");
        buttonSave.addClickListener(event -> {
            TheoreticalTopic topic = comboBox.getValue();
            if(topic != null) {
                theoreticalTopicService.saveTextByTopicsAndPage(topic.getId(), currentNumberPage, textArea.getValue());
            }

        });
        horizontalLayout.add(horizontalButtons, buttonSave);
    }

    private void updateButton(long id) {
        int count = theoreticalTopicService.getCountPageByIdTopic(id);
        horizontalButtons.removeAll();
        for(int i = 0; i < count + 1; i ++) {
            Button button = new Button(String.valueOf(i+1));
            TheoreticalTopic topic = comboBox.getValue();
            if(topic != null) {
                int finalI = i;
                button.addClickListener(event -> {
                    currentNumberPage = finalI + 1;
                    textArea.setValue(theoreticalTopicService.viewPage(topic.getId(), finalI + 1));
                });
            }
            horizontalButtons.add(button);
        }
        List<Component> componentList = horizontalButtons.getChildren().toList();

        if(componentList.size() > 1) {
            TheoreticalTopic topic = comboBox.getValue();
            textArea.setValue(theoreticalTopicService.viewPage(topic.getId(), 1));
        } else {
            textArea.setValue("");
        }
    }




}
