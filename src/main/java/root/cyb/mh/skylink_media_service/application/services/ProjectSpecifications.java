package root.cyb.mh.skylink_media_service.application.services;

import org.springframework.data.jpa.domain.Specification;
import root.cyb.mh.skylink_media_service.application.dto.ProjectSearchCriteria;
import root.cyb.mh.skylink_media_service.domain.entities.Project;
import root.cyb.mh.skylink_media_service.domain.entities.ProjectAssignment;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ProjectSpecifications {
    
    public static Specification<Project> buildSpecification(ProjectSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (criteria.getTextSearch() != null && !criteria.getTextSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getTextSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("workOrderNumber")), pattern),
                    cb.like(cb.lower(root.get("location")), pattern),
                    cb.like(cb.lower(root.get("clientCode")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            
            if (criteria.getPaymentStatus() != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), criteria.getPaymentStatus()));
            }
            
            if (criteria.getDueDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), criteria.getDueDateFrom()));
            }
            
            if (criteria.getDueDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), criteria.getDueDateTo()));
            }
            
            if (criteria.getPriceFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("invoicePrice"), criteria.getPriceFrom()));
            }
            
            if (criteria.getPriceTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("invoicePrice"), criteria.getPriceTo()));
            }
            
            if (criteria.getAssignedContractorId() != null) {
                Join<Project, ProjectAssignment> assignments = root.join("assignments", JoinType.LEFT);
                predicates.add(cb.equal(assignments.get("contractor").get("id"), criteria.getAssignedContractorId()));
                query.distinct(true);
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
