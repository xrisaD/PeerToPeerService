import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UtilTest {
    
    @org.junit.Test
    public void divideBytesArray() {
        byte[] bytes = {1,2,3,4,5};
        byte[][] outBytes = Util.divide(bytes, 2);

        assertEquals(3, outBytes.length);
        assertEquals(2, outBytes[0].length);
        assertEquals(2, outBytes[1].length);
        assertEquals(1, outBytes[2].length);
    }

}