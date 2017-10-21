package test;

import address.Address;
import address.AddressUtil;
import coin.PhxCoinInterface;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
public class GenerateAddr extends Thread{
    private List<Address> list;
    PhxCoinInterface post;

    public GenerateAddr(PhxCoinInterface post) {
        this.post = post;
        list = new ArrayList<>();
    }

    @Override
    public void run(){
        for (int i = 0; i < 600; i++) {
            try {
                list.add(AddressUtil.generatAddress());
            } catch (NoSuchAlgorithmException |NoSuchProviderException |InvalidKeySpecException |InvalidAlgorithmParameterException |IOException e) {
                System.out.println(e.getMessage());
            }
        }

        coinbase(list);
        PrintWriter out = null;
        try {
            File file = new File("test.txt");
            if (!file.exists()){
                file.createNewFile();
            }
            out = new PrintWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < list.size()/2; i++) {
            out.println(list.get(i).getAddress() + "," +list.get(i+300).getAddress()+","+ list.get(i).getPublicKeyBase64()+ "," +list.get(i).getPrivateKeyBase64());
        }
        out.close();
    }

    public void coinbase(List<Address> list){
        for (int i = 0; i < list.size()/2; i++) {
            try {
                post.doCoinbase(list.get(i).getAddress(), list.get(i).getPublicKeyBase64(), 1000000000, -1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(list.get(list.size()-1).getAddress());
    }
}
