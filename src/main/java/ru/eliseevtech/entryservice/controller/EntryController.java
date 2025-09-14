package ru.eliseevtech.entryservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.eliseevtech.entryservice.dto.EntryDto;
import ru.eliseevtech.entryservice.dto.MoveRequest;
import ru.eliseevtech.entryservice.service.EntryService;

@RestController
@RequestMapping("/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService service;

    @GetMapping
    public ResponseEntity<Page<EntryDto>> list(
            @PageableDefault(size = 50, sort = "position", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<EntryDto> move(@PathVariable Long id, @RequestBody MoveRequest req) {
        return ResponseEntity.ok(service.move(id, req));
    }
}
