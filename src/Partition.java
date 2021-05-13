public class Partition {
    public int id;
    public byte[] data;

    public Partition(byte[] data, int id) {
        this.data = data;
        this.id = id;
    }

    public Partition(int id) {
        this.id = id;
    }
}
