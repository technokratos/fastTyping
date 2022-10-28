package com.training.apparatus.view;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.provider.UserDataProvider;
import com.training.apparatus.data.service.UserService;
import com.training.apparatus.secutiy.SecurityService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kulikov Denis
 * @since 28.10.2022
 */
@PermitAll
@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Admin")
@Getter
@Setter
public class AdminView extends VerticalLayout {
    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDataProvider userDataProvider;

    private final Grid<User> grid;

    public AdminView() {
        grid = new Grid<>(20);
        grid.addColumn(User::getId).setHeader("id");
        grid.addColumn(User::getUsername).setHeader("userName");
        grid.addColumn(User::getEmail).setHeader("email");
        grid.addColumn(User::getApprovedEmail).setHeader("approvedEmail");
        grid.addColumn(User::isEnabled).setHeader("enable");
        grid.addColumn(user -> (user.getGroup()!=null)?user.getGroup().getName():"").setHeader("group");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        add(grid);
    }


    @PostConstruct
    private void init() {
        grid.setItems(userDataProvider);
    }
}
