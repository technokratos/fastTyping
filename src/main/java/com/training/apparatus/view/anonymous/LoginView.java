package com.training.apparatus.view.anonymous;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
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
    private final Button registration = new Button(getTranslation("login.registration"));

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        initTranslation();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        login.setAction("login");

        registration.addClickListener(e ->
                registration.getUI().ifPresent(ui ->
                        ui.navigate("registration"))
        );

//        add(new H1("Training apparatus"), login, registration);
//
        Button about = new Button(getTranslation("login.about"), event -> registration.getUI().ifPresent(ui ->
                ui.navigate("about")));
        Button theoretical = new Button(getTranslation("login.theoreticalDescription"), event -> registration.getUI().ifPresent(ui ->
                ui.navigate("theoretical")));

        Button demo = new Button(getTranslation("login.demo"), event -> registration.getUI().ifPresent(ui ->
                ui.navigate("demo")));
        HorizontalLayout buttons = new HorizontalLayout(registration, about, theoretical, demo);
        add(new H1(getTranslation("login.trainingApparatus")), login, buttons);

    }

    private void initTranslation() {
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle("");
        i18nForm.setUsername(getTranslation("login.user"));
        i18nForm.setPassword(getTranslation("login.password"));
        i18nForm.setSubmit(getTranslation("login.submit"));
        i18nForm.setForgotPassword(getTranslation("login.forgotPassword"));
        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle(getTranslation("login.errorLogin"));
        i18nErrorMessage.setMessage(getTranslation("login.errorMessage"));
        i18n.setErrorMessage(i18nErrorMessage);


        login.setI18n(i18n);
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
