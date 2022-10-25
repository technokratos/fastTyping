package com.training.apparatus.view;

import com.training.apparatus.data.entity.Group;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.UserRepository;
import com.training.apparatus.data.service.EncodingService;
import com.training.apparatus.data.service.GroupService;
import com.training.apparatus.data.service.UserService;
import com.training.apparatus.secutiy.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


@Getter
@Setter
@SpringComponent
@UIScope
public class RegistrationGroupView extends VerticalLayout {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;
    @Autowired
    private SecurityService securityService;

    @Autowired
    private EncodingService encodingService;

    @Value("${fasttypping.host}")
    private String host;
    private TextField name = new TextField(getTranslation("group.name"));
    private Label link = new Label();
    private Button generate = new Button(getTranslation("group.create"));

    Binder<Group> binder = new Binder<>(Group.class);

    private Group group = new Group();

    private User user;

    public RegistrationGroupView() {
        //binder.bindInstanceFields(this);
        view();
        validate();
        setAlignItems(Alignment.CENTER);
    }

    @PostConstruct
    private void init() {
        user = securityService.getAuthUser();
    }

    public void view() {
        Span span = new Span(getTranslation("registrationGroup.title"));

        name.setMinWidth("450px");
        link.setMinWidth("450px");

        add(span, name, link, generate);
        setAlignItems(FlexComponent.Alignment.CENTER);
        generate.addClickListener(e -> save());
    }

    private void save() {
        try {
            if (binder.validate().isOk()) {
                binder.writeBean(group);
                group.setManager(user);
                group = groupService.save(group);
                String hash = encodingService.encodingGroup(group.getId(), user.getId());
                String linkValue = "%s/registration/group?groupId=%d&hash=%s".formatted(host, group.getId(), hash);
                group.setLink(linkValue);
                link.setText(linkValue);
                groupService.save(group);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
        } finally {
            System.out.println();
        }

    }

    private void validate() {
        name.setValueChangeMode(ValueChangeMode.EAGER);

        binder.forField(name)
                // Explicit validator instance
                .asRequired(getTranslation("registrationGroup.nameOfGroupShouldBeFilled"))
                .withValidator(name -> groupService.notExistGroupForTheUser(name, user), getTranslation("group.isAlreadyExist", name.getValue()))
                .bind(Group::getName, Group::setName);

    }
}
