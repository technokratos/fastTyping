    package com.training.apparatus.view.anonymous;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.service.EncodingService;
import com.training.apparatus.data.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
@Route("approveEmail")
@PageTitle("Approve email")
@Getter
@Setter
@Slf4j
public class ApproveEmailView extends VerticalLayout  implements BeforeEnterObserver {

    @Value("${fasttypping.host}")
    private String host;
    @Autowired
    private UserService userService;

    @Autowired
    private EncodingService encodingService;
    private Label email = new Label();


    public ApproveEmailView(){
        view();

    }

    public void view() {
        email.setMinWidth("450px");
        add(email);
        Button main = new Button(getTranslation("common.login"));
        main.addClickListener(event ->
                getUI().ifPresent(ui ->
                        ui.navigate(""))
        );
        add(main);
        setAlignItems(Alignment.CENTER);
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
                User user = userService.findByEmail(emailValue);
                String hash = hashes.get(0);
                if (user != null) {
                    boolean checkHash = encodingService.checkHashRestoreEmail(hash, emailValue, user.getId());
                    if (checkHash) {
                        email.setText(getTranslation("approve.emailIsApproved", emailValue));
                        user.setApprovedEmail(true);
                        userService.save(user);
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

