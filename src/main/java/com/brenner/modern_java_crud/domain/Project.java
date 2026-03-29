package com.brenner.modern_java_crud.domain;

import com.brenner.modern_java_crud.exception.BusinessException;
import com.brenner.modern_java_crud.util.Ensure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "projects_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> members = new HashSet<>();

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "start_at", nullable = false)
    private LocalDate startAt;

    @Column(name = "expected_end_at", nullable = false)
    private LocalDate expectedEndAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Setter(AccessLevel.NONE)
    @Formula("fn_effective_date(end_at, expected_end_at)")
    private LocalDate effectiveEndAt;

    @Setter(AccessLevel.NONE)
    @Formula(
        "fn_months_between(start_at, fn_effective_date(end_at, expected_end_at))"
    )
    private Integer durationMonths;

    @Column(name = "total_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBudget;

    @Column(length = 500)
    private String description;

    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ProjectStatus status;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public void fillStatus(final ProjectStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus não deve ser nulo");
        Ensure.notNull(this, this.status);

        if (!this.status.canChangeTo(newStatus))
            throw new BusinessException(
                String.format(
                    "O status não pode ser alterado de %s para %s.",
                    status,
                    newStatus
                )
            );

        this.status = newStatus;
    }

    public void fillNextStatus() {
        Ensure.notNull(this, this.status);
        this.fillStatus(this.status.getNext());
    }

    public void fillCancelStatus() {
        this.fillStatus(ProjectStatus.CANCELLED);
    }

    void validateDateRange() {
        Ensure.notNull(this, this.startAt, this.expectedEndAt);

        if (this.expectedEndAt.isBefore(this.startAt))
            throw new BusinessException(
                "A data de término prevista não deve ser anterior à data de início."
            );
    }

    void validateEndDate() {
        Ensure.notNull(this, this.status, this.startAt);

        if (this.status.isFinal() && this.endAt == null)
            throw new BusinessException(
                "A data de término é obrigatória ao finalizar projeto."
            );

        if (this.endAt != null && this.endAt.isBefore(this.startAt))
            throw new BusinessException(
                "A data de término não deve ser anterior à data de início."
            );
    }

    public void validateCreate() {
        this.validateDateRange();
        this.validateManager();
        this.validateMembers();
    }

    public void validateUpdate() {
        this.validateDateRange();
        this.validateEndDate();
        this.validateManager();
        this.validateMembers();
    }

    public void validateDelete() {
        Ensure.notNull(this, this.status);

        if (!this.status.canDelete())
            throw new BusinessException(
                String.format("O status %s não permite exclusão!", this.status)
            );
    }

    public void validateManager() {
        Ensure.notNull(this, this.manager);

        final boolean isManagerInMembers = this.members.stream()
            .anyMatch(
                member -> Objects.equals(member.getId(), this.manager.getId())
            );

        if (isManagerInMembers)
            throw new BusinessException(
                String.format(
                    "O membro ID %d já está como gerente responsável do projeto.",
                    this.manager.getId()
                )
            );
    }

    public void validateMembers() {
        Ensure.notNull(this, this.members);

        if (this.members.size() < 1 || this.members.size() > 10)
            throw new BusinessException(
                "Cada projeto deve permitir a alocação de no mínimo 1 e no máximo 10 membros."
            );
    }

    private void fillInitialStatus() {
        this.status = ProjectStatus.getInitial();
    }

    @PrePersist
    protected void prePersist() {
        this.fillInitialStatus();
        this.validateCreate();
    }

    @PreUpdate
    protected void preUpdate() {
        this.validateUpdate();
    }

    @PreRemove
    protected void preRemove() {
        this.validateDelete();
    }

    public RiskLevel getRiskLevel() {
        return this.totalBudget == null || this.durationMonths == null ? null
            : RiskLevel.from(this.totalBudget, this.durationMonths);
    }

}
