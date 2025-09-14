package ru.eliseevtech.entryservice.exception;

public class MoveConflictException extends RuntimeException {

    public MoveConflictException(String msg) {
        super(msg);
    }
}