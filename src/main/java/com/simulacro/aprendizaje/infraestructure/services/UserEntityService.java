package com.simulacro.aprendizaje.infraestructure.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.simulacro.aprendizaje.api.dto.request.UserRequest;
import com.simulacro.aprendizaje.api.dto.response.AssignmentResponse.AssignmentResponse;
import com.simulacro.aprendizaje.api.dto.response.AssignmentResponse.LessonsResponseInAssignment;
import com.simulacro.aprendizaje.api.dto.response.MessageResponse.MessageResponse;
import com.simulacro.aprendizaje.api.dto.response.UserResponse.CourseResponseInUser;
import com.simulacro.aprendizaje.api.dto.response.UserResponse.EnrollmentResponseInUser;
import com.simulacro.aprendizaje.api.dto.response.UserResponse.LessonResponseInUser;
import com.simulacro.aprendizaje.api.dto.response.UserResponse.SubmissionResponseInUser;
import com.simulacro.aprendizaje.api.dto.response.UserResponse.UserResponse;
import com.simulacro.aprendizaje.domain.entities.Assignment;
import com.simulacro.aprendizaje.domain.entities.Course;
import com.simulacro.aprendizaje.domain.entities.Enrollment;
import com.simulacro.aprendizaje.domain.entities.Lesson;
import com.simulacro.aprendizaje.domain.entities.Message;
import com.simulacro.aprendizaje.domain.entities.Submission;
import com.simulacro.aprendizaje.domain.entities.UserEntity;
import com.simulacro.aprendizaje.domain.repositories.UserRepository;
import com.simulacro.aprendizaje.infraestructure.abstract_services.IUserEntityService;
import com.simulacro.aprendizaje.utils.enums.SortType;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserEntityService implements IUserEntityService {

    @Autowired
    private final UserRepository userRepository;

    @Override
    public Page<UserResponse> getAll(int page, int size, SortType sortType) {
        if (page < 0) page = 0;
        PageRequest pagination = PageRequest.of(page, size);
        return this.userRepository.findAll(pagination).map(this::entityToResponse);
    }

    @Override
    public UserResponse create(UserRequest request) {
        UserEntity user = this.requestToEntity(request);
        return this.entityToResponse(this.userRepository.save(user));
    }

    @Override
    public UserResponse update(UserRequest request, Long id) {
        UserEntity user = this.find(id);
        if (request.getUserName() != null) user.setUserName(request.getUserName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPassword() != null) user.setPassword(request.getPassword());
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        return this.entityToResponse(this.userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        this.userRepository.delete(this.find(id));
    }

    @Override
    public UserResponse getById(Long id) {
        UserEntity user = find(id);
        return entityToResponse(user);
    }

    private UserEntity find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private UserResponse entityToResponse(UserEntity user) {
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);

        userResponse.setCourses(user.getCourses().stream()
                .map(this::courseToResponse)
                .collect(Collectors.toList()));

        userResponse.setEnrollments(enrollmentResponseInUser(user.getEnrollments()));
        userResponse.setLessons(lessonsToResponse(user.getLessons()));
        userResponse.setSubmissions(submissionsToResponses(user.getSubmissions()));
        userResponse.setSentMessages(messagesToResponses(user.getSentMessages()));
        userResponse.setReceivedMessages(messagesToResponses(user.getReceivedMessages()));

        return userResponse;
    }

    private List<EnrollmentResponseInUser> enrollmentResponseInUser(List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(enrollment -> {
                    EnrollmentResponseInUser enrollmentResponseInUser = new EnrollmentResponseInUser();
                    enrollmentResponseInUser.setIdEnrollment(enrollment.getIdEnrollment());
                    enrollmentResponseInUser.setEnrollmentDate(enrollment.getEnrollmentDate());
                    return enrollmentResponseInUser;
                })
                .collect(Collectors.toList());
    }

    private CourseResponseInUser courseToResponse(Course course) {
        CourseResponseInUser courseResponseInUser = new CourseResponseInUser();
        courseResponseInUser.setIdCourse(course.getIdCourse());
        courseResponseInUser.setCourseName(course.getCourseName());
        courseResponseInUser.setDescription(course.getDescription());
        courseResponseInUser.setIdInstructor(course.getInstructor().getIdUser());
        return courseResponseInUser;
    }

    private List<MessageResponse> messagesToResponses(List<Message> messages) {
        return messages.stream()
                .map(message -> {
                    MessageResponse messageResponse = new MessageResponse();
                    BeanUtils.copyProperties(message, messageResponse);
                    messageResponse.setMessageId(message.getIdMessage());
                    messageResponse.setSenderId(message.getSender().getIdUser());
                    messageResponse.setReceiverId(message.getReceiver().getIdUser());
                    if (message.getCourse() != null) {
                        messageResponse.setCourseId(message.getCourse().getIdCourse());
                    } else {
                        messageResponse.setCourseId(null); 
                    }
                    messageResponse.setDate(message.getSentDate());
                    return messageResponse;
                })
                .collect(Collectors.toList());
    }

    private List<LessonResponseInUser> lessonsToResponse(List<Lesson> lessons) {
        return lessons.stream()
                .map(lesson -> {
                    LessonResponseInUser lessonResponseInUser = new LessonResponseInUser();
                    lessonResponseInUser.setIdLesson(lesson.getIdLesson());
                    lessonResponseInUser.setLessonTitle(lesson.getLessonTitle());
                    lessonResponseInUser.setContent(lesson.getContent());
                    lessonResponseInUser.setAssignments(assignmentToResponse(lesson.getAssignments()));
                    return lessonResponseInUser;
                })
                .collect(Collectors.toList());
    }

    private List<SubmissionResponseInUser> submissionsToResponses(List<Submission> submissions) {
        return submissions.stream()
                .map(submission -> {
                    SubmissionResponseInUser submissionResponseInUser = new SubmissionResponseInUser();
                    BeanUtils.copyProperties(submission, submissionResponseInUser);
                    return submissionResponseInUser;
                })
                .collect(Collectors.toList());
    }

    private List<AssignmentResponse> assignmentToResponse(List<Assignment> assignments) {
        return assignments.stream()
                .map(assignment -> {
                    AssignmentResponse assignmentResponse = new AssignmentResponse();
                    assignmentResponse.setIdAssignment(assignment.getIdAssignment());
                    assignmentResponse.setAssignmentTitle(assignment.getAssignmentTitle());
                    assignmentResponse.setDescription(assignment.getDescription());
                    assignmentResponse.setDueDateAssignment(assignment.getDueDateAssignment());
                    assignmentResponse.setLessons(lessonsResponseInAssignments(List.of(assignment.getLesson())));
                    return assignmentResponse;
                })
                .collect(Collectors.toList());
    }

    private List<LessonsResponseInAssignment> lessonsResponseInAssignments(List<Lesson> lessons) {
        return lessons.stream()
                .map(lesson -> {
                    LessonsResponseInAssignment lessonsResponseInAssignment = new LessonsResponseInAssignment();
                    lessonsResponseInAssignment.setLessonId(lesson.getIdLesson());
                    lessonsResponseInAssignment.setLessonTitle(lesson.getLessonTitle());
                    lessonsResponseInAssignment.setContent(lesson.getContent());
                    lessonsResponseInAssignment.setCourseId(lesson.getCourse().getIdCourse());
                    return lessonsResponseInAssignment;
                })
                .collect(Collectors.toList());
    }

    private UserEntity requestToEntity(UserRequest request) {
        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(request, user);
        return user;
    }

}
