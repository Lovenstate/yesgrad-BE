package com.yesgrad.service.controller;

import com.yesgrad.service.domain.CommonResponse;
import com.yesgrad.service.domain.Subject;
import com.yesgrad.service.dto.SubjectNode;
import com.yesgrad.service.repository.SubjectRepository;
import com.yesgrad.service.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping("/{id}")
    public Mono<CommonResponse<Subject>> getSubjectById(@PathVariable Long id) {
        return subjectService.findSubjectById(id)
            .map(CommonResponse::success)
            .defaultIfEmpty(CommonResponse.error("NOT_FOUND", "Subject not found"));
    }

    @GetMapping("/tree")
    public Mono<CommonResponse<List<SubjectNode>>> getSubjectTree() {
        return subjectService.getSubjectTree()
            .map(CommonResponse::success)
            .defaultIfEmpty(CommonResponse.error("NOT_FOUND", "No subjects found"));
    }

    @GetMapping("/{parentId}/children")
    public Mono<CommonResponse<List<Subject>>> getChildren(@PathVariable Long parentId) {
        return subjectService.getImmediateChildren(parentId)
            .map(CommonResponse::success);
    }
}
