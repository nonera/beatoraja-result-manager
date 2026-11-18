package com.beatoraja.screenshot.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClearLampTest {

    @Test
    void idsFollowBeatorajaNumbering() {
        assertEquals(0, ClearLamp.NO_PLAY.id());
        assertEquals(6, ClearLamp.HARD.id());
        assertEquals(10, ClearLamp.MAX.id());
        assertEquals(Optional.of(ClearLamp.EXHARD), ClearLamp.fromId(7));
        assertEquals(Optional.empty(), ClearLamp.fromId(11));
    }

    @Test
    void looksUpByNameAndBmzLabel() {
        assertEquals(Optional.of(ClearLamp.NORMAL), ClearLamp.fromDisplayName(" clear "));
        assertEquals(Optional.of(ClearLamp.LIGHT_ASSIST_EASY), ClearLamp.fromBmzLabel("LightAssistEasy"));
        assertEquals(Optional.empty(), ClearLamp.fromBmzLabel("Hazard"));
    }

    @Test
    void findsLongerNamesBeforeNamesTheyContain() {
        assertEquals(Optional.of(ClearLamp.EXHARD), ClearLamp.findIn("Song EXHARD CLEAR AA"));
        assertEquals(Optional.of(ClearLamp.LIGHT_ASSIST_EASY), ClearLamp.findIn("Song LIGHT ASSIST EASY CLEAR B"));
        assertEquals(Optional.of(ClearLamp.NORMAL), ClearLamp.findIn("MAX 300 CLEAR A"));
    }
}
