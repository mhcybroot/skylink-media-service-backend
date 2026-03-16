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
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "statusUpdatedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    Project toEntity(ProjectDTO projectDTO);
}
