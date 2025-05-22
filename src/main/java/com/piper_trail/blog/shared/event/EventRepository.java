package com.piper_trail.blog.shared.event;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<EventDocument, String> {

    /**
     * 발행되지 않은 이벤트 중 특정 시간 이전에 생성된 것들 조회
     * EventStoreService.republishUnpublishedEvents()에서 사용
     * 이벤트 발행 실패 시 재시도를 위해 필요
     */
    List<EventDocument> findByPublishedFalseAndCreatedAtBefore(Instant timestamp);

    /**
     * 관리자 대시보드에서 사용
     */
    List<EventDocument> findByAggregateIdOrderByCreatedAtAsc(String aggregateId);
}