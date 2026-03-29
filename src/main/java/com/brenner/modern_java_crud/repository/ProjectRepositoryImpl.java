package com.brenner.modern_java_crud.repository;

import com.brenner.modern_java_crud.domain.Member_;
import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.domain.Project_;
import com.brenner.modern_java_crud.repository.projection.ProjectStatusMetricsProjection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final EntityManager entityManager;

    /*
    * SELECT status, COUNT(*), SUM(total_budget)
    * FROM projects
    * WHERE [filters] - Optional
    * GROUP BY status
    */
    @Override
    public List<ProjectStatusMetricsProjection> getMetricsByStatus(
        final Specification<Project> spec
    ) {
        final var cb = entityManager.getCriteriaBuilder();
        final var query = cb.createQuery(ProjectStatusMetricsProjection.class);
        final var root = query.from(Project.class);

        query.multiselect(
            root.get(Project_.STATUS),
            cb.count(root),
            cb.sum(root.get(Project_.TOTAL_BUDGET)).as(BigDecimal.class)
        );
        Optional.ofNullable(spec.toPredicate(root, query, cb))
            .ifPresent(query::where);
        query.groupBy(root.get(Project_.STATUS));

        return entityManager.createQuery(query).getResultList();
    }

    /*
    * SELECT AVG(fn_days_between(start_at, fn_effective_date(end_at, expected_end_at))))
    * FROM projects
    * WHERE [filters] - Optional
    */
    @Override
    public Double getAverageDurationInDays(final Specification<Project> spec) {
        final var cb = entityManager.getCriteriaBuilder();
        final var query = cb.createQuery(Double.class);
        final var root = query.from(Project.class);

        query.select(
            cb.avg(
                cb.function(
                    "fn_days_between",
                    Integer.class,
                    root.get(Project_.START_AT),
                    root.get(Project_.EFFECTIVE_END_AT)
                )
            )
        );
        Optional.ofNullable(spec.toPredicate(root, query, cb))
            .ifPresent(query::where);

        final var result = entityManager.createQuery(query).getSingleResult();
        return result == null ? 0.0
            : result;
    }

    /*
     * SELECT COUNT(DISTINCT m.id)
     * FROM project p
     * JOIN projects_members pm ON pm.project_id = p.id
     * JOIN members m ON m.id = pm.member_id
     * WHERE [filters] - Optional
     */
    @Override
    public Long countUniqueMembersAllocated(final Specification<Project> spec) {
        final var cb = entityManager.getCriteriaBuilder();
        final var query = cb.createQuery(Long.class);
        final var root = query.from(Project.class);
        final var memberId = root.join(Project_.MEMBERS).get(Member_.ID);

        query.select(cb.countDistinct(memberId));
        Optional.ofNullable(spec.toPredicate(root, query, cb))
            .ifPresent(query::where);

        return entityManager.createQuery(query).getSingleResult();
    }

    /*
     * SELECT COUNT(DISTINCT manager_id)
     * FROM project
     * WHERE [filters] - Optional
     */
    @Override
    public Long countUniqueManagersAllocated(
        final Specification<Project> spec
    ) {
        final var cb = entityManager.getCriteriaBuilder();
        final var query = cb.createQuery(Long.class);
        final var root = query.from(Project.class);
        final var managerId = root.get(Project_.MANAGER).get(Member_.ID);

        query.select(cb.countDistinct(managerId));
        Optional.ofNullable(spec.toPredicate(root, query, cb))
            .ifPresent(query::where);

        return entityManager.createQuery(query).getSingleResult();
    }

}
