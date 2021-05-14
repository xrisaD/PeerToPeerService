import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class State {

    // variables for the main logic
    int counter;
    boolean done4;
    boolean done200;

    State(){
        reset();
    }

    public void reset(){
        this.counter = 0;
        this.done4 = false;
        this.done200 = false;
    }

    public boolean isAtLeastOneDone(){
        return this.done4 || this.done200;
    }
}
