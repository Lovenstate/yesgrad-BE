package com.yesgrad.service.service;

import com.yesgrad.service.domain.TutorAvailability;
import com.yesgrad.service.dto.AvailabilityRequest;
import com.yesgrad.service.dto.TutorAvailabilityRequest;
import com.yesgrad.service.repository.TutorAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorAvailabilityService {

    private final TutorAvailabilityRepository tutorAvailabilityRepository;
    private final TutorCompletionService tutorCompletionService;

    public Mono<Void> saveAvailabilities(Long tutorId, TutorAvailabilityRequest request) {
        return tutorAvailabilityRepository.deleteByTutorId(tutorId)
                .thenMany(Flux.fromIterable(request.availabilities()))
                .map(availabilityRequest -> {
                    TutorAvailability tutorAvailability = new TutorAvailability();
                    tutorAvailability.setTutorId(tutorId);
                    tutorAvailability.setDayOfWeek(availabilityRequest.dayOfWeek());
                    tutorAvailability.setStartTime(availabilityRequest.startTime());
                    tutorAvailability.setEndTime(availabilityRequest.endTime());
                    tutorAvailability.setIsAvailable(availabilityRequest.isAvailable());
                    return tutorAvailability;
                }).collectList()
                .flatMapMany(tutorAvailabilityRepository::saveAll)
                .then(tutorCompletionService.updateTutorCompletion(tutorId))
                .then();
    }

    public Mono<Void> saveAvailability(Long tutorId, AvailabilityRequest request) {
        TutorAvailability tutorAvailability = new TutorAvailability();
        tutorAvailability.setTutorId(tutorId);
        tutorAvailability.setDayOfWeek(request.dayOfWeek());
        tutorAvailability.setStartTime(request.startTime());
        tutorAvailability.setEndTime(request.endTime());
        tutorAvailability.setIsAvailable(request.isAvailable());
        return tutorAvailabilityRepository.save(tutorAvailability).then();
    }

    public Mono<List<TutorAvailability>> getAvailabilities(Long tutorId) {
        return tutorAvailabilityRepository.findByTutorId(tutorId).collectList();
    }

    public Mono<Void> deleteAvailability(Long tutorId, Long id) {
        return tutorAvailabilityRepository.deleteByTutorIdAndId(tutorId, id);
    }
}