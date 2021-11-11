import javax.sound.sampled.SourceDataLine;

public class Member implements Runnable {

    //Class variables
    public final SourceDataLine line; //Line for audio output
    public volatile boolean waiting; //Flag for waiting to play note
    public volatile boolean playing; //Flag for running and stopping thread
    public final Note myNote; //Note to play
    public final Thread thread;
    private volatile NoteLength currentL; //Note Length

    /**
     * Constructor for a Member. Members are used by the Conductor to play notes. Each Member
     * is given one type of note to play and when signaled by the conductor will play it for a
     * length given at the time. When not playing the Member will wait until signaled by playNow()
     *
     * @param l SourceDataLine
     * @param n Note
     */
     Member(SourceDataLine l, Note n) {
        line = l;
        waiting = true;
        playing = false;
        thread = new Thread(this);
        myNote = n;
    }

    /**
     * Run function for the member thread. Will wait until signaled by playNow or stopped.
     */
    public synchronized void run() {
        while(playing) {
            while(waiting) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(!playing) {break;}
            System.out.println(myNote);
            playNote();
            waiting = true;
        }
    }

    /**
     * A running member will wait until signalled by playNow to play their note. The note will
     * be played for the length passed and the go back to waiting.
     *
     * @param l NoteLength, tells the member how long to play the note
     */
    public synchronized void playNow(NoteLength l) {
        waiting = false;
        currentL = l;
        notify();
    }

    /**
     * Start the thread and put the member into a running state waiting for the playNow function
     * to be called.
     */
    public void start() {
        playing = true;
        waiting = true;
        thread.start();
    }

    /**
     * Will set playing and waiting flags to false and let the thread die
     */
    public synchronized void stop() {
        playing = false;
        waiting = false;
        notify();
    }

    /**
     * Uses the line that the Member has been passed to play the note that the Member owns.
     */
    private void playNote(){
            final int ms = Math.min(currentL.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
            final int length = Note.SAMPLE_RATE * ms / 1000;
            line.write(myNote.sample(), 0, length);
            line.write(Note.REST.sample(), 0, 50);
    }
}