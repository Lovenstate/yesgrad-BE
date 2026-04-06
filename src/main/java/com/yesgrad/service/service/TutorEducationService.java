package com.yesgrad.service.service;

import com.yesgrad.service.domain.TutorEducation;
import com.yesgrad.service.dto.TutorEducationRequest;
import com.yesgrad.service.repository.TutorEducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorEducationService {

    private final TutorEducationRepository tutorEducationRepository;
    private final TutorCompletionService tutorCompletionService;

    public Mono<Void> saveEducation(
            Long tutorId,
            TutorEducationRequest request) {

        return tutorEducationRepository.deleteByTutorId(tutorId)
                .thenMany(
                        Flux.fromIterable(request.educations())
                                .map(educationRequest -> {
                                    TutorEducation edu = new TutorEducation();
                                    edu.setTutorId(tutorId);
                                    edu.setSchool(educationRequest.school());
                                    edu.setDegree(educationRequest.degree());
                                    edu.setFieldOfStudy(educationRequest.fieldOfStudy());
                                    edu.setGraduationYear(educationRequest.graduationYear());
                                    return edu;
                                })
                ).collectList()
                .flatMapMany(tutorEducationRepository::saveAll)
                .then(tutorCompletionService.updateTutorCompletion(tutorId))
                .then();
    }

    public Mono<List<TutorEducation>> getEducations(Long tutorId) {
        return tutorEducationRepository.findByTutorId(tutorId).collectList();
    }

    public Mono<Void> deleteEducation(Long tutorId, Long id) {
        return tutorEducationRepository.deleteByTutorIdAndId(tutorId, id);
    }
}
