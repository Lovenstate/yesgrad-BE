package com.yesgrad.service.controller;

import com.yesgrad.service.domain.CommonResponse;
import com.yesgrad.service.domain.Subject;
import com.yesgrad.service.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository subjectRepository;

    @GetMapping
    public Mono<CommonResponse<Flux<Subject>>> getAllSubjects() {
        return Mono.just(CommonResponse.success(subjectRepository.findByIsActiveTrue()));
    }

    @GetMapping("/{id}")
    public Mono<CommonResponse<Subject>> getSubjectById(@PathVariable Long id) {
        return subjectRepository.findById(id)
            .map(CommonResponse::success)
            .defaultIfEmpty(CommonResponse.error("NOT_FOUND", "Subject not found"));
    }
}
