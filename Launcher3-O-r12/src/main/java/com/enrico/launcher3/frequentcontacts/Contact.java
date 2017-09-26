package com.enrico.launcher3.frequentcontacts;


class Contact {

    private String name, id, thumbnail;

    String getContactName() {
        return name;
    }

    void setContactName(String name) {
        this.name = name;
    }

    String getContactId() {
        return id;
    }

    void setContactId(String id) {
        this.id = id;
    }

    String getContactThumbnail() {
        return thumbnail;
    }

    void setContactThumbnail(String thumbnail) {

        this.thumbnail = thumbnail;
    }
}
