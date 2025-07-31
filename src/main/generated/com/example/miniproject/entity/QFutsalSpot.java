package com.example.miniproject.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFutsalSpot is a Querydsl query type for FutsalSpot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFutsalSpot extends EntityPathBase<FutsalSpot> {

    private static final long serialVersionUID = -1207768151L;

    public static final QFutsalSpot futsalSpot = new QFutsalSpot("futsalSpot");

    public final StringPath businessTime = createString("businessTime");

    public final StringPath homepageUrl = createString("homepageUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath img1Url = createString("img1Url");

    public final StringPath img2Url = createString("img2Url");

    public final StringPath img3Url = createString("img3Url");

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath name = createString("name");

    public QFutsalSpot(String variable) {
        super(FutsalSpot.class, forVariable(variable));
    }

    public QFutsalSpot(Path<? extends FutsalSpot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFutsalSpot(PathMetadata metadata) {
        super(FutsalSpot.class, metadata);
    }

}

