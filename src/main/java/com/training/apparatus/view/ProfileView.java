package com.training.apparatus.view;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.GroupRepository;
import com.training.apparatus.data.repo.ResultRepository;
import com.training.apparatus.data.repo.UserRepository;
import com.training.apparatus.data.service.EmailServiceImpl;
import com.training.apparatus.data.service.GroupService;
import com.training.apparatus.data.service.UserService;
import com.training.apparatus.secutiy.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@PermitAll
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Worker List")
@Getter
@Setter
public class ProfileView extends VerticalLayout {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    EmailServiceImpl emailService;

    User user;

    Button reset;
    private Span speedSpan;
    private Span mistakesSpan;
    private Span attemptsSpan;


    public ProfileView() {
        addClassName("profile");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
    }

    @PostConstruct
    private void init() {
        addUser();
        add(getToolBox(), getReset());
        if (!Boolean.TRUE.equals(user.getApprovedEmail())) {
            Button requestEmailButton = new Button(getTranslation("profile.requestApproveEmail"));
            requestEmailButton.addClickListener(event -> emailService.approveEmail(user, getLocale(), () -> Notification.show(getTranslation("common.sentEmail"))));
            add(requestEmailButton);
        }
    }


    public void addUser() {
        user = securityService.getAuthUser();
    }

    public VerticalLayout getToolBox() {
        VerticalLayout vert = new VerticalLayout();
        Span hello = new Span(getTranslation("profile.pseudonym", user.getPseudonym()));

        speedSpan = new Span(getTranslation("profile.averageSpeed", resultRepository.avgSpeed(user.getId()).orElse(0.0)));
        mistakesSpan = new Span(getTranslation("profile.averageCorrectness", resultRepository.avgMistakes(user.getId()).orElse(0.0)));
        attemptsSpan = new Span(getTranslation("profile.attempts", resultRepository.countResult(user.getId())));

        vert.add(hello);
        if (user.getGroup() != null) {
            vert.add(new Span(getTranslation("common.group", user.getGroup().getName())));
        }
        vert.add(speedSpan, mistakesSpan, attemptsSpan);
        vert.setAlignItems(Alignment.CENTER);
        return vert;
    }

    public Button getReset() {
        reset = new Button(getTranslation("profile.resetResults"));
        reset.addClickListener(e -> {
            userService.resetResult(user.getEmail());
            speedSpan.setText(getTranslation("profile.averageSpeed", 0.0));
            mistakesSpan.setText(getTranslation("profile.averageCorrectness", 0.0));
            attemptsSpan.setText(getTranslation("profile.attempts", 0));
        });
        return reset;
    }

}
