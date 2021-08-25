package io.legacyfighter.cabs.distance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

class DistanceTest {

    @Test
    void cannotUnderstandInvalidUnit() {
        //expect
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Distance.ofKm(2000).printIn("invalid"));
    }

    @Test
    void canConvertToFloat() {
        //expect
        assertEquals(2000f, Distance.ofKm(2000).toKmInFloat());
        assertEquals(0f, Distance.ofKm(0).toKmInFloat());
        assertEquals(312.22f, Distance.ofKm(312.22f).toKmInFloat());
        assertEquals(2f, Distance.ofKm(2).toKmInFloat());
    }

    @Test
    void canRepresentDistanceAsMeters() {
        //expect
        assertEquals("2000000m", Distance.ofKm(2000).printIn("m"));
        assertEquals("0m", Distance.ofKm(0).printIn("m"));
        assertEquals("312220m", Distance.ofKm(312.22f).printIn("m"));
        assertEquals("2000m", Distance.ofKm(2).printIn("m"));
    }

    @Test
    void canRepresentDistanceAsKm() {
        //expect
        assertEquals("2000km", Distance.ofKm(2000).printIn("km"));
        assertEquals("0km", Distance.ofKm(0).printIn("km"));
        assertEquals("312.220km", Distance.ofKm(312.22f).printIn("km"));
        assertEquals("312.221km", Distance.ofKm(312.221111232313f).printIn("km"));
        assertEquals("2km", Distance.ofKm(2).printIn("km"));
    }

    @Test
    void canRepresentDistanceAsMiles() {
        //expect
        assertEquals("1242.742miles", Distance.ofKm(2000).printIn("miles"));
        assertEquals("0miles", Distance.ofKm(0).printIn("miles"));
        assertEquals("194.005miles", Distance.ofKm(312.22f).printIn("miles"));
        assertEquals("194.005miles", Distance.ofKm(312.221111232313f).printIn("miles"));
        assertEquals("1.243miles", Distance.ofKm(2).printIn("miles"));
    }



}