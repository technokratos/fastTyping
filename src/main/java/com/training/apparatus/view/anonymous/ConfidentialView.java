package com.training.apparatus.view.anonymous;

import com.training.apparatus.view.components.HtmlRenderComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.Getter;
import lombok.Setter;

@AnonymousAllowed
@Route("confidential")
@PageTitle("Confidentiality policy")
@Getter
@Setter
public class ConfidentialView extends VerticalLayout {

    public ConfidentialView() {
        Button main = new Button(getTranslation("login.registration"));
        main.addClickListener(event ->
                getUI().ifPresent(ui ->
                        ui.navigate(""))
        );
        add(main);
        add(HtmlRenderComponent.byFileName("static/","confidential.html"));
    }

}

