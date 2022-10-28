package com.training.apparatus.data.provider;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.UserRepository;
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
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * @author Kulikov Denis
 * @since 28.10.2022
 */
@Component
public class UserDataProvider implements DataProvider<User, Void> {

    private final Set<Registration> registrations = new HashSet<>();
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;
    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<User, Void> query) {
        return (int) userRepository.count();
    }

    @Override
    public Stream<User> fetch(Query<User, Void> query) {
        List<Sort.Order> orders = query.getSortOrders().stream().map(it -> new Sort.Order(mapDirection(it), it.getSorted())).collect(Collectors.toList());
        Sort sort = Sort.by(orders);
        PageRequest pageable = PageRequest.of(query.getPage(), query.getPageSize(), sort);
        return userRepository.findAll(pageable).stream();

    }

    private static Sort.Direction mapDirection(QuerySortOrder it) {
        return it.getDirection() == SortDirection.ASCENDING? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    @Override
    public void refreshItem(User item) {
       entityManager.refresh(item);
    }

    @Override
    public void refreshAll() {

    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<User> listener) {
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
