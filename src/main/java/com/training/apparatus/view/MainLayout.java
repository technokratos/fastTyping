package com.training.apparatus.view;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.secutiy.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

import java.util.Optional;


public class MainLayout extends AppLayout {
    private final SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1(getTranslation("mainView.trainingApparatus"));
        logo.addClassNames("text-l", "m-m");

        Button logout = new Button(getTranslation("mainView.logOut"), e -> securityService.logout());

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo,
                logout
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.expand(logo);
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {

//        RouterLink course = new RouterLink(getTranslation("mainView.course"), CourseView.class);
        RouterLink generation = new RouterLink(getTranslation("mainView.generation"), GenerationView.class);
//        RouterLink theoretical = new RouterLink(getTranslation("mainView.theoretical"), TheoreticalBackgroundView.class);
        //RouterLink dashboard = new RouterLink(getTranslation("mainView.dashboard"), DashboardView.class);
        RouterLink externalText = new RouterLink(getTranslation("mainView.externalText"), ExternalTextView.class);
        RouterLink profile = new RouterLink(getTranslation("mainView.profile"), ProfileView.class);
        RouterLink worker = null;
        Optional<User> user = Optional.ofNullable(securityService.getAuthUser());
        if(user.isPresent()) {
            if(user.get().getRole().name().equals("ROLE_BOSS")) {
                 worker = new RouterLink(getTranslation("mainView.worker"), WorkerListView.class);
            }
        }
        generation.setHighlightCondition(HighlightConditions.sameLocation());

        if(worker == null) {
            addToDrawer(new VerticalLayout(
                    /*listLink, course,*/ generation, externalText,  /*theoretical, dashboard,*/ profile
            ));
        } else {
            addToDrawer(new VerticalLayout(
                    /*listLink, course,*/ generation, externalText,/*theoretical, dashboard,*/ profile, worker
            ));
        }

    }
}
