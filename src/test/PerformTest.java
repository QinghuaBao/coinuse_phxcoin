package test;

import address.Address;
import address.AddressUtil;
import coin.CoinFactory;
import coin.PhxCoinInterface;
import coinuse.Controller;
import javafx.fxml.FXML;
import utils.JsonFormatTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bqh on 2017/10/20.
 * <p>
 * E-mail:M201672845@hust.edu.cn
 */
public class PerformTest {
    private PhxCoinInterface post;
    private String iptext = "61.183.76.102";
    private String porttext = "20015";

    public PerformTest() {
        CoinFactory factory = new CoinFactory();
        post = factory.getCoinInstance();
        post.setADD_URL(iptext + ":" + porttext);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        PerformTest performTest = new PerformTest();
        GenerateAddr generateAddr = new GenerateAddr(performTest.post);
        generateAddr.run();
        generateAddr.join();
    }


    public PhxCoinInterface getPost() {
        return post;
    }
}
