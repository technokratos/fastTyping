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
@Route("about")
@PageTitle("About")
@Getter
@Setter
public class AboutView extends VerticalLayout {

    public AboutView() {
        Button main = new Button(getTranslation("common.login"));
        main.addClickListener(event ->
                getUI().ifPresent(ui ->
                        ui.navigate(""))
        );
        add(main);
        add(HtmlRenderComponent.byFileName("static/","about.html"));
    }

}

