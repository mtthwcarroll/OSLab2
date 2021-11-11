import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Conductor {

    //Class variables
    private ArrayList<BellNote> song; //data structure for Song that will be played
    private final AudioFormat af; //audio source for playing sound
    private boolean err; //flag if error occurred, will not play songs if true.

    /**
     * Conductor is used to read a song file and then play it using multiple Member threads. It
     * is a multi-threaded solution to the Tone class. The constructor instantiates an AudioFormat itself
     * which is passed to members to use in the form of a SourceDataLine.
     */
    Conductor() {
        af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
    }

    /**
     * Takes a String that should be the filename of a song .txt file in the src folder. Song files need
     * to follow a rigid format or the function will halt (*NoteType* *Length* with each note being
     * on its own line i.e. A5 4). Song will be read into song class variable that can be used by
     * playSong()
     *
     * @param filename String, should be a .txt file (i.e. "Song.txt"). File
     *                 should be in src folder.
     */
    public void readSong(String filename) {
        err = false; //flag if there was an error
        int index = 0; //index for counting lines for error reporting
        song = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/" + filename + ".txt"))) {
            String nextLine = br.readLine();
            index++;
            while(nextLine != null) {
                NoteLength length = null;
                String[] splitLine = nextLine.split(" ");
                if(splitLine.length != 2) {
                    System.err.println("Song reader error: More than or less than two items on line " + index);
                    err = true;
                    break;
                }
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
                    case "16":
                        length=NoteLength.SIXTEENTH;
                        break;
                    default:
                        System.err.println("Song reader error: unrecognized note length at line " + index);
                        err = true;
                        break;
                }
                if(err) {break;}
                try {
                    Note n = Note.valueOf(splitLine[0]);
                    BellNote bn = new BellNote(n,length);
                    song.add(bn);
                } catch (IllegalArgumentException e){
                    System.err.println("Song reader error: unrecognized note on line " + index);
                    e.printStackTrace();
                    err = true;
                    break;
                }
                nextLine = br.readLine();
                index++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't find file.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Reader error");
            e.printStackTrace();
        }
    }

    /**
     * Will play a song if there was no reader error.
     * First creates a Member map and populates it with a Member for each unique note
     * in the song that was read, then will play each note using the Member's playNote function.
     * Catches errors related to thread sleeping and lines.
     */
    void playSong() {
        if (!err) {
            Map<Note, Member> members = new HashMap<>();
            try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {

                line.open();
                line.start();

                for (BellNote bn : song) {
                    Note note = bn.getNote();
                    if (!members.containsKey(note)) {
                        members.put(note, new Member(line, bn.getNote()));
                    }
                }

                for (Member m : members.values()) {
                    m.start();
                }

                for (BellNote bn : song) {
                    Member m = members.get(bn.getNote());
                    m.playNow(bn.length);
                    Thread.sleep(bn.length.timeMs());
                }

                line.drain();

                for (Member m : members.values()) {
                    m.line.drain();
                    m.stop();
                }

            } catch (InterruptedException e) {
                System.err.println("Thread sleep error");
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                System.err.println("Line Unavailable Exception");
                e.printStackTrace();
            }
        } else {
            System.err.println("Reader error, try reading a different song.");
        }
    }

    //Create a conductor, read in directory, and read input from console.
    public static void main(String[] args) {
        Conductor conduct = new Conductor();
        while(true) { //Break out of loop when given an input of 0
            File f = new File("src");
            Map<Integer,String> songNames = new HashMap<>();
            String[] pathNames = f.list();
            int index = 1;
            // validate pathNames and populate songNames with names of .txt files in src folder
            if(pathNames != null) {
                for (String s : pathNames) {
                    if (s.endsWith(".txt")) {
                        songNames.put(index++, s.substring(0, s.length() - 4));
                    }
                }
            } else {
                System.err.println("Can't find path names");
                break;
            }
            System.out.println("Enter a number of a song to play or 0 to exit.");
            // Print out song names
            for(Integer i : songNames.keySet()) {
                System.out.println(i + ". " + songNames.get(i));
            }
            System.out.println("Enter number: ");
            Scanner scan = new Scanner(System.in);
            int i;
            // Scan until a valid input has been given.
            while(!scan.hasNext("[0-" + songNames.size() + "]")) { //inspired by Jaden
                System.out.println("Please enter a valid input");
                scan.next();
            }
            i = scan.nextInt();
            // Make a choice based upon input
            if(i == 0) {
                scan.close();
                break;
            } else {
                conduct.readSong(songNames.get(i));
                conduct.playSong();
            }
        }
    }
}
