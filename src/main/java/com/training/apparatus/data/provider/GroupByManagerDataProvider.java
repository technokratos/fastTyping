package com.training.apparatus.data.provider;

import com.training.apparatus.data.dto.GroupDto;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.GroupRepository;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.shared.Registration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * @author Kulikov Denis
 * @since 28.10.2022
 */

public class GroupByManagerDataProvider implements DataProvider<GroupDto, Void> {

    private final Set<Registration> registrations = new HashSet<>();

    private final GroupRepository groupRepository;

    private final User manager;

    public GroupByManagerDataProvider(GroupRepository groupRepository, User manager) {
        this.groupRepository = groupRepository;
        this.manager = manager;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<GroupDto, Void> query) {
        return (int) groupRepository.countByManager(manager).longValue();
    }

    @Override
    public Stream<GroupDto> fetch(Query<GroupDto, Void> query) {
        List<Sort.Order> orders = query.getSortOrders().stream().map(it -> new Sort.Order(mapDirection(it), it.getSorted())).collect(Collectors.toList());
        Sort sort = Sort.by(orders);
        PageRequest pageable = PageRequest.of(query.getPage(), query.getPageSize(), sort);
        return groupRepository.findGroupDtoByManager(manager.getId(), pageable).stream();

    }

    private static Sort.Direction mapDirection(QuerySortOrder it) {
        return it.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    @Override
    public void refreshItem(GroupDto item) {
    }

    @Override
    public void refreshAll() {
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<GroupDto> listener) {
        Registration registration = new Registration() {
            @Override
            public void remove() {
                registrations.remove(this);
            }
        };
        registrations.add(registration);
        return registration;
    }
}
