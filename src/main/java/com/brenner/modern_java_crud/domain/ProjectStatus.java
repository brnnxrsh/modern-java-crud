package com.brenner.modern_java_crud.domain;

import java.util.EnumSet;
import java.util.Set;

public enum ProjectStatus {

    IN_REVIEW,
    REVIEW_COMPLETED,
    REVIEW_APPROVED,
    STARTED,
    PLANNED,
    IN_PROGRESS,
    FINISHED,
    CANCELLED;

    private static final Set<ProjectStatus> NOT_DELETABLE = EnumSet
        .of(STARTED, IN_PROGRESS, FINISHED);

    public static ProjectStatus getInitial() {
        return IN_REVIEW;
    }

    static ProjectStatus getFinal() {
        return FINISHED;
    }

    public ProjectStatus getNext() {
        return switch (this) {
            case IN_REVIEW -> REVIEW_COMPLETED;
            case REVIEW_COMPLETED -> REVIEW_APPROVED;
            case REVIEW_APPROVED -> STARTED;
            case STARTED -> PLANNED;
            case PLANNED -> IN_PROGRESS;
            case IN_PROGRESS -> FINISHED;
            case FINISHED, CANCELLED -> this;
        };
    }

    public boolean isFinal() {
        return this == getFinal();
    }

    public boolean isCanceled() {
        return this == CANCELLED;
    }

    public boolean isFinalOrCanceled() {
        return this.isFinal() || this.isCanceled();
    }

    public boolean canChangeTo(ProjectStatus status) {
        return status == CANCELLED || status == getNext();
    }

    public boolean canDelete() {
        return !NOT_DELETABLE.contains(this);
    }

}
