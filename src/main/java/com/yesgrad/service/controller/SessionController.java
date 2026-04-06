package com.yesgrad.service.controller;

import com.yesgrad.service.domain.CommonResponse;
import com.yesgrad.service.dto.AvailabilitySlot;
import com.yesgrad.service.dto.BookingSessionRequest;
import com.yesgrad.service.dto.CancelSessionRequest;
import com.yesgrad.service.dto.SessionResponse;
import com.yesgrad.service.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/tutor")
    public Mono<CommonResponse<List<SessionResponse>>> getTutorSessions(
            @RequestAttribute("profileId") Long tutorId,
            @RequestParam(required = false) String status
    ) {
        return sessionService.getTutorSessions(tutorId, status)
                .map(sessions -> CommonResponse.success("Sessions retrieved", sessions));
    }

    @GetMapping("/student/{studentId}")
    public Mono<CommonResponse<List<SessionResponse>>> getStudentSessions(
            @PathVariable Long studentId,
            @RequestParam(required = false) String status
    ) {
        return sessionService.getStudentSessions(studentId, status)
                .map(sessions -> CommonResponse.success("Sessions retrieved", sessions));
    }

    @GetMapping("/{sessionId}")
    public Mono<CommonResponse<SessionResponse>> getSession(@PathVariable Long sessionId) {
        return sessionService.getSession(sessionId)
                .map(session -> CommonResponse.success("Session retrieved", session));
    }

    @PostMapping
    public Mono<CommonResponse<SessionResponse>> bookSession(
            @RequestAttribute("profileId") Long profileId,
            @RequestAttribute("role") String role,
            @RequestBody BookingSessionRequest request
    ) {
        return sessionService.bookSession(profileId, role, request)
                .map(session -> CommonResponse.success("Session booked", session));
    }

    @PutMapping("/{sessionId}/confirm")
    public Mono<CommonResponse<SessionResponse>> confirmSession(@PathVariable Long sessionId) {
        return sessionService.updateStatus(sessionId, "CONFIRMED", null)
                .map(session -> CommonResponse.success("Session confirmed", session));
    }

    @PutMapping("/{sessionId}/cancel")
    public Mono<CommonResponse<SessionResponse>> cancelSession(
            @PathVariable Long sessionId,
            @RequestBody CancelSessionRequest request
    ) {
        return sessionService.updateStatus(sessionId, "CANCELLED", request.reason())
                .map(session -> CommonResponse.success("Session cancelled", session));
    }

    @PutMapping("/{sessionId}/complete")
    public Mono<CommonResponse<SessionResponse>> completeSession(@PathVariable Long sessionId) {
        return sessionService.updateStatus(sessionId, "COMPLETED", null)
                .map(session -> CommonResponse.success("Session completed", session));
    }

    @PutMapping("/{sessionId}/decline")
    public Mono<CommonResponse<SessionResponse>> declineSession(@PathVariable Long sessionId) {
        return sessionService.updateStatus(sessionId, "DECLINED", null)
                .map(session -> CommonResponse.success("Session declined", session));
    }


//    // GET /api/tutors/{tutorId}/availability - Get tutor availability
//    @GetMapping("/tutors/{tutorId}/availability")
//    public Mono<CommonResponse<List<AvailabilitySlot>>> getTutorAvailability(
//            @PathVariable Long tutorId,
//            @RequestParam String date // YYYY-MM-DD
//    );
}
