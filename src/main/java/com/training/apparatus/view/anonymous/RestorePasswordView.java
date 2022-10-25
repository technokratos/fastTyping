package com.training.apparatus.view.anonymous;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.service.EncodingService;
import com.training.apparatus.data.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@AnonymousAllowed
@Route("restorePassword")
@PageTitle("Restore password")
@Getter
@Setter
@Slf4j
public class RestorePasswordView extends VerticalLayout  implements BeforeEnterObserver {
    @Value("${fasttypping.host}")
    private String host;
    @Autowired
    private UserService userService;

    @Autowired
    private EncodingService encodingService;
    private Label email = new Label();
    private PasswordField password = new PasswordField(getTranslation("registration.password"));
    private PasswordField confirmPassword = new PasswordField(getTranslation("registration.confirmPassword"));
    private Button updatePassword = new Button(getTranslation("restorePassword.updatePassword"));

    Binder<User> binder = new Binder<>(User.class);
    private User user;

    public RestorePasswordView(){
        view();
        validate();
    }

    public void view() {
        email.setMinWidth("450px");
        password.setMinWidth("450px");
        confirmPassword.setMinWidth("450px");
        updatePassword.setMinWidth("450px");
        add(
                email,
                password, confirmPassword,
                updatePassword
        );
        setAlignItems(Alignment.CENTER);
    }

    private void save() {
        if (user == null) {
            log.error("Error in restore password, Incorrect state, user is null");
            throw new IllegalStateException("Incorrect state, user is null");
        }
        try {
            if(binder.validate().isOk() ) {

                binder.writeBean(user);
//                user.setRole(Role.ROLE_WORKER);
                user.setApprovedEmail(true);
                userService.saveUserWithEncodePassword(user);
                updatePassword.getUI().ifPresent(ui ->
                    ui.navigate(""));
            }
        } catch (ValidationException e) {
            e.getValidationErrors().forEach(error-> Notification.show(error.getErrorMessage()));
        } finally {
            System.out.println();
        }

    }

    private void validate() {
        password.setValueChangeMode(ValueChangeMode.EAGER);
        confirmPassword.setValueChangeMode(ValueChangeMode.EAGER);


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
    public void beforeEnter(BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        Map<String, List<String>> parametersMap = queryParameters
                .getParameters();

        if (parametersMap.containsKey("email") && parametersMap.containsKey("hash")) {

            List<String> emails = parametersMap.get("email");
            List<String> hashes = parametersMap.get("hash");
            if (emails.size() == 1 && hashes.size() == 1) {
                String emailValue = emails.get(0);
                user = userService.findByEmail(emailValue);
                String hash = hashes.get(0);
                if (user != null) {
                    boolean checkHash = encodingService.checkHashRestoreEmail(hash, emailValue, user.getId());
                    if (checkHash) {
                        email.setText(emailValue);
                        updatePassword.addClickListener(e -> save());
                    } else {
                        log.warn("Incorrect hash link for restore password {}", location.getPath());
                        Notification.show(getTranslation("common.incorrectLink"));
                        redirectToHost();
                    }
                } else {
                    log.warn("Incorrect user for restore password {}", location.getPath());
                    Notification.show(getTranslation("common.incorrectLink"));
                    redirectToHost();
                }
            } else {
                log.warn("Incorrect parameters for restore password {}", location.getPath());
                redirectToHost();
            }

        }
    }

    private void redirectToHost() {
        UI.getCurrent().getPage().setLocation(host);
    }
}

