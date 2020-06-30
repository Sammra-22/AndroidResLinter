package com.tanitech.reslinter

enum ResourceType {
    DRAWABLE('drawable', ".*${File.separator}res${File.separator}drawable.*${File.separator}.*[.].*"),
    ANIM('anim|animator', ".*${File.separator}res${File.separator}anim.*${File.separator}.*[.].*"),
    LAYOUT('layout', ".*${File.separator}res${File.separator}layout.*${File.separator}.*[.].*"),
    STRING('string|plurals', ".*${File.separator}res${File.separator}values.*${File.separator}strings.xml"),
    COLOR('color', ".*${File.separator}res${File.separator}values.*${File.separator}colors.xml")

    String refMatcher
    String pathMatcher

    ResourceType(String refMatcher, String pathMatcher) {
        this.refMatcher = refMatcher
        this.pathMatcher = pathMatcher
    }

    boolean isResourceValue() {
        return this in [STRING, COLOR]
    }

    @Override
    String toString() {
        return name().toLowerCase()
    }
}
