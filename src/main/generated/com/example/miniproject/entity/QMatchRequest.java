package com.example.miniproject.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatchRequest is a Querydsl query type for MatchRequest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatchRequest extends EntityPathBase<MatchRequest> {

    private static final long serialVersionUID = -1455727528L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatchRequest matchRequest = new QMatchRequest("matchRequest");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath matched = createBoolean("matched");

    public final QMatchSlot matchSlot;

    public final QMember member;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    public final QMember opponent;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regTime = _super.regTime;

    public final EnumPath<com.example.miniproject.constant.MatchStatus> status = createEnum("status", com.example.miniproject.constant.MatchStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QMatchRequest(String variable) {
        this(MatchRequest.class, forVariable(variable), INITS);
    }

    public QMatchRequest(Path<? extends MatchRequest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatchRequest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatchRequest(PathMetadata metadata, PathInits inits) {
        this(MatchRequest.class, metadata, inits);
    }

    public QMatchRequest(Class<? extends MatchRequest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.matchSlot = inits.isInitialized("matchSlot") ? new QMatchSlot(forProperty("matchSlot"), inits.get("matchSlot")) : null;
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
        this.opponent = inits.isInitialized("opponent") ? new QMember(forProperty("opponent")) : null;
    }

}

