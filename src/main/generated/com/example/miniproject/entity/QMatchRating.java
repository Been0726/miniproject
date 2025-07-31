package com.example.miniproject.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatchRating is a Querydsl query type for MatchRating
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatchRating extends EntityPathBase<MatchRating> {

    private static final long serialVersionUID = 226519732L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatchRating matchRating = new QMatchRating("matchRating");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath comment = createString("comment");

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMatchRequest matchRequest;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    public final QMember rater;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regTime = _super.regTime;

    public final NumberPath<Integer> score = createNumber("score", Integer.class);

    public final QMember target;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QMatchRating(String variable) {
        this(MatchRating.class, forVariable(variable), INITS);
    }

    public QMatchRating(Path<? extends MatchRating> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatchRating(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatchRating(PathMetadata metadata, PathInits inits) {
        this(MatchRating.class, metadata, inits);
    }

    public QMatchRating(Class<? extends MatchRating> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.matchRequest = inits.isInitialized("matchRequest") ? new QMatchRequest(forProperty("matchRequest"), inits.get("matchRequest")) : null;
        this.rater = inits.isInitialized("rater") ? new QMember(forProperty("rater")) : null;
        this.target = inits.isInitialized("target") ? new QMember(forProperty("target")) : null;
    }

}

