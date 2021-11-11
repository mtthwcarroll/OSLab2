enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGTH(0.125f),
    SIXTEENTH(0.0625f);

    private final int timeMs;

    /**
     * Enumeration for helping calculate the note length in a BellNote object.
     *
     * @param length length of the note (1 for a whole note, .5 for a half note)
     */
    private NoteLength(float length) {
        timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}