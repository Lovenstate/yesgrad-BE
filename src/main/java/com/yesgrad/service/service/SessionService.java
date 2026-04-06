package com.yesgrad.service.service;

import com.yesgrad.service.domain.Session;
import com.yesgrad.service.dto.BookingSessionRequest;
import com.yesgrad.service.dto.SessionResponse;
import com.yesgrad.service.enums.LessonStatus;
import com.yesgrad.service.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public Mono<List<SessionResponse>> getTutorSessions(Long tutorId, String status) {
        Flux<SessionResponse> sessions = status != null
                ? sessionRepository.findByTutorIdAndStatusWithDetails(tutorId, status)
                : sessionRepository.findByTutorIdWithDetails(tutorId);
        return sessions.collectList();
    }

    public Mono<List<SessionResponse>> getStudentSessions(Long studentId, String status) {
        Flux<SessionResponse> sessions = status != null
                ? sessionRepository.findByStudentIdAndStatusWithDetails(studentId, status)
                : sessionRepository.findByStudentIdWithDetails(studentId);
        return sessions.collectList();
    }

    public Mono<SessionResponse> getSession(Long sessionId) {
        return sessionRepository.findByIdWithDetails(sessionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    public Mono<SessionResponse> bookSession(Long profileId, String role, BookingSessionRequest request) {
        var session = new Session();
        if (role.equals("TUTOR")) {
            session.setTutorId(profileId);
            session.setStudentId(request.studentId());
            session.setStatus(LessonStatus.CONFIRMED);
        } else {
            session.setStudentId(profileId);
            session.setTutorId(request.tutorId());
            session.setStatus(LessonStatus.SCHEDULED);
        }

        session.setSubjectId(request.subjectId());
        session.setSessionDate(request.sessionDate());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setDurationMinutes(request.durationMinutes());
        session.setHourlyRate(request.hourlyRate());
        session.setAmount(request.amount());
        session.setLessonFormat(request.lessonFormat());
        session.setLocation(request.location());
        session.setNotes(request.notes());
        session.setCreatedAt(LocalDateTime.now());

        return sessionRepository.save(session)
                .flatMap(saved -> sessionRepository.findByIdWithDetails(saved.getId()));
    }

    public Mono<SessionResponse> updateStatus(Long sessionId, String status, String reason) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found")))
                .flatMap(session -> {
                    session.setStatus(LessonStatus.valueOf(status));
                    session.setUpdatedAt(LocalDateTime.now());
                    if (reason != null) session.setCancellationReason(reason);
                    return sessionRepository.save(session);
                })
                .flatMap(saved -> sessionRepository.findByIdWithDetails(saved.getId()));
    }
}
