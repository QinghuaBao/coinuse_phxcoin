package test;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by bqh on 2017/10/21.
 * <p>
 * E-mail:M201672845@hust.edu.cn
 */
public class ResultTest {
    public static void main(String[] args) {
        File file = new File("test.txt");
        try {
            Scanner sc = new Scanner(file);
            PerformTest performTest = new PerformTest();
            while (sc.hasNextLine()){
                String[] strings = sc.nextLine().split(",");
                String s = performTest.getPost().getAccountInfo(strings[1]);
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
