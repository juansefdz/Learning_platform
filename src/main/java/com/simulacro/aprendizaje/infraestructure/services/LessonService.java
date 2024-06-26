package com.simulacro.aprendizaje.infraestructure.services;



import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.simulacro.aprendizaje.api.dto.request.LessonRequest;
import com.simulacro.aprendizaje.api.dto.response.LessonResponse.AssignmentResponseInLesson;
import com.simulacro.aprendizaje.api.dto.response.LessonResponse.CourseResponseInLesson;
import com.simulacro.aprendizaje.api.dto.response.LessonResponse.LessonResponse;
import com.simulacro.aprendizaje.api.dto.response.LessonResponse.UserResponseInLesson;
import com.simulacro.aprendizaje.domain.entities.Assignment;
import com.simulacro.aprendizaje.domain.entities.Course;

import com.simulacro.aprendizaje.domain.entities.Lesson;
import com.simulacro.aprendizaje.domain.entities.UserEntity;
import com.simulacro.aprendizaje.domain.repositories.LessonRepository;
import com.simulacro.aprendizaje.infraestructure.abstract_services.ILessonService;
import com.simulacro.aprendizaje.utils.enums.SortType;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LessonService implements ILessonService {

    @Autowired
    private final LessonRepository lessonRepository;

    @Override
    public Page<LessonResponse> getAll(int page, int size, SortType sortType) {
        if (page < 0)
            page = 0;

        PageRequest pagination = PageRequest.of(page, size);
        return this.lessonRepository.findAll(pagination).map(this::entityToResponse);
    }

    @Override
    public LessonResponse getById(Long id) {
        Lesson lesson = find(id);
        return entityToResponse(lesson);
    }

    @Override
    public LessonResponse create(LessonRequest request) {
        Lesson lesson = this.requestToEntity(request);
        return this.entityToResponse(this.lessonRepository.save(lesson));
    }

    @Override
    public LessonResponse update(LessonRequest request, Long id) {
        Lesson lesson = this.find(id);

        if (request.getLessonTitle() != null) {
            lesson.setLessonTitle(request.getLessonTitle());
        }

        if (request.getContent() != null) {
            lesson.setContent(request.getContent());
        }

        return this.entityToResponse(this.lessonRepository.save(lesson));
    }


    @Override
    public void delete(Long id) {
        this.lessonRepository.delete(this.find(id));
    }

    private Lesson find(Long id) {
        return this.lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User ID not found with this ID: " + id));
    }

    private LessonResponse entityToResponse(Lesson lesson) {
        LessonResponse lessonResponse = LessonResponse.builder()
            .idLesson(lesson.getIdLesson())
            .lessonTitle(lesson.getLessonTitle())
            .content(lesson.getContent())
            .build();
        
        if (lesson.getCourse() != null) {
            lessonResponse.setCourses(courseResponseInLesson(lesson.getCourse()));
        }
        
        if (lesson.getUser() != null) {
            lessonResponse.setUsers(userResponseInLesson(List.of(lesson.getUser())));
        }
        
        if (lesson.getAssignments() != null ) {
            lessonResponse.setAssignments(assignmentResponseInLesson(lesson.getAssignments()));
        }
        
        return lessonResponse;
    }

    private Lesson requestToEntity(LessonRequest request) {
        Lesson lesson = new Lesson();
        BeanUtils.copyProperties(request, lesson);
        return lesson;
    }


   
    private CourseResponseInLesson courseResponseInLesson(Course course) {
        CourseResponseInLesson courseResponseInLesson = new CourseResponseInLesson();
        courseResponseInLesson.setIdCourse(course.getIdCourse());
        courseResponseInLesson.setCourseName(course.getCourseName());
        courseResponseInLesson.setDescription(course.getDescription());
        courseResponseInLesson.setInstructorId(course.getInstructor().getIdUser());
        courseResponseInLesson.setInstructorName(course.getInstructor().getFullName());
        return courseResponseInLesson;
    }

    private List<AssignmentResponseInLesson> assignmentResponseInLesson(List<Assignment> assignments) {
        return assignments.stream()
                .map(assignment -> {
                    AssignmentResponseInLesson assignmentResponseInLesson = new AssignmentResponseInLesson();
                    assignmentResponseInLesson.setIdAssigment(assignment.getIdAssignment());
                    assignmentResponseInLesson.setAssignmentTitle(assignment.getAssignmentTitle());
                    assignmentResponseInLesson.setDescription(assignment.getDescription());
                    return assignmentResponseInLesson;
                })
                .collect(Collectors.toList());
    }

    private List<UserResponseInLesson> userResponseInLesson(List<UserEntity> users) {
        return users.stream()
            .map(user -> {
                UserResponseInLesson userResponseInLesson = new UserResponseInLesson();
                userResponseInLesson.setIdUser(user.getIdUser());
                userResponseInLesson.setFullName(user.getFullName());
                userResponseInLesson.setEmail(user.getEmail());
                userResponseInLesson.setUserName(user.getUserName());
                return userResponseInLesson;
            })
            .collect(Collectors.toList());
    }







}
