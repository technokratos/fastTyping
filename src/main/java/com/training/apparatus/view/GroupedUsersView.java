package com.training.apparatus.view;

import com.training.apparatus.data.dto.UserDto;
import com.training.apparatus.data.entity.Group;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.service.GroupService;
import com.training.apparatus.data.service.UserService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

//@RolesAllowed("ROLE_BOSS")
@Slf4j
@PermitAll
@Route(value = "list", layout =  MainLayout.class)
@PageTitle("Grouped users")
public class GroupedUsersView extends VerticalLayout {
    private final Grid<UserDto> grid = new Grid<>(UserDto.class);

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    public GroupedUsersView() {
    }

    @PostConstruct
    private void init() {
        ComboBox<Group> groupComboBox = new ComboBox<>(getTranslation("common.groups"));
        User user = userService.getUser();
        List<Group> groupsByManager = groupService.getGroupsByManager(user);
        groupComboBox.setItems(groupsByManager);
        add(groupComboBox);
        groupComboBox.addValueChangeListener(event ->
                grid.setItems(userService.findUsersDtoByGroup(event.getValue().getId())));
        groupComboBox.setItemLabelGenerator(Group::getName);
        if (groupsByManager.size() > 0) {
            groupComboBox.setValue(groupsByManager.get(groupsByManager.size() - 1));
        }

        addClassName("list-view");
        setSizeFull();
        configureGrid();
        add(grid);
    }

    private void configureGrid() {
        grid.addClassNames("contact-grid");
        grid.setSizeFull();
        grid.setColumns("pseudonym", "email", "avgMistakes", "avgSpeed", "count");
        grid.getColumnByKey("count").setHeader("Пройдено курсов");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

}
