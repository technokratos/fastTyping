package com.training.apparatus.view;

import com.training.apparatus.data.dto.GroupDto;
import com.training.apparatus.data.dto.UserDto;
import com.training.apparatus.data.entity.Group;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.provider.GroupByManagerDataProvider;
import com.training.apparatus.data.repo.GroupRepository;
import com.training.apparatus.data.service.GroupService;
import com.training.apparatus.data.service.UserService;
import com.training.apparatus.secutiy.SecurityService;
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
    private final Grid<UserDto> userGrid = new Grid<>(20);
    private final Grid<GroupDto> groupGrid = new Grid<>(20);

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private RegistrationGroupView registrationGroupView;

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
                userGrid.setItems(userService.findUsersDtoByGroup(event.getValue().getId())));
        groupComboBox.setItemLabelGenerator(Group::getName);
        if (groupsByManager.size() > 0) {
            groupComboBox.setValue(groupsByManager.get(groupsByManager.size() - 1));
        }
        addClassName("list-view");
        setSizeFull();
        configureUserGrid();
        configureGroupGrid();
        add(userGrid);
        add(groupGrid);
        add(registrationGroupView);
        registrationGroupView.addSaveListener((group) -> {
            groupComboBox.setItems(groupService.getGroupsByManager(user));
            groupGrid.getDataProvider().refreshAll();
        });

    }

    private void configureUserGrid() {
        userGrid.addClassNames("contact-grid");
        userGrid.setSizeFull();
        userGrid.addColumn(UserDto::getPseudonym).setHeader(getTranslation("group.pseudonym"));
        userGrid.addColumn(UserDto::getEmail).setHeader(getTranslation("registration.email"));
        userGrid.addColumn(UserDto::getAvgMistakes).setHeader(getTranslation("group.averageMistakes"));
        userGrid.addColumn(UserDto::getAvgSpeed).setHeader(getTranslation("group.averageSpeed"));
        userGrid.addColumn(UserDto::getCount).setHeader(getTranslation("group.attempts"));
        userGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void configureGroupGrid() {

        groupGrid.setItems(new GroupByManagerDataProvider(groupRepository, securityService.getAuthUser()));
        groupGrid.addClassNames("contact-grid");
        groupGrid.setSizeFull();
        groupGrid.addColumn(GroupDto::getName).setHeader(getTranslation("group.name"));
        groupGrid.addColumn(GroupDto::getLink).setHeader(getTranslation("group.link"));
        groupGrid.addColumn(GroupDto::getCount).setHeader(getTranslation("group.count"));
        groupGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

}
