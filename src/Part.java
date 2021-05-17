public class Part implements Comparable<Part>{
    public int id;
    public byte[] data;

    public Part(byte[] data, int id) {
        this.data = data;
        this.id = id;
    }

    public Part(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Part o) {
        return  this.id > o.id ? +1 : this.id < o.id ? -1 : 0;
    }
}
