package ru.eliseevtech.entryservice.mapper;

import org.mapstruct.Mapper;
import ru.eliseevtech.entryservice.dto.EntryDto;
import ru.eliseevtech.entryservice.entity.Entry;

@Mapper(componentModel = "spring")
public interface EntryMapper {

    EntryDto toDto(Entry entity);

    Entry toEntity(EntryDto dto);
}
