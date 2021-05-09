
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class UtilTest {

    @org.junit.Test
    public void divideBytesArrayWithSizeGreaterThanOne() {
        byte[] bytes = {1,2,3,4,5};
        byte[][] outBytes = Util.divide(bytes, 2);

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

}