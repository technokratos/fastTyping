package com.training.apparatus.data.component;

import com.training.apparatus.data.dto.LessonDto;
import com.training.apparatus.data.entity.Language;
import com.training.apparatus.data.entity.Task;
import com.training.apparatus.data.entity.Type;
import com.training.apparatus.data.repo.TaskRepository;
import com.training.apparatus.data.service.LessonUtils;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kulikov Denis
 * @since 09.10.2022
 */
@Component
public class LoadLessonsOnStartup {

    public static final String BASIC_LESSONS = "Basic Lessons";
    public static final String BASIC_LESSONS_RUS = "Базовые уроки";
    @Autowired
    private TaskRepository taskRepository;


    @PostConstruct
    @Transactional
    void init() {
        fillLanguageByLesson(Language.Russian, BASIC_LESSONS_RUS);
        fillLanguageByLesson(Language.English, BASIC_LESSONS);
    }

    private void fillLanguageByLesson(Language language, String lessonsTitle) {
        List<LessonDto> lessonDtos = LessonUtils.getByLanguage(language);

        Long count = taskRepository.countByTypeAndLanguage(Type.Basic, language);

        if (count == 0) {
            List<LessonDto> lessons = lessonDtos.stream()
                    .filter(lessonDto -> lessonDto.getGroup().equals(lessonsTitle))
                    .toList();
            for (int i = 0; i < lessons.size(); i++) {
                LessonDto lesson = lessons.get(i);
                Task task = new Task();
                task.setType(Type.Basic);
                task.setLanguage(language);
                task.setNumber((long) i);
                task.setTitle(lesson.getTitle());
                task.setText(lesson.getContent());
                taskRepository.saveAndFlush(task);
            }

        }
    }
}
