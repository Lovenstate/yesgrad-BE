package com.yesgrad.service.service;

import com.yesgrad.service.domain.Subject;
import com.yesgrad.service.dto.SubjectNode;
import com.yesgrad.service.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public Mono<Subject> findSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    public Mono<List<SubjectNode>> getSubjectTree() {
        return subjectRepository.findAll()
                .collectList()
                .map(this::buildTree);
    }

    public Mono<List<Subject>> getImmediateChildren(Long parentId) {
        return subjectRepository.findByParentId(parentId)
                .collectList();
    }

    private List<SubjectNode> buildTree(List<Subject> subjects) {
        Map<Long, List<Subject>> grouped =
                subjects.stream()
                        .collect(Collectors.groupingBy(
                                subject -> subject.getParentId() == null ? 0L : subject.getParentId()
                        ));

        return buildChildren(0L, grouped);
    }

    private List<SubjectNode> buildChildren(long parentId, Map<Long, List<Subject>> grouped) {
        return grouped.getOrDefault(parentId, List.of())
                .stream()
                .map(subject -> new SubjectNode(
                        subject.getId(),
                        subject.getName(),
                        buildChildren(subject.getId(), grouped)
                ))
                .collect(Collectors.toList());

    }
}
