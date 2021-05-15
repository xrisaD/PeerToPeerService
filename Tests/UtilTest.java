
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class UtilTest {

    @org.junit.Test
    public void divideBytesArrayWithSizeGreaterThanOne() {
        byte[] bytes = {1,2,3,4,5};
        byte[][] outBytes = Util.divide(bytes, 2);
        // [[1 2] [3 4] [5]]
        assertEquals(3, outBytes.length);
        assertEquals(2, outBytes[0].length);
        assertEquals(2, outBytes[1].length);
        assertEquals(1, outBytes[2].length);

        byte[] inBytes = Util.assemble(outBytes);
        assertEquals(bytes.length, inBytes.length);
        boolean areEqual = Arrays.equals(bytes, inBytes);
        assertTrue(areEqual);
    }

    @org.junit.Test
    public void divideBytesArrayWithSizeOne() {
        byte[] bytes = {1};
        byte[][] outBytes = Util.divide(bytes, 2);

        assertEquals(1, outBytes.length);
        assertEquals(1, outBytes[0].length);

        byte[] inBytes = Util.assemble(outBytes);
        assertEquals(bytes.length, inBytes.length);
        boolean areEqual = Arrays.equals(bytes, inBytes);
        assertTrue(areEqual);
    }

    @org.junit.Test
    public void difference() {
        ArrayList<String> allCars = new ArrayList<String>();
        allCars.add("Volvo");
        allCars.add("BMW");
        allCars.add("Ford");
        allCars.add("Mazda");

        ArrayList<String> cars = new ArrayList<String>();
        cars.add("Volvo");

        ArrayList<String> x = Util.difference(allCars, cars);

        assertEquals(3, x.size());

    }

    @org.junit.Test
    public void getNumbersInRangeTest() {
        ArrayList<Integer> x = Util.getNumbersInRange(0,4);
        for (int i = 0; i<x.size(); i++){
            assertEquals(i, (int)x.get(i));
        }
    }

    @org.junit.Test
    public void findOrderTest() {
        ArrayList<Partition> x = new ArrayList<>();
        byte[] bytes1 = {1,2};
        byte[] bytes2 = {3,4};
        byte[] bytes3 = {5,6};
        x.add(new Partition(bytes2, 2));
        x.add(new Partition(bytes3, 3));
        x.add(new Partition(bytes1, 1));

        byte[][] res = Util.findOrder(x);
        assertEquals(3, res.length);
        assertEquals(2, res[0].length);
        assertEquals(2, res[1].length);
        assertEquals(2, res[2].length);

        assertEquals(1,res[0][0]);

        int actual = 1;
        for (int p = 0; p < res.length; p++){
            for (int i = 0; i < res[p].length; i++){
                assertEquals(actual, res[p][i]);
                actual++;
            }
        }
    }

    @org.junit.Test
    public void partitionTest() {
        int partitionSize = 2;
        byte[] file = {1,2,3,4,5};
        byte[][] filePartition = Util.divide(file, partitionSize);
        assertEquals(3, filePartition.length);
        ArrayList<Integer> parts = Util.getNumbersInRange(1, filePartition.length + 1);
        assertEquals(3, parts.size());

    }




}