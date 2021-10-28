import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Tone {

    public List<BellNote> readSong(String filename) throws IOException {
        List<BellNote> song = new ArrayList<BellNote>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/" + filename + ".txt"))) {
            String nextLine = br.readLine();
            while(nextLine != null) {
                NoteLength length = null;
                String[] splitLine = nextLine.split(" ");
                switch (splitLine[1]) {
                    case "1":
                        length=NoteLength.WHOLE;
                        break;
                    case "2":
                        length=NoteLength.HALF;
                        break;
                    case "4":
                        length=NoteLength.QUARTER;
                        break;
                    case "8":
                        length=NoteLength.EIGTH;
                        break;
                }
                song.add(new BellNote(Note.valueOf(splitLine[0]), length));
                nextLine = br.readLine();
            }
        }
        return song;
    }

    public static void main(String[] args) throws Exception {
        final AudioFormat af =
                new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Tone t = new Tone(af);
        List<BellNote> song = t.readSong("MH");
        t.playSong(song);
    }

    private final AudioFormat af;

    Tone(AudioFormat af) {
        this.af = af;
    }

    void playSong(List<BellNote> song) throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            for (BellNote bn: song) {
                playNote(line, bn);
            }
            line.drain();
        }
    }

    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
}