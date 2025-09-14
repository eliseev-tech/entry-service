package ru.eliseevtech.entryservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.eliseevtech.entryservice.dto.EntryDto;
import ru.eliseevtech.entryservice.dto.MoveRequest;

public interface EntryService {

    Page<EntryDto> list(Pageable pageable);

    EntryDto move(Long id, MoveRequest req);
}
