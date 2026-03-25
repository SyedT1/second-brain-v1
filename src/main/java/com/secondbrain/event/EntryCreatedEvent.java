package com.secondbrain.event;

import com.secondbrain.model.Entry;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EntryCreatedEvent extends ApplicationEvent {

    private final Entry entry;

    public EntryCreatedEvent(Object source, Entry entry) {
        super(source);
        this.entry = entry;
    }
}
