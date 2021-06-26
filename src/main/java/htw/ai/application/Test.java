package htw.ai.application;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : LoraProtocolInterface
 * @version : 1.0
 * @since : 14-06-2021
 **/
public class Test {
    public static void main(String[] args) {
//        byte Ankommend = 127;
//        byte Momentan = -128;
//        System.out.println((byte) (Ankommend - Momentan));


        long start = System.currentTimeMillis();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();

        long diff = end - start;
        System.out.println(diff);

        System.out.println((byte) (180000 / 1000));
    }
}
