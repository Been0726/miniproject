package com.example.miniproject.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatchSlot is a Querydsl query type for MatchSlot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatchSlot extends EntityPathBase<MatchSlot> {

    private static final long serialVersionUID = 1046084821L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatchSlot matchSlot = new QMatchSlot("matchSlot");

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath matched = createBoolean("matched");

    public final ListPath<MatchRequest, QMatchRequest> matchRequests = this.<MatchRequest, QMatchRequest>createList("matchRequests", MatchRequest.class, QMatchRequest.class, PathInits.DIRECT2);

    public final QFutsalSpot spot;

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public QMatchSlot(String variable) {
        this(MatchSlot.class, forVariable(variable), INITS);
    }

    public QMatchSlot(Path<? extends MatchSlot> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatchSlot(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatchSlot(PathMetadata metadata, PathInits inits) {
        this(MatchSlot.class, metadata, inits);
    }

    public QMatchSlot(Class<? extends MatchSlot> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.spot = inits.isInitialized("spot") ? new QFutsalSpot(forProperty("spot")) : null;
    }

}

