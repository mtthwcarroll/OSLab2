class BellNote {
    final Note note;
    final NoteLength length;

    /**
     * Used to combine a Note and a NoteLength to from a playable note
     *
     * @param note Note enumeration
     * @param length NoteLength enumeration
     */
    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }

    public Note getNote() {
        return note;
    }
}