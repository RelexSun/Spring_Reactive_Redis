package com.kshrd.reactiveredis.common.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DummyProperty {
    DUMMY_PROPERTY("Dummy");

    private final String fieldName;
}
