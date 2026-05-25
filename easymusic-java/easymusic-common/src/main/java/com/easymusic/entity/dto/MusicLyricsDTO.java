package com.easymusic.entity.dto;

import java.math.BigDecimal;

public class MusicLyricsDTO {
    private BigDecimal start;
    private BigDecimal end;
    private String text;

    public BigDecimal getStart() {
        return start;
    }

    public void setStart(BigDecimal start) {
        this.start = start;
    }

    public BigDecimal getEnd() {
        return end;
    }

    public void setEnd(BigDecimal end) {
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
