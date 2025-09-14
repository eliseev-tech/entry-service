package ru.eliseevtech.entryservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eliseevtech.entryservice.dto.EntryDto;
import ru.eliseevtech.entryservice.dto.MoveRequest;
import ru.eliseevtech.entryservice.entity.Entry;
import ru.eliseevtech.entryservice.exception.MoveConflictException;
import ru.eliseevtech.entryservice.mapper.EntryMapper;
import ru.eliseevtech.entryservice.repository.EntryRepository;
import ru.eliseevtech.entryservice.service.EntryService;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class EntryServiceImpl implements EntryService {

    private static final long GAP = 1000L;

    private final EntryRepository repository;
    private final EntryMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public Page<EntryDto> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional
    @Override
    public EntryDto move(Long id, MoveRequest req) {
        requireNonNull(req, "MoveRequest is required");
        requireNonNull(req.getMode(), "MoveRequest.mode is required");

        var target = repository.findById(id).orElseThrow();
        return switch (req.getMode()) {
            case UP -> mapper.toDto(moveUp(target));
            case DOWN -> mapper.toDto(moveDown(target));
            case TO_POSITION -> mapper.toDto(moveToPosition(target, req.getPosition()));
        };
    }

    private Entry moveUp(Entry target) {
        var left = leftNeighbor(target.getPosition()).orElse(null);
        if (left == null) {
            return target;
        }
        return swapWithBuffer(target, left);
    }

    private Entry moveDown(Entry target) {
        var right = rightNeighbor(target.getPosition()).orElse(null);
        if (right == null) {
            return target;
        }
        return swapWithBuffer(target, right);
    }

    private Entry swapWithBuffer(Entry a, Entry b) {
        long posA = a.getPosition();
        long posB = b.getPosition();
        long tmp = Long.MIN_VALUE + 1;

        repository.swapWithBuffer(a.getId(), b.getId(), posA, posB, tmp);

        return repository.findById(a.getId()).orElse(a);
    }

    private Entry moveToPosition(Entry target, Long desired) {
        requireNonNull(desired, "position is required for TO_POSITION");
        if (desired.equals(target.getPosition())) {
            return target;
        }

        var occupant = repository.findByPosition(desired);
        if (occupant.isEmpty() || occupant.get().getId().equals(target.getId())) {
            target.setPosition(desired);
            return safeSave(target);
        }

        var left = leftNeighbor(desired).orElse(null);
        var right = rightNeighbor(desired).orElse(null);

        long newPos = computeBetween(left, right);
        target.setPosition(newPos);
        return safeSave(target);
    }

    private Optional<Entry> leftNeighbor(Long position) {
        return repository.findFirstByPositionLessThanOrderByPositionDesc(position);
    }

    private Optional<Entry> rightNeighbor(Long position) {
        return repository.findFirstByPositionGreaterThanOrderByPositionAsc(position);
    }

    private long computeBetween(Entry left, Entry right) {
        if (left == null && right == null) {
            return 0L;
        }
        if (left == null) {
            return right.getPosition() - GAP;
        }
        if (right == null) {
            return left.getPosition() + GAP;
        }

        long l = left.getPosition();
        long r = right.getPosition();
        if (l >= r - 1) {
            throw new MoveConflictException("No gap between neighbors");
        }

        return Math.floorDiv(l + r, 2);
    }

    private void swapPositions(Entry a, Entry b) {
        long tmp = a.getPosition();
        a.setPosition(b.getPosition());
        b.setPosition(tmp);
    }

    private Entry safeSave(Entry e) {
        try {
            return repository.saveAndFlush(e);
        } catch (DataIntegrityViolationException ex) {
            throw new MoveConflictException("Position conflict, try again");
        }
    }
}