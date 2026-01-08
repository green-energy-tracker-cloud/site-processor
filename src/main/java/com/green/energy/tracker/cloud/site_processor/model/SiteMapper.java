package com.green.energy.tracker.cloud.site_processor.model;

import com.green.energy.tracker.cloud.site.v1.Site;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SiteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SiteWriteDocument updateDoc(Site site, @MappingTarget SiteWriteDocument document);

}
