package coin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by bcotm on 2017/6/20.
 */
public class UrlPool {
    public static final String configFile = "src/coin/urls.txt";
    private List<String> urlList = new ArrayList<String>();
    public static final UrlPool getUrlPoolInstance(){
        return UrlPoolHolder.instance;
    }

    public void readUrlList() throws IOException {
        // easiest solution
        if(urlList.size()>0) return;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
        String data = null;
        while((data = br.readLine())!=null) {
            if(urlList.contains(data)) continue;
            System.out.println(data+"/chaincode");
            urlList.add(data+"/chaincode");
        }
    }

    public String getUrl(){
        Random rand = new Random();
        int index = rand.nextInt()%(urlList.size());
        return urlList.get(index);
    }

    private static class UrlPoolHolder{
        private static final UrlPool instance = new UrlPool();
    }
}
