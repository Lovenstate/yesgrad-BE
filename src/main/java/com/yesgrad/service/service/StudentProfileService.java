package com.yesgrad.service.service;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.dto.OnboardingStatusResponse;
import com.yesgrad.service.dto.StudentDashboardResponse;
import com.yesgrad.service.dto.StudentProfileResponse;
import com.yesgrad.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentSubjectRepository studentSubjectRepository;
    private final StudentLanguageRepository studentLanguageRepository;
    private final StudentAvailabilityRepository studentAvailabilityRepository;
    private final SubjectRepository subjectRepository;
    private final SessionRepository sessionRepository;
    private final MessageService messageService;

    public Mono<StudentProfile> getStudentProfile(Long studentId) {
        return studentProfileRepository.findById(studentId);
    }

    public Mono<StudentProfile> getProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId);
    }

    public Mono<StudentProfile> saveStudentProfile(StudentProfile profile) {
        return studentProfileRepository.save(profile);
    }

    public Mono<StudentProfileResponse> getFullProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .flatMap(profile -> Mono.zip(
                        studentSubjectRepository.findByStudentId(profile.getId()).collectList(),
                        studentLanguageRepository.findByStudentId(profile.getId()).collectList(),
                        studentAvailabilityRepository.findByStudentId(profile.getId()).collectList()
                ).flatMap(tuple -> {
                    List<StudentSubject> subjects = tuple.getT1();
                    List<StudentLanguage> languages = tuple.getT2();
                    List<StudentAvailability> availability = tuple.getT3();

                    Mono<List<StudentProfileResponse.StudentSubjectResponse>> subjectResponsesMono = subjects.isEmpty()
                            ? Mono.just(List.of())
                            : Flux.fromIterable(subjects)
                                    .flatMap(s -> subjectRepository.findById(s.getSubjectId())
                                            .map(subject -> StudentProfileResponse.StudentSubjectResponse.builder()
                                                    .id(s.getId())
                                                    .subjectId(s.getSubjectId())
                                                    .subjectName(subject.getName())
                                                    .level(s.getLevel())
                                                    .build())
                                            .defaultIfEmpty(StudentProfileResponse.StudentSubjectResponse.builder()
                                                    .id(s.getId())
                                                    .subjectId(s.getSubjectId())
                                                    .level(s.getLevel())
                                                    .build()))
                                    .collectList();

                    List<StudentProfileResponse.StudentLanguageResponse> languageResponses = languages.stream()
                            .map(l -> StudentProfileResponse.StudentLanguageResponse.builder()
                                    .id(l.getId())
                                    .language(l.getLanguage())
                                    .proficiency(l.getProficiency())
                                    .build())
                            .toList();

                    List<StudentProfileResponse.StudentAvailabilityResponse> availabilityResponses = availability.stream()
                            .map(a -> StudentProfileResponse.StudentAvailabilityResponse.builder()
                                    .id(a.getId())
                                    .dayOfWeek(a.getDayOfWeek())
                                    .startTime(a.getStartTime().toString())
                                    .endTime(a.getEndTime().toString())
                                    .build())
                            .toList();

                    return subjectResponsesMono.map(subjectResponses ->
                            StudentProfileResponse.builder()
                                    .id(profile.getId())
                                    .userId(profile.getUserId())
                                    .bio(profile.getBio())
                                    .gradeLevel(profile.getGradeLevel())
                                    .school(profile.getSchool())
                                    .learningGoals(profile.getLearningGoals())
                                    .learningStyle(profile.getLearningStyle())
                                    .timezone(profile.getTimezone())
                                    .budgetMin(profile.getBudgetMin())
                                    .budgetMax(profile.getBudgetMax())
                                    .lessonFormat(profile.getLessonFormat() != null ? profile.getLessonFormat().name() : null)
                                    .onboardingStatus(profile.getOnboardingStatus())
                                    .profileCompletion(profile.getProfileCompletion())
                                    .subjects(subjectResponses)
                                    .languages(languageResponses)
                                    .availability(availabilityResponses)
                                    .build());
                }));
    }

    public Mono<StudentProfile> saveBasicInfo(Long userId, BasicInfoRequest request) {
        return studentProfileRepository.findByUserId(userId)
                .flatMap(profile -> {
                    profile.setBio(request.bio());
                    profile.setGradeLevel(request.gradeLevel());
                    profile.setSchool(request.school());
                    profile.setLearningGoals(request.learningGoals());
                    profile.setLearningStyle(request.learningStyle());
                    profile.setTimezone(request.timezone());
                    profile.setBudgetMin(request.budgetMin());
                    profile.setBudgetMax(request.budgetMax());
                    profile.setLessonFormat(request.lessonFormat());
                    profile.setUpdatedAt(LocalDateTime.now());
                    return studentProfileRepository.save(profile)
                            .flatMap(this::recalculateCompletion);
                });
    }

    public Mono<List<StudentSubject>> saveSubjects(Long userId, List<SubjectRequest> requests) {
        return studentProfileRepository.findByUserId(userId)
                .flatMap(profile -> studentSubjectRepository.deleteByStudentId(profile.getId())
                        .thenMany(studentSubjectRepository.saveAll(requests.stream()
                                .map(r -> {
                                    StudentSubject s = new StudentSubject();
                                    s.setStudentId(profile.getId());
                                    s.setSubjectId(r.subjectId());
                                    s.setLevel(r.level());
                                    return s;
                                }).toList()))
                        .collectList()
                        .flatMap(saved -> recalculateCompletion(profile).thenReturn(saved)));
    }

    public Mono<List<StudentLanguage>> saveLanguages(Long userId, List<LanguageRequest> requests) {
        return studentProfileRepository.findByUserId(userId)
                .flatMap(profile -> studentLanguageRepository.deleteByStudentId(profile.getId())
                        .thenMany(studentLanguageRepository.saveAll(requests.stream()
                                .map(r -> {
                                    StudentLanguage l = new StudentLanguage();
                                    l.setStudentId(profile.getId());
                                    l.setLanguage(r.language());
                                    l.setProficiency(r.proficiency());
                                    return l;
                                }).toList()))
                        .collectList()
                        .flatMap(saved -> recalculateCompletion(profile).thenReturn(saved)));
    }

    public Mono<List<StudentAvailability>> saveAvailability(Long userId, List<AvailabilityRequest> requests) {
        return studentProfileRepository.findByUserId(userId)
                .flatMap(profile -> studentAvailabilityRepository.deleteByStudentId(profile.getId())
                        .thenMany(studentAvailabilityRepository.saveAll(requests.stream()
                                .map(r -> {
                                    StudentAvailability a = new StudentAvailability();
                                    a.setStudentId(profile.getId());
                                    a.setDayOfWeek(r.dayOfWeek());
                                    a.setStartTime(LocalTime.parse(r.startTime()));
                                    a.setEndTime(LocalTime.parse(r.endTime()));
                                    return a;
                                }).toList()))
                        .collectList()
                        .flatMap(saved -> recalculateCompletion(profile).thenReturn(saved)));
    }

    public Mono<StudentProfile> updateProfile(Long userId, StudentProfileRequest req) {
        return studentProfileRepository.findByUserId(userId)
                .flatMap(profile -> {
                    if (req.gradeLevel() != null) profile.setGradeLevel(req.gradeLevel());
                    if (req.learningGoals() != null) profile.setLearningGoals(req.learningGoals());
                    if (req.budgetMin() != null) profile.setBudgetMin(req.budgetMin());
                    if (req.budgetMax() != null) profile.setBudgetMax(req.budgetMax());
                    if (req.lessonFormat() != null) profile.setLessonFormat(req.lessonFormat());
                    profile.setUpdatedAt(LocalDateTime.now());
                    return studentProfileRepository.save(profile)
                            .flatMap(this::recalculateCompletion);
                });
    }

    public Mono<StudentDashboardResponse> getDashboard(Long studentId, Long userId) {
        return Mono.zip(
                sessionRepository.findUpcomingLessonsByStudentId(studentId, LocalDate.now()).collectList(),
                sessionRepository.findRecentCompletedLessonsByStudent(studentId, 5).collectList(),
                sessionRepository.countByStudentId(studentId),
                sessionRepository.countDistinctTutorsByStudentId(studentId),
                sessionRepository.sumCompletedHoursByStudentId(studentId).defaultIfEmpty(0.0),
                messageService.getUnreadCount(userId)
        ).map(tuple -> new StudentDashboardResponse(
                tuple.getT1(), tuple.getT2(),
                tuple.getT3().intValue(), tuple.getT4().intValue(),
                tuple.getT5().intValue(), tuple.getT6().intValue(),
                false
        ));
    }

    private Mono<StudentProfile> recalculateCompletion(StudentProfile profile) {
        Mono<Boolean> hasSubjects = studentSubjectRepository.findByStudentId(profile.getId()).hasElements();
        Mono<Boolean> hasLanguages = studentLanguageRepository.findByStudentId(profile.getId()).hasElements();
        Mono<Boolean> hasAvailability = studentAvailabilityRepository.findByStudentId(profile.getId()).hasElements();

        return Mono.zip(hasSubjects, hasLanguages, hasAvailability)
                .flatMap(tuple -> {
                    int completion = 0;
                    List<String> completed = new ArrayList<>();
                    List<String> missing = new ArrayList<>();

                    if (hasBasicInfo(profile)) { completion += 30; completed.add("PROFILE"); }
                    else missing.add("PROFILE");

                    if (tuple.getT1()) { completion += 30; completed.add("SUBJECTS"); }
                    else missing.add("SUBJECTS");

                    if (tuple.getT2()) { completion += 20; completed.add("LANGUAGES"); }
                    else missing.add("LANGUAGES");

                    if (tuple.getT3()) { completion += 20; completed.add("AVAILABILITY"); }
                    else missing.add("AVAILABILITY");

                    profile.setProfileCompletion(completion);
                    profile.setOnboardingStatus(completion >= 100 ? "COMPLETED" : "STARTED");
                    profile.setUpdatedAt(LocalDateTime.now());
                    return studentProfileRepository.save(profile);
                });
    }

    private boolean hasBasicInfo(StudentProfile profile) {
        return profile.getGradeLevel() != null
                && profile.getLearningGoals() != null
                && !profile.getLearningGoals().isBlank()
                && profile.getLessonFormat() != null
                && profile.getBudgetMin() != null
                && profile.getBudgetMax() != null;
    }

    public record BasicInfoRequest(
            String bio, String gradeLevel, String school, String learningGoals,
            String learningStyle, String timezone,
            java.math.BigDecimal budgetMin, java.math.BigDecimal budgetMax,
            StudentProfile.LessonFormat lessonFormat
    ) {}

    public record SubjectRequest(Long subjectId, String level) {}
    public record LanguageRequest(String language, String proficiency) {}
    public record AvailabilityRequest(String dayOfWeek, String startTime, String endTime) {}
}
