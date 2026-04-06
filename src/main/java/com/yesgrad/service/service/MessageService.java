package com.yesgrad.service.service;

import com.yesgrad.service.domain.Message;
import com.yesgrad.service.dto.message.ConversationSummary;
import com.yesgrad.service.dto.message.MessageResponse;
import com.yesgrad.service.dto.message.QuotedMessageResponse;
import com.yesgrad.service.dto.message.SendMessageRequest;
import com.yesgrad.service.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int QUOTE_PREVIEW_LENGTH = 120;
    private static final Message SENTINEL = new Message();

    private final MessageRepository messageRepository;

    /**
     * Send a new message, optionally as a reply to an existing one.
     * When {@code replyToMessageId} is set:
     *  - the parent message must exist
     *  - the parent must belong to the same conversation (same two users)
     */
    public Mono<MessageResponse> sendMessage(SendMessageRequest req) {
        return validate(req)
                .then(resolveParent(req).defaultIfEmpty(SENTINEL))
                .flatMap(parentOrSentinel -> {
                    // Unwrap sentinel — null means this is a top-level message
                    Message parentMsg = (parentOrSentinel == SENTINEL) ? null : parentOrSentinel;
                    Message message = new Message(req.senderId(), req.receiverId(), req.content().trim(), req.replyToMessageId());

                    Mono<Message> parentMono = (parentMsg != null)
                            ? Mono.just(parentMsg)
                            : Mono.empty();

                    return messageRepository.save(message)
                            .flatMap(saved -> hydrateResponse(saved, parentMono));
                });
    }

    /**
     * Full conversation thread (oldest-first), auto-marking incoming as read.
     * Each message that is a reply includes a {@link QuotedMessageResponse} preview.
     */
    public Flux<MessageResponse> getConversation(Long userId, Long otherUserId) {
        return messageRepository.findConversation(userId, otherUserId)
                .flatMapSequential(message -> markIfIncoming(message, userId))
                .flatMapSequential(message -> hydrateResponse(message, Mono.empty()));
    }

    /**
     * Fetch all direct replies to a specific message, with their own quotes hydrated.
     */
    public Flux<MessageResponse> getReplies(Long messageId) {
        return messageRepository.findRepliesByMessageId(messageId)
                .flatMapSequential(msg -> hydrateResponse(msg, Mono.empty()));
    }

    /**
     * Mark a single message as read (idempotent). Only the receiver may call this.
     */
    public Mono<MessageResponse> markMessageRead(Long messageId, Long userId) {
        return messageRepository.findById(messageId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Message not found: " + messageId)))
                .flatMap(message -> {
                    if (!message.getReceiverId().equals(userId)) {
                        return Mono.error(new IllegalStateException(
                                "User " + userId + " is not the receiver of message " + messageId));
                    }
                    if (message.getIsRead()) return Mono.just(message);
                    message.setIsRead(true);
                    message.setReadAt(LocalDateTime.now());
                    return messageRepository.save(message);
                })
                .flatMap(msg -> hydrateResponse(msg, Mono.empty()));
    }

    // - Unread
    /** Stream of all unread messages addressed to {@code userId}. */
    public Flux<MessageResponse> getUnreadMessages(Long userId) {
        return messageRepository.findUnreadByReceiver(userId)
                .flatMapSequential(msg -> hydrateResponse(msg, Mono.empty()));
    }

    /** Total unread count for {@code userId} (native DB COUNT). */
    public Mono<Long> getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    // - Inbox
    /**
     * One {@link ConversationSummary} per partner, sorted most-recent-first.
     * Uses {@code Mono.zip} to fetch last message and unread count in parallel.
     */
    public Flux<ConversationSummary> getInbox(Long userId) {
        return messageRepository.findConversationPartnerIds(userId)
                .flatMap(partnerId ->
                        Mono.zip(
                                messageRepository.findLatestInConversation(userId, partnerId),
                                messageRepository.countUnreadBetween(partnerId, userId)
                        ).map(tuple -> new ConversationSummary(
                                partnerId,
                                tuple.getT1().getContent(),
                                tuple.getT1().getSentAt(),
                                tuple.getT2()
                        ))
                )
                .sort((a, b) -> b.lastMessageAt().compareTo(a.lastMessageAt()));
    }

    // ── Analytics

    /** Avg hours between receipt and read, for messages since {@code since}. */
    public Mono<Double> getAverageResponseTimeHours(Long userId, LocalDateTime since) {
        return messageRepository.getAverageResponseTimeHours(userId, since);
    }

    /** Percentage of received messages that were read, since {@code since}. */
    public Mono<Double> getResponseRate(Long userId, LocalDateTime since) {
        return messageRepository.getResponseRate(userId, since);
    }

    // ── Delete
    /**
     * Delete a message. Only the original sender may delete.
     * Emits {@code Mono.error(IllegalStateException)} on violations.
     */
    public Mono<Void> deleteMessage(Long messageId, Long requesterId) {
        return messageRepository.findById(messageId)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("Message not found: " + messageId)))
                .flatMap(message -> {
                    if (!message.getSenderId().equals(requesterId)) {
                        return Mono.error(new IllegalStateException(
                                "User " + requesterId + " cannot delete message " + messageId));
                    }
                    return messageRepository.deleteById(messageId);
                });
    }

    // ── Private helpers
    private Mono<Void> validate(SendMessageRequest req) {
        if (req.senderId() == null || req.receiverId() == null) {
            return Mono.error(new IllegalArgumentException("senderId and receiverId are required."));
        }
        if (req.senderId().equals(req.receiverId())) {
            return Mono.error(new IllegalArgumentException("A user cannot message themselves."));
        }
        if (req.content() == null || req.content().isBlank()) {
            return Mono.error(new IllegalArgumentException("Message content must not be empty."));
        }
        return Mono.empty();
    }

    private Mono<Message> markIfIncoming(Message message, Long readerId) {
        if (!message.getReceiverId().equals(readerId) || message.getIsRead()) {
            return Mono.just(message);
        }
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    /**
     * If a replyToMessageId is provided, resolve the parent and verify it belongs
     * to the same conversation. Returns Mono.empty() for top-level messages.
     */
    private Mono<Message> resolveParent(SendMessageRequest req) {
        if (req.replyToMessageId() == null) {
            return Mono.empty();
        }

        return messageRepository.findById(req.replyToMessageId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Quoted  message not found: " + req.replyToMessageId())))
                .flatMap(parent -> {
                    boolean sameConversation =
                            (parent.getSenderId().equals(req.senderId()) && parent.getReceiverId().equals(req.receiverId())) ||
                                    (parent.getSenderId().equals(req.receiverId()) && parent.getReceiverId().equals(req.senderId()));

                    if (!sameConversation) {
                        return Mono.error(new IllegalArgumentException(
                                "Cannot reply to a message from a different conversation."));
                    }
                    return Mono.just(parent);
                });
    }

    /**
     * Enrich a message with its quoted-message preview.
     * {@code existingParent} is passed in when already resolved to avoid a second DB call.
     */
    private Mono<MessageResponse> hydrateResponse(Message message, Mono<Message> existingParent) {
        if (message.getReplyToMessageId() == null) {
            return Mono.just(toResponse(message, null));
        }

        Mono<Message> parentMono = existingParent
                .switchIfEmpty(Mono.defer(() -> messageRepository.findById(message.getReplyToMessageId())));

        return parentMono
                .map(parent -> {
                    String preview = parent.getContent().length() > QUOTE_PREVIEW_LENGTH ?
                            parent.getContent().substring(0, QUOTE_PREVIEW_LENGTH) + "..." :
                            parent.getContent();

                    return toResponse(message, new QuotedMessageResponse(
                            parent.getId(),
                            parent.getSenderId(),
                            preview
                    ));
                }).defaultIfEmpty(toResponse(message, null));
    }

    private MessageResponse toResponse(Message m, QuotedMessageResponse quoted) {
        return new MessageResponse(
                m.getId(), m.getSenderId(), m.getReceiverId(),
                m.getContent(), m.getReplyToMessageId(), quoted, m.getSentAt(), m.getReadAt(), m.getIsRead()
        );
    }
}
