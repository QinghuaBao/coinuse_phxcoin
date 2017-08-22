package coinuse;

import address.Address;
import address.AddressUtil;
import coin.CoinFactory;
import coin.PhxCoinInterface;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import utils.JsonFormatTool;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    @FXML
    private TextField ipTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField addressTextField;
    @FXML
    private TextField pubKeyTextField;
    @FXML
    private TextField priKeyTextField;
    @FXML
    private TextField coinbaseValueTextField;
    @FXML
    private TextField transferValueTextField;
    @FXML
    private TextField coinbaseUntilTextField;
    @FXML
    private TextField transferUntilTextField;
    @FXML
    private TextField transferAddressTextField;
    @FXML
    private TextArea returnInfo;
    @FXML
    private TextArea parseInfo;

    private PhxCoinInterface post;
    private String address = "R37DAbhzvHjEUBN3bX1k5eSigxkZBmciK";
    private String priKey = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQguML2lEXwVVYEkpNN19AcIxQ4AuYBndHrYcTjxBMvyb2gBwY" +
            "FK4EEAAqhRANCAASPJveHbTGv5CEXmib0+XoXMA3xi1aNNC55FlRFwQhgeeLZSpATYvXZ0i33Hknlh9aqEDKDLBrRFBa7oVb5djGS";
    private String pubKey = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEjyb3h20xr+QhF5om9Pl6FzAN8YtWjTQueRZURcEIYHni2UqQE2L12dIt9x5" +
            "J5YfWqhAygywa0RQWu6FW+XYxkg==";
    private long coinbaseValue = 100000L;
    private long transferValue = 100000L;
    private long coinbaseUntil = -1L;
    private long transferUntil = -1L;
    private String transferAddress = "ga7Lm7i45tDgT26TVEB7XSnhiGoPayFfU";

    @FXML
    private void initialize(){
        returnInfo.setWrapText(true);
        parseInfo.setWrapText(true);
        CoinFactory factory = new CoinFactory();
        post = factory.getCoinInstance();
    }

    @FXML
    private void handleRandom(){
        try {
            Address addr = AddressUtil.generatAddress();
            ipTextField.setText("127.0.0.1");
            portTextField.setText("7051");
            addressTextField.setText(addr.getAddress());
            priKeyTextField.setText(addr.getPrivateKeyBase64());
            pubKeyTextField.setText(addr.getPublicKeyBase64());
            coinbaseValueTextField.setText("100000");
            coinbaseUntilTextField.setText("-1");
            transferAddressTextField.setText("YvLqmtUfYC3XqWLtkaXkLC6fR8fnD2bAY");
            transferUntilTextField.setText("-1");
            transferValueTextField.setText("100000");
            handleConnection();
        } catch (NoSuchAlgorithmException |NoSuchProviderException |InvalidAlgorithmParameterException |IOException |InvalidKeySpecException e) {
            warning("未找到相应算法，请自行填充数据");
        }
    }

    @FXML
    private void handleConnection(){
        if (!isIP(ipTextField.getText()) || !isValidPort(portTextField.getText())){
            warning("ip格式或者端口号有误!");
            return;
        }
        if (!addressTextField.getText().equals("")){
            address = addressTextField.getText();
        }
        if (!priKeyTextField.getText().equals("")){
            priKey = priKeyTextField.getText();
        }
        if (!pubKeyTextField.getText().equals("")){
            pubKey = pubKeyTextField.getText();
        }

        post.setADD_URL(ipTextField.getText() + ":" + portTextField.getText());
        //post.connectToBlockChain();
        returnInfo.setText(JsonFormatTool.formatJson(post.getReturnJson()));
        warning("初始化成功！");
//        if (!flag){
//            warning("连接成功！");
//            flag = true;
//        }
    }

    @FXML
    private void handleQueryAccount(){
//        handleConnection();
        if (!addressTextField.getText().equals("")){
            address = addressTextField.getText();
        }
        try {
            String result = post.getAccountInfo(address);
            returnInfo.setText(JsonFormatTool.formatJson(post.getReturnJson()));
            parseInfo.setText(JsonFormatTool.formatJson(result));
        } catch (IOException e) {
            warning(e.getMessage());
        }
    }

    @FXML
    private void handleQueryPhxCoinInfo(){
        warning("功能暂未开放！");
//        handleConnection();
//        try {
//            String result = post.getPhxCoinInfo();
//            returnInfo.setText(JsonFormatTool.formatJson(post.getReturnJson()));
//            parseInfo.setText(JsonFormatTool.formatJson(result));
//        } catch (IOException e) {
//            warning(e.getMessage());
//        }
    }

    @FXML
    private void handleCoinbase(){
        if (!pubKeyTextField.getText().equals("")){
            pubKey = pubKeyTextField.getText();
        }

        if (!coinbaseValueTextField.getText().equals("")){
            if (isValidNum(coinbaseValueTextField.getText())){
                coinbaseValue = Long.parseLong(coinbaseValueTextField.getText());
            }else{
                warning("非法数值[凤币数量]！");
                return;
            }
        }

        if (!coinbaseUntilTextField.getText().equals("")){
            if (isValidNum(coinbaseUntilTextField.getText())){
                coinbaseUntil = Long.parseLong(coinbaseUntilTextField.getText());
            }else{
                warning("非法数值[时间锁]！");
                return;
            }
        }
        //handleConnection();

        try {
            String result = post.doCoinbase(address, pubKey, coinbaseValue, coinbaseUntil);
            returnInfo.setText(JsonFormatTool.formatJson(post.getReturnJson()));
            parseInfo.setText(JsonFormatTool.formatJson(result));
        } catch (IOException e) {
            warning(e.getMessage());
        }
    }

    @FXML
    private void handleTransfer(){
        if (!priKeyTextField.getText().equals("")){
            priKey = priKeyTextField.getText();
        }
        if (!pubKeyTextField.getText().equals("")){
            pubKey = pubKeyTextField.getText();
        }
//        handleConnection();
        if (!transferValueTextField.getText().equals("")){
            if (isValidNum(transferValueTextField.getText())){
                transferValue = Long.parseLong(transferValueTextField.getText());
            }else{
                warning("非法数值[转账凤币数量]！");
                return;
            }
        }

        if (!transferUntilTextField.getText().equals("")){
            if (isValidNum(transferUntilTextField.getText())){
                transferUntil = Long.parseLong(transferUntilTextField.getText());
            }else{
                warning("非法数值[转账时间锁]！");
                return;
            }
        }

        if (!transferAddressTextField.getText().equals("")){
            transferAddress = transferAddressTextField.getText();
        }

        String param;
        try {
            param = post.getTransferParam(address, transferAddress, transferValue, "foam_java_pc",transferUntil, pubKey, priKey);
            String result = post.doTransferByParam(param);
            returnInfo.setText(JsonFormatTool.formatJson(post.getReturnJson()));
            parseInfo.setText(JsonFormatTool.formatJson(result));
        } catch (IOException e) {
            warning(e.getMessage());
        }
    }


    public static void warning(String contentText){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private boolean isIP(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                +"(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        return mat.matches();
    }

    private boolean isValidNum(String str){
        try {
            Long.parseLong(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean isValidPort(String str){
        try {
            int port = Integer.parseInt(str);
            if (!(port > 0 && port < 65536)){
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
