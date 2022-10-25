package com.training.apparatus.view.anonymous;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.UserRepository;
import com.training.apparatus.data.service.EmailServiceImpl;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@AnonymousAllowed
@Route("sendRestorePasswordEmail")
@PageTitle("Send restore password email")
@Getter
@Setter
public class SendRestorePasswordView extends VerticalLayout  {
    private TextField email = new TextField(getTranslation("registration.email"));

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailServiceImpl emailService;

    private Button send = new Button(getTranslation("restore.sendEmail"));



    public SendRestorePasswordView() {
        view();
        EmailValidator emailValidator = new EmailValidator(getTranslation("registration.isNotEmail"));
        Validator<String> existValidator = (value, context) -> {
            User user = userRepository.findByEmail(value);
            if (user != null) {
                return ValidationResult.ok();
            } else {
                return ValidationResult.error(getTranslation("restore.emailIsNotExist"));
            }
        };
        List<Validator<String>> validators = Arrays.asList(emailValidator, existValidator);

        send.addClickListener(event -> {
            String value = email.getValue();
            List<ValidationResult> results = validators.stream().map(validator -> validator.apply(value, null))
                    .filter(ValidationResult::isError).toList();
            if (!results.isEmpty()) {
                results.forEach(it->Notification.show(it.getErrorMessage()));
            } else {
                emailService.restorePassword(value, getLocale(), () -> Notification.show(getTranslation("common.sentEmail")));
            }
        });

    }

    public void view() {
        email.setMinWidth("450px");
        send.setMinWidth("450px");

        add(email, send);
        setAlignItems(Alignment.CENTER);
    }





}

