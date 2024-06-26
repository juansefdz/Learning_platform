package com.simulacro.aprendizaje.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.simulacro.aprendizaje.api.dto.request.LessonRequest;
import com.simulacro.aprendizaje.api.dto.response.LessonResponse.LessonResponse;
import com.simulacro.aprendizaje.infraestructure.abstract_services.ILessonService;
import com.simulacro.aprendizaje.utils.enums.SortType;
import com.simulacro.aprendizaje.utils.exceptions.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(path = "/lessons")
@AllArgsConstructor
@Tag(name = "Lesson Controller") // SWAGGER
public class LessonController {

    @Autowired
    private final ILessonService iLessonService;

    /*--------------
     * GET ALL
     * -------------
     */
     @Operation(
        summary = "Displays all Lessons",
        description = "Displays the lessons in a list, it is configured to display 10 items per page."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "SUCCESSFUL"),
        @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
        @ApiResponse(responseCode = "401", description = "NOT AUTHORIZED"),
        @ApiResponse(responseCode = "403", description = "FORBIDDEN ACCESS"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping
    public ResponseEntity<Page<LessonResponse>> getAll(
            @Parameter(description = "Page number (default: 0)", example = "0")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page (default: 10)", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(this.iLessonService.getAll(page - 1, size, SortType.NONE));
    }

    /*----------------
     * GET BY ID
     * ---------------
     */

    @Operation(
        summary = "Displays one lesson by id",
        description = "Shows the lesson by the ID sent or requested by path, value cannot be less than 1."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "SUCCESSFUL"),
        @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
        @ApiResponse(responseCode = "401", description = "NOT AUTHORIZED"),
        @ApiResponse(responseCode = "403", description = "FORBIDDEN ACCESS"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(path = "/{lesson_id}")
    public ResponseEntity<LessonResponse> getById(
        @Parameter(description = "Lesson ID", example = "1")
        @PathVariable Long lesson_id) {

         LessonResponse lesson = iLessonService.getById(lesson_id);
        if (lesson == null) {
            throw new ResourceNotFoundException("Lesson not found");
        }
        return ResponseEntity.ok(lesson);
    }
    /*------------------
     * CREATE
     * -----------------
     */
    @Operation(
        summary = "Creates a new lesson",
        description = "Creates a new lesson by entering the required data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "SUCCESSFUL"),
        @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
        @ApiResponse(responseCode = "401", description = "NOT AUTHORIZED"),
        @ApiResponse(responseCode = "403", description = "FORBIDDEN ACCESS"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(path = "/create")
    public ResponseEntity<LessonResponse> create(
            @Validated @RequestBody LessonRequest request) {
        return ResponseEntity.ok(this.iLessonService.create(request));
    }

    /*------------------
     * DELETE
     * -----------------
     */

    @Operation(
        summary = "Delete lesson by ID",
        description = "Deletes a lesson based on an ID sent by path, value cannot be less than 1"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "SUCCESSFUL"),
        @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
        @ApiResponse(responseCode = "401", description = "NOT AUTHORIZED"),
        @ApiResponse(responseCode = "403", description = "FORBIDDEN ACCESS"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })

    @DeleteMapping(path = "/{lesson_id}")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Lesson ID", example = "1")
        @PathVariable Long id) {
        this.iLessonService.delete(id);

        return ResponseEntity.noContent().build();
    }

    /*------------------------
     * UPDATE
     * -----------------------
     */

     @Operation(
        summary = "Update lesson by ID",
        description = "Updates a previously created lesson. The ID and the new modified parameters must be sent through the path, value cannot be less than 1"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "SUCCESSFUL"),
        @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
        @ApiResponse(responseCode = "401", description = "NOT AUTHORIZED"),
        @ApiResponse(responseCode = "403", description = "FORBIDDEN ACCESS"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(path = "/{lesson_id}")
    public ResponseEntity<LessonResponse> update(
            @Validated @RequestBody LessonRequest request, 
            @Parameter(description = "Lesson ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(this.iLessonService.update(request, id));
    }

}
