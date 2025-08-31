package com.fyp.file_service.mapper;

import com.fyp.file_service.dto.FileInfo;
import com.fyp.file_service.entity.FileManagement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FileManagementMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    FileManagement toFileManagement(FileInfo fileInfo);
}
