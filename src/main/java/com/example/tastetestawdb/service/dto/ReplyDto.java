package com.example.tastetestawdb.service.dto;

public class ReplyDto {
    private String text;

    public ReplyDto(String text) {
        this.text = text;
    }

    public ReplyDto() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
