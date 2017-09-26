package coin;
import java.io.IOException;
import java.util.Scanner;

public class CoinTest {
    public static String ADDR = "R37DAbhzvHjEUBN3bX1k5eSigxkZBmciK";
    public static String PRIBASE64 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQguML2lEXwVVYEkpNN19AcIxQ4AuYBndHrYcTj" +
            "xBMvyb2gBwYFK4EEAAqhRANCAASPJveHbTGv5CEXmib0+XoXMA3xi1aNNC55FlRFwQhgeeLZSpATYvXZ0i33Hknlh9aqEDKDLBrRFBa7oVb5djGS";
    public static String PUBBASE64 = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEjyb3h20xr+QhF5om9Pl6FzAN8YtWjTQueRZURcEIYHni2UqQ" +
            "E2L12dIt9x5J5YfWqhAygywa0RQWu6FW+XYxkg==";


    public static void main(String[] args) throws IOException{
        CoinFactory f = new CoinFactory();
        PhxCoinInterface post = f.getCoinInstance();

        int a = 0;
        String s;
        switch (a){
            case 0:
                //get account
                s = post.getAccountInfo("ga7Lm7i45tDgT26TVEB7XSnhiGoPayFfU");
                System.out.println("case 0 :"+s);
            case 1:
                //get tx by txhash, txhash can be get by calculated or account txout key
                Scanner sc = new Scanner(System.in);
                s = post.getTransaction(sc.nextLine());
                System.out.println("case 1 :" + s);
            case 2:
                //transfer
                //ga7Lm7i45tDgT26TVEB7XSnhiGoPayFfU
                //mQMfz1SqgnnPZgvUgiYsreRhwrYa3FZ2F
                String param = post.getTransferParam("R37DAbhzvHjEUBN3bX1k5eSigxkZBmciK",
                        "ga7Lm7i45tDgT26TVEB7XSnhiGoPayFfU", 100000000000L, "foam",-1, PUBBASE64, PRIBASE64);
                s = post.doTransferByParam(param);
                System.out.println("case 2 :" + s);
            case 3:
                //get account balance
                Long balance = post.getAccountBalance("ga7Lm7i45tDgT26TVEB7XSnhiGoPayFfU");
                System.out.println("case 3 :" + balance);
            case 4:
                //get txout account
                int txoutcount = post.getTxoutCount("R37DAbhzvHjEUBN3bX1k5eSigxkZBmciK");
                System.out.println("case 4 :" + txoutcount);
            case 5:
                //get some txout with acoount
                s = post.getAccountTxout("R37DAbhzvHjEUBN3bX1k5eSigxkZBmciK", 3);
                System.out.println("case 5 :" + s);
        }
    }


//    public static void main(String[] args)throws IOException{
//        /*****************************************************
//         *  Step 1  :   get an instance of PhxCoinInterface  *
//         *****************************************************
//         */
//        CoinFactory f = new CoinFactory();
//        PhxCoinInterface post = f.getCoinInstance();
//        /*****************************************************
//         *  Step 2  :   do whatever you want                 *
//         *****************************************************
//         */
//        /*
//         *  get an account's detail information
//         */
//        String q= post.getAccountInfo("ga7Lm7i45tDgT26TVEB7XSnhiGoPayFfU");
//        System.out.println(q);
//        /*
//         *  App get transfer parameter which will be delivered to backend later
//         */
//        String para=post.getTransferParam("ga7Lm7i45tDgT26TVEB7XSnhiGoPayFfU",
//                "cBpk6HG9AUWYe4iN3eu6hQZHANgsHkRDk",
//                100,
//                "foam",
//                -1,
//                "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEju2f3HW28eNhwjeWo/BDndR8+dQDaD8AfM8vrfv9gFeue5v2+" +
//                        "lnsptPqNAwl8QxVSoB3SvyatzbCQ5gOCarSXg==",
//                "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgcboMcgO+DMl3jxR5FT3CJ4PBcf1Dhzf/" +
//                        "wu7IRlRe182gBwYFK4EEAAqhRANCAASO7Z/cdbbx42HCN5aj8EOd1Hz51ANoPwB8zy+t+/2AV657m/b6Weym0+o0DCXxDFVKgHdK/Jq3NsJDmA4JqtJe");
//        System.out.println(para);
//        /*
//         *  get transaction hash by parameter above
//         */
//        String txHash = post.getTxHash(para);
//        System.out.println("[post.getTxHash]:"+txHash);
//        /*
//         *  do the transaction
//         */
//        String r = post.doTransferByParam(para);
//        System.out.println("[post.doTransferByParam]:"+r);
////        String res = post.doTransfer("ga7Lm7i45tDgT26TVEB7XSnhiGoPayFfU",
////                "cBpk6HG9AUWYe4iN3eu6hQZHANgsHkRDk",
////                100,
////                "foam",
////                -1,
////                "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEju2f3HW28eNhwjeWo/BDndR8+dQDaD8AfM8vrfv9gFeue5v2+" +
////                "lnsptPqNAwl8QxVSoB3SvyatzbCQ5gOCarSXg==",
////                "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgcboMcgO+DMl3jxR5FT3CJ4PBcf1Dhzf/" +
////                "wu7IRlRe182gBwYFK4EEAAqhRANCAASO7Z/cdbbx42HCN5aj8EOd1Hz51ANoPwB8zy+t+/2AV657m/b6Weym0+o0DCXxDFVKgHdK/Jq3NsJDmA4JqtJe");
//        /*
//         *  get another account's detail information
//         */
//        String qr= post.getAccountInfo("cBpk6HG9AUWYe4iN3eu6hQZHANgsHkRDk");
//        System.out.print("[post.getAccountInfo[cBpk6HG9AUWYe4iN3eu6hQZHANgsHkRDk]:"+qr);
//        /*
//         *  get detail information about a transaction, cannot be tx above, because of latency you will ge nothing
//         */
//        String tx = post.getTransaction("c1c6edce07c23732e6fd6789a8e646f8bf0dbe4c50c0d9ea6f875c4a7ba3d44a");
//        System.out.println("[post.getTransaction]:");
//        System.out.println(tx);
//    }
}
