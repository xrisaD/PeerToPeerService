
// a thread that sleeps for 200secs and when it wakes up it sets don200 to true
public class Thread200 extends Thread{

    Peer p;
    Thread200(Peer p){
        this.p = p;
    }
    @Override
    public void run() {
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (p.state){
            p.state.done200 = true;
        }
    }
}
