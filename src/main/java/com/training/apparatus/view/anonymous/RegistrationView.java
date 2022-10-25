package com.training.apparatus.view.anonymous;

import com.training.apparatus.data.entity.Group;
import com.training.apparatus.data.entity.Role;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.UserRepository;
import com.training.apparatus.data.service.EncodingService;
import com.training.apparatus.data.service.GroupService;
import com.training.apparatus.data.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@AnonymousAllowed
@Route("registration")
@PageTitle("Registration")
@Getter
@Setter
public class RegistrationView extends VerticalLayout implements HasUrlParameter<String> {

    private final Label groupName = new Label();
    private TextField pseudonym = new TextField(getTranslation("registration.pseudonym"));
    private TextField email = new TextField(getTranslation("registration.email"));
    private PasswordField password = new PasswordField(getTranslation("registration.password"));
    private PasswordField confirmPassword = new PasswordField(getTranslation("registration.confirmPassword"));
    private Button registration = new Button(getTranslation("login.registration"));
    private final Button confidential = new Button(getTranslation("registration.confidential"));
    Binder<User> binder = new Binder<>(User.class);

    @Value("${fasttypping.host}")
    private String host;
    private User user = new User();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncodingService encodingService;

    @Autowired
    private GroupService groupService;
    private Group group;

    public RegistrationView() {

        view();
        validate();
        registration.addClickListener(e -> save());
        confidential.addClickListener(event -> registration.getUI().ifPresent(ui ->
                ui.navigate("confidential")));
    }

    public void view() {
        add(groupName);
        pseudonym.setMinWidth("450px");
        email.setMinWidth("450px");
        password.setMinWidth("450px");
        confirmPassword.setMinWidth("450px");
        registration.setMinWidth("450px");
        add(
                pseudonym, email,
                password, confirmPassword,
                registration, confidential
        );
        setAlignItems(Alignment.CENTER);
    }

    private void save() {
        try {
            if (binder.validate().isOk()) {
                binder.writeBean(user);
                user.setRole(Role.ROLE_WORKER);
                user.setGroup(group);
                userService.saveUserWithEncodePassword(user);
                registration.getUI().ifPresent(ui ->
                        ui.navigate(""));
            }
        } catch (ValidationException e) {
            e.getValidationErrors().forEach(error -> Notification.show(error.getErrorMessage()));
        } finally {
            System.out.println();
        }

    }

    private void validate() {
        password.setValueChangeMode(ValueChangeMode.EAGER);
        confirmPassword.setValueChangeMode(ValueChangeMode.EAGER);

        //binderPseudonym.bind(pseudonym, User::getPseudonym, User::setPseudonym);
        binder.forField(pseudonym)
                // Explicit validator instance
                .asRequired(getTranslation("registration.pseudonymShouldBeFilled"))
                .bind(User::getPseudonym, User::setPseudonym);

        binder.forField(email)
                // Explicit validator instance
                .withValidator(new EmailValidator(
                        //"This doesn't look like a valid email address"
                        getTranslation("registration.isNotEmail")))
                .withValidator((SerializablePredicate<String>) email -> !userRepository.existsByEmail(email),
                        getTranslation("registration.emailIsAlreadyExist", email)
                )
                .bind(User::getEmail, User::setEmail);


        binder.forField(password)
                // Validator defined based on a lambda
                // and an error message
                .withValidator(
                        pass -> pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{4,15}$"),
                        //"Password does not meet the requirements"
                        getTranslation("registration.wrongPassword"))
                .bind(User::getPassword, User::setPassword);

        binder.forField(confirmPassword)
                // Validator defined based on a lambda
                // and an error message
                .withValidator(
                        pass -> password.getValue().equals(confirmPassword.getValue()),
                        getTranslation("registration.notTheSamePassword"));//"The spanks are not the same"
    }


    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if (parameter.equals("group")) {
            log.info("Registration for group");
            Location location = event.getLocation();
            QueryParameters queryParameters = location.getQueryParameters();

            Map<String, List<String>> parametersMap = queryParameters
                    .getParameters();

            String checkedParameter = "groupId";

            if (parametersMap.containsKey(checkedParameter) && parametersMap.containsKey("hash")) {

                List<String> ids = parametersMap.get(checkedParameter);
                List<String> hashes = parametersMap.get("hash");
                if (ids.size() == 1 && hashes.size() == 1) {
                    String parameterValue = ids.get(0);
                    String hash = hashes.get(0);
                    Optional<Group> groupOptional = groupService.getGroupById(Long.parseLong(parameterValue));
                    if (groupOptional.isPresent()) {
                        boolean checkHash = encodingService.checkedGroupHash(hash, Long.parseLong(parameterValue), groupOptional.get().getManager().getId());
                        if (checkHash) {
                            group = groupOptional.get();
                            groupName.setText(getTranslation("registration.groupCreatedBy", group.getName(), group.getManager().getPseudonym(), group.getManager().getEmail()));
                        } else {
                            log.warn("Incorrect hash link for registration group {}", location.getPath());
                            Notification.show(getTranslation("registration.incorrectLink"));
                            redirectToHost();
                        }
                    } else {
                        log.warn("Absent group for registration group {}", location.getPath());
                        Notification.show(getTranslation("registration.incorrectLink"));
                        redirectToHost();
                    }


                } else {
                    log.warn("Incorrect parameters for registration group {}", location.getPath());
                    redirectToHost();
                }

            } else {
                log.warn("Absent parameters for registration group {}", location.getPath());
                redirectToHost();
            }
        } else {
            log.info("Single registration");
        }
    }

    private void redirectToHost() {
        UI.getCurrent().getPage().setLocation(host);
    }
}

