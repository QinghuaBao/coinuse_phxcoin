package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by bqh on 2017/10/21.
 * <p>
 * E-mail:M201672845@hust.edu.cn
 */
public class TransferTest {
    public static void main(String[] args)throws IOException {
        File file = new File("test.txt");
        if (!file.exists()){
            System.out.println("失败");
            return;
        }

        Scanner sc = new Scanner(file);
        PerformTest performTest = new PerformTest();
        List<String> list = new ArrayList<>();
        while (sc.hasNextLine()){
            String[] strings = sc.nextLine().split(",");
            String str = performTest.getPost().getTransferParam(strings[0], strings[1], 100000, "bqh", -1, strings[2], strings[3]);
            list.add(str);
        }

        //断点，等所有结果获得
        for (int i = 0; i < list.size(); i++) {
            performTest.getPost().doTransferByParam(list.get(i));
        }
    }
}
