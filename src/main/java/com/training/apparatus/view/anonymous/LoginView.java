package com.training.apparatus.view.anonymous;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Login | Simulator")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();
    private final Button registration = new Button("Registration");

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        login.setAction("login");

        registration.addClickListener(e ->
                registration.getUI().ifPresent(ui ->
                        ui.navigate("registration"))
        );

//        add(new H1("Training apparatus"), login, registration);
//
        Button about = new Button("About", event -> registration.getUI().ifPresent(ui ->
                ui.navigate("about")));
        Button theoretical = new Button("Theoretical description", event -> registration.getUI().ifPresent(ui ->
                ui.navigate("theoretical")));

        Button demo = new Button("Demo", event -> registration.getUI().ifPresent(ui ->
                ui.navigate("demo")));
        HorizontalLayout buttons = new HorizontalLayout(registration, about, theoretical, demo);
        add(new H1("Training apparatus"), login, buttons);

    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}
