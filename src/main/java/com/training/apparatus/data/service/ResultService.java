package com.training.apparatus.data.service;

import com.training.apparatus.data.entity.Result;
import com.training.apparatus.data.entity.Task;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.ResultRepository;
import com.training.apparatus.data.repo.TaskRepository;
import com.training.apparatus.data.repo.UserRepository;
import com.training.apparatus.secutiy.SecurityService;
import java.time.LocalDateTime;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResultService {
    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TaskRepository taskRepository;

    public ResultService() {

    }

    public Result create(long count, long length, double min) {
        Result result = new Result();
        result.setMistakes((double)length / count * 100);
        result.setSpeed((int)(count / min));
        result.setTime(LocalDateTime.now());
        return result;
    }

    @Transactional
    public Result save(Task task, Double mistakes, double speed, int length, User user) {
        Result result = new Result();
        result.setMistakes(mistakes);
        result.setSpeed((int) speed);
        result.setTime(LocalDateTime.now());
        result.setTotalSymbols(length);
        result.setTask(task);
        result.setUser(user);
        return resultRepository.save(result);
    }
}
