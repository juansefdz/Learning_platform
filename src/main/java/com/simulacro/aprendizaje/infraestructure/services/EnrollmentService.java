package com.simulacro.aprendizaje.infraestructure.services;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simulacro.aprendizaje.api.dto.request.EnrollmentRequest;
import com.simulacro.aprendizaje.api.dto.response.CourseResponse.CourseResponse;
import com.simulacro.aprendizaje.api.dto.response.EnrollmentResponse.CourseResponseInEnrolment;
import com.simulacro.aprendizaje.api.dto.response.EnrollmentResponse.EnrollmentResponse;
import com.simulacro.aprendizaje.api.dto.response.EnrollmentResponse.UserResponseInEnrolment;
import com.simulacro.aprendizaje.api.dto.response.UserResponse.UserResponse;
import com.simulacro.aprendizaje.domain.entities.Enrollment;
import com.simulacro.aprendizaje.domain.entities.UserEntity;
import com.simulacro.aprendizaje.domain.entities.Course;
import com.simulacro.aprendizaje.domain.repositories.EnrrollmentRepository;
import com.simulacro.aprendizaje.domain.repositories.UserRepository;
import com.simulacro.aprendizaje.domain.repositories.CourseRepository;
import com.simulacro.aprendizaje.infraestructure.abstract_services.IEnrrollmentService;
import com.simulacro.aprendizaje.utils.enums.SortType;

import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class EnrollmentService implements IEnrrollmentService {

    @Autowired
    private EnrrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public Page<EnrollmentResponse> getAll(int page, int size, SortType sortType) {
        if (page < 0) page = 0;
        PageRequest pagination = PageRequest.of(page, size);
        return this.enrollmentRepository.findAll(pagination).map(this::entityToResponse);
    }

    @Override
    public EnrollmentResponse getById(Long id) {
       Enrollment enrollment = find (id);
       return entityToResponse(enrollment);
    }

    @Override
    public EnrollmentResponse create(EnrollmentRequest request) {
        Enrollment enrollment = this.requestToEntity(request);
        return this.entityToResponse(this.enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponse update(EnrollmentRequest request, Long id) {
        Enrollment enrollment = this.find(id);
        enrollment = this.requestToEntity(request);
        enrollment.setIdEnrollment(id);
        return this.entityToResponse(this.enrollmentRepository.save(enrollment));
    }

    @Override
    public void delete(Long id) {
        this.enrollmentRepository.delete(this.find(id));
    }

    private Enrollment find(Long id) {
        return this.enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment ID not found with this ID: " + id));
    }

    private Enrollment requestToEntity(EnrollmentRequest request) {
        Enrollment enrollment = new Enrollment();

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + request.getCourseId()));

        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(request.getEnrollmentDate());

        return enrollment;
    }

    private EnrollmentResponse entityToResponse(Enrollment enrollment) {
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(enrollment.getUser(), userResponse);
        CourseResponse courseResponse = new CourseResponse();
        BeanUtils.copyProperties(enrollment.getCourse(), courseResponse);
       

        return EnrollmentResponse.builder()
                .idEnrollment(enrollment.getIdEnrollment())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .user(userResponseInEnrolment(enrollment.getUser()))
                .course(courseResponseInEnrolment(enrollment.getCourse()))
                .build();
    }


    private CourseResponseInEnrolment courseResponseInEnrolment(Course course) {
        CourseResponseInEnrolment courseResponseInEnrolment = new CourseResponseInEnrolment();
        courseResponseInEnrolment.setCourseId(course.getIdCourse());
        courseResponseInEnrolment.setCourseName(course.getCourseName());
        courseResponseInEnrolment.setDescription(course.getDescription());
        courseResponseInEnrolment.setInstructorId(course.getInstructor().getIdUser());
        return courseResponseInEnrolment;
    }

      private UserResponseInEnrolment userResponseInEnrolment(UserEntity users) {
        
                UserResponseInEnrolment userResponseInEnrollment = new UserResponseInEnrolment();
                BeanUtils.copyProperties(users, userResponseInEnrollment);
                return userResponseInEnrollment;
         
           
    }

}
