package root.cyb.mh.skylink_media_service.application.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import root.cyb.mh.skylink_media_service.domain.entities.Project;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);
    
    @Mapping(source = "status", target = "status")
    @Mapping(source = "paymentStatus", target = "paymentStatus")
    @Mapping(source = "statusUpdatedBy.username", target = "statusUpdatedBy")
    ProjectDTO toDTO(Project project);
    
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "assignedContractors", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "statusUpdatedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "firstOpenedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "statusUpdatedAt", ignore = true)
    @Mapping(target = "ppwNumber", ignore = true)
    @Mapping(target = "workType", ignore = true)
    @Mapping(target = "workDetails", ignore = true)
    @Mapping(target = "clientCompany", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "loanNumber", ignore = true)
    @Mapping(target = "loanType", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "woAdmin", ignore = true)
    @Mapping(target = "invoicePrice", ignore = true)
    @Mapping(target = "blocked", ignore = true)
    @Mapping(target = "blockedAt", ignore = true)
    @Mapping(target = "blockedBy", ignore = true)
    @Mapping(target = "blockedReason", ignore = true)
    Project toEntity(ProjectDTO projectDTO);
}
