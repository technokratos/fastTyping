package com.training.apparatus.view;

import com.training.apparatus.data.entity.Language;
import com.training.apparatus.data.entity.Result;
import com.training.apparatus.data.entity.Role;
import com.training.apparatus.data.entity.Task;
import com.training.apparatus.data.entity.Type;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.ResultRepository;
import com.training.apparatus.data.repo.TaskRepository;
import com.training.apparatus.data.service.ResultService;
import com.training.apparatus.data.service.UserService;
import com.training.apparatus.secutiy.SecurityService;
import com.training.apparatus.view.components. TypingWithCommandsBlock;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.transaction.Transactional;

@PermitAll
@Route(value = "course", layout = MainLayout.class)
@PageTitle("Course")
public class CourseView extends VerticalLayout {

    TaskRepository taskRepository;
    SecurityService securityService;
    ResultService resultService;
    ResultRepository resultRepository;
    UserService userService;
    Tabs lessonTabs;
    Tabs languageTabs;
    User auth;
    Label title;

    private final  TypingWithCommandsBlock typingBlock;
    private final TextArea textArea;

    private final Map<Tab, Task> tabTaskMap = new HashMap<>();

    public CourseView(TaskRepository taskRepository, SecurityService securityService,
                      ResultService resultService, ResultRepository resultRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.securityService = securityService;
        this.resultService = resultService;
        this.resultRepository = resultRepository;
        this.userService = userService;
        addClassName("course-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setAuth();
        title = new Label("");
        textArea = new TextArea();
        textArea.setSizeFull();
        typingBlock = new  TypingWithCommandsBlock();
        add(addHorizTables());
        typingBlock.addResultListener(this::addResult);
        HorizontalLayout layout = new HorizontalLayout(addVerticalTables(), typingBlock);
        layout.setSizeFull();
        add(layout);
        setAlignItems(Alignment.CENTER);
        if (auth.getRole().equals(Role.ROLE_ADMIN)) {
            typingBlock.setVisible(false);
            layout.add(textArea);
            textArea.setVisible(true);
            Button saveButton = new Button("Save");
            ToggleButton tryButton = new ToggleButton("Try");
            add(new HorizontalLayout(saveButton, tryButton));
            saveButton.addClickListener(b -> save());
            tryButton.addClickListener(buttonClickEvent -> {
                        if (tryButton.getValue()) {
                            if (textArea.getValue().length() > 0) {
                                textArea.setVisible(false);
                                typingBlock.setVisible(true);
                                typingBlock.setText(textArea.getValue());
                                typingBlock.addResultListener(typingResult -> {
                                    typingBlock.setVisible(false);
                                    textArea.setVisible(true);
                                });
                            } else {
                                Notification.show("Empty text");
                                tryButton.setValue(false);
                            }
                        } else {
                            typingBlock.setVisible(false);
                            textArea.setVisible(true);
                        }
                    }
            );
        }

    }

    public void setAuth() {
        auth = securityService.getAuthUser();
    }

    public Div addHorizTables() {
        Div div = new Div();
        Tab russian = new Tab("Russian");
        Tab english = new Tab("English");

        languageTabs = new Tabs(russian, english);
        lessonTabs = new Tabs();
        languageTabs.setMaxWidth("100%");
        languageTabs.setWidth("400px");
        div.add(languageTabs);
        languageTabs.addSelectedChangeListener(event -> {
                    updateVerticalTables();
                    fillTextFields();
                }
        );
        updateVerticalTables();
        fillTextFields();
        return div;
    }

    public VerticalLayout addVerticalTables() {
        updateVerticalTables();
        VerticalLayout layout = new VerticalLayout();

        if (auth.getRole().equals(Role.ROLE_ADMIN)) {
            Button button = new Button("add Element");
            layout.add(button);
            button.addClickListener(
                    b -> addElement()
            );
        }

        layout.add(lessonTabs);
        lessonTabs.setWidth("240px");
        lessonTabs.setOrientation(Tabs.Orientation.VERTICAL);
        layout.setWidth("250px");
        lessonTabs.addSelectedChangeListener(event -> fillTextFields());
        return layout;
    }


    public void updateVerticalTables() {
        lessonTabs.removeAll();
        String name = languageTabs.getSelectedTab().getLabel();
        List<Task> tasks = taskRepository.findByTypeAndLanguage(Type.Basic, Language.valueOf(name));
        tasks.forEach(task -> {
            Long results = resultRepository.countByTaskAndUserOrderById(task, auth);
            final Icon icon = (results > 0) ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CLOSE_CIRCLE.create();
            Tab tab = new Tab(icon, new Span(task.getTitle()));
            lessonTabs.add(tab);
            tabTaskMap.put(tab, task);
        });
    }

    public void fillTextFields() {
        if (lessonTabs.getChildren().toList().size() == 0) {
            title.setText("");
            textArea.setValue("The text for typing is not ready yet");
            typingBlock.setText("The text for typing is not ready yet");
            return;
        }
        Task task = tabTaskMap.get(lessonTabs.getSelectedTab());

        if (task != null) {
            textArea.setValue(task.getText());
            typingBlock.setText(task.getText());
        } else {
            textArea.setValue("Enter text");
            typingBlock.setText("Not filled");
        }

        List<Result> results = resultRepository.findByTaskAndUserOrderById(task, auth);

        if (results.size() > 0) {
            Result last = results.get(results.size() - 1);
            title.setText("You have already completed this task\n" +
                    "Your speed " + last.getSpeed() + "\n" +
                    "Your error rate " + last.getMistakes());
        } else {
            title.setText("You haven't completed this quest yet");
        }

    }

    @Transactional
    public void save() {

        Task task = getSelectedTask();
        task.setText(typingBlock.getText());
        taskRepository.save(task);
        updateVerticalTables();
        fillTextFields();


    }

    private Task getSelectedTask() {
        return tabTaskMap.get(lessonTabs.getSelectedTab());
    }


    //todo extract to external service to use transaction
    @Transactional
    public void addElement() {
        if (tabTaskMap.values().stream().allMatch(it -> it.getId() != 0)) {
            Notification.show("Save current new lesson");
        } else {
            String name = languageTabs.getSelectedTab().getLabel();
            Language language = Language.valueOf(name);
            long size = taskRepository.countByTypeAndLanguage(Type.Basic, language);
            String title = "New lesson " + size;
            Tab tab = new Tab(
                    VaadinIcon.CLOSE_CIRCLE.create(),
                    new Span(title)
            );

            lessonTabs.add(tab);
            Task task = new Task();
            task.setNumber(size);
            task.setType(Type.Basic);
            task.setTitle(title);
            task.setLanguage(language);
            taskRepository.saveAndFlush(task);

            tabTaskMap.put(tab, task);
            fillTextFields();
        }
    }


    private void addResult( TypingWithCommandsBlock.TypingResult typingResult) {
        Task task = getSelectedTask();
        Tab selectedTab = lessonTabs.getSelectedTab();
        Optional<Component> iconOptional = selectedTab.getChildren().filter(it -> it instanceof Icon).findFirst();
        iconOptional.ifPresent(selectedTab::remove);
        selectedTab.addComponentAsFirst(VaadinIcon.CHECK_CIRCLE.create());
        resultService.save(task, typingResult.mistakes(), typingResult.timeInMinutes(), typingResult.length(), auth);
    }

}
