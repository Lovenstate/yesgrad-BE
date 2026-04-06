package com.yesgrad.service.controller;

import com.yesgrad.service.domain.CommonResponse;
import com.yesgrad.service.dto.message.ConversationSummary;
import com.yesgrad.service.dto.message.MessageResponse;
import com.yesgrad.service.dto.message.SendMessageRequest;
import com.yesgrad.service.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public Mono<CommonResponse<MessageResponse>> sendMessage(@RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request).map( messageResponse -> CommonResponse.success("Message sent successfully",messageResponse));
    }

    @GetMapping("/conversation/{otherUserId}")
    public Flux<MessageResponse> getConversation(
            @RequestParam Long userId,
            @PathVariable Long otherUserId) {
        return messageService.getConversation(userId, otherUserId);
    }

    @GetMapping("/{messageId}/replies")
    public Flux<MessageResponse> getReplies(@PathVariable Long messageId) {
        return messageService.getReplies(messageId);
    }

    @PatchMapping("/{messageId}/read")
    public Mono<MessageResponse> markAsRead(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        return messageService.markMessageRead(messageId, userId);
    }

    @GetMapping("/unread")
    public Flux<MessageResponse> getUnread(@RequestParam Long userId) {
        return messageService.getUnreadMessages(userId);
    }

    @GetMapping("/unread/count")
    public Mono<Map<String, Long>> getUnreadCount(@RequestParam Long userId) {
        return messageService.getUnreadCount(userId)
                .map(count -> Map.of("unreadCount", count));
    }

    @GetMapping("/inbox")
    public Flux<ConversationSummary> getInbox(@RequestParam Long userId) {
        return messageService.getInbox(userId);
    }

    @GetMapping("/analytics/response-time")
    public Mono<Map<String, Double>> getAverageResponseTime(
            @RequestParam Long userId,
            @RequestParam LocalDateTime since) {
        return messageService.getAverageResponseTimeHours(userId, since)
                .map(avg -> Map.of("averageResponseTimeHours", avg));
    }

    @GetMapping("/analytics/response-rate")
    public Mono<Map<String, Double>> getResponseRate(
            @RequestParam Long userId,
            @RequestParam LocalDateTime since) {
        return messageService.getResponseRate(userId, since)
                .map(rate -> Map.of("responseRatePercent", rate));
    }

    @DeleteMapping("/{messageId}")
    public Mono<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long requesterId) {
        return messageService.deleteMessage(messageId, requesterId);
    }

    @PostMapping("/read")
    public Mono<CommonResponse<MessageResponse>> readMessage(@RequestBody ReadMessageRequest req) {
        return messageService.markMessageRead(req.messageId(), req.userId()).map( messageResponse -> CommonResponse.success("Message marked as read successfully",messageResponse));
    }

    public record ReadMessageRequest(
            Long messageId,
            Long userId
    ) {}

//
//#### Message Endpoints
//```
//    GET    /api/messages/conversations # Get conversations
//    GET    /api/messages/conversation/{id} # Get conversation messages
//    POST   /api/messages               # Send message
//    PUT    /api/messages/{id}/read     # Mark as read
//    DELETE /api/messages/{id}          # Delete message
}
