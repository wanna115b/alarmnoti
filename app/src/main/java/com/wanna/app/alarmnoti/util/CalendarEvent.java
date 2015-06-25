package com.wanna.app.alarmnoti.util;

public class CalendarEvent {
    public String id;
    public String summary;
    public String accessRole;
    public boolean checked;
    public String uri;

    public CalendarEvent() {
    }

    public CalendarEvent(String id, String summary, String accessRole, boolean checked, String uri) {
        this.id = id;
        this.summary = summary;
        this.accessRole = accessRole;
        this.checked = checked;
        this.uri = uri;
    }
}
