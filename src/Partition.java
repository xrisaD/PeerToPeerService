public class Partition implements Comparable<Partition>{
    public int id;
    public byte[] data;

    public Partition(byte[] data, int id) {
        this.data = data;
        this.id = id;
    }

    public Partition(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Partition o) {
        return  this.id > o.id ? +1 : this.id < o.id ? -1 : 0;
    }
}
