package me.parhamziaei.practice.exception.custom.service;

public class EmailServiceException extends RuntimeException {
	public EmailServiceException(String message) {
		super(message);
	}
}
