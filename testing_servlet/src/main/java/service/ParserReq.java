package service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entity.Account;
import entity.Balance;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.regex.Pattern;


/**
 * Created by USER on 20.07.2017.
 */
public class ParserReq {

    private static Logger logger = Log.getLogger(ParserReq.class);

    private static final String TEG_TYPE = "type";
    private static final String TEG_REGISTER_CUSTOMER = "registerCustomer";
    private static final String TEG_SET_BALANCE = "setBalance";
    private static final String TEG_GET_BALANCE = "getBalance";
    private static final String TEG_LOGIN = "login";
    private static final String TEG_PASSWORD = "password";
    private static final String TEG_BALANCE = "balance";
    private static final String PHONE_FORMAT = "^.+[0-9]{1,2}.([0-9]{3}.)[0-9]{3}-[0-9]{2}-[0-9]{2}$";
    private static final String PASSWORD_FORMAT = "\\w{8,}";

    private static final String CONTEXT_JSON = "application/json";
    private static final String CONTEXT_XML = "application/XML";

    public static String parse(HttpServletRequest request)throws IOException{
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String temp;
        while ((temp = reader.readLine())!=null){
            stringBuilder.append(temp);
        }

        logger.info(stringBuilder.toString());

        if(CONTEXT_JSON.equals(request.getContentType())){
            try {
                return parseJson(stringBuilder.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return jGetError(4);
            }
        }
        if(CONTEXT_XML.equals(request.getContentType())){
            return parseXMLString(stringBuilder.toString());
        }

        return getError(4);
    }

    private static String parseXMLString(String string) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(string.getBytes("UTF-8")));

            Element root = doc.getDocumentElement();
            NodeList list = root.getChildNodes();

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                if (TEG_TYPE.equals(node.getNodeName())) {
                    switch (node.getTextContent()){
                        case TEG_REGISTER_CUSTOMER:
                            return registerCustomer(list);
                        case TEG_SET_BALANCE:
                            return setBalance(list);
                        case TEG_GET_BALANCE:
                            return getBalance(list);
                            default:
                                return getError(4);
                    }

                }
            }

        }catch (SAXException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            return getError(4);
        }
        return getError(4);
    }

    private static String getError(int id){
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<response>\n");
        builder.append("<code>");
        builder.append(String.valueOf(id));
        builder.append("</code>\n");
        builder.append("</response>");
        if(true) return  builder.toString();
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<response>\n" +
                "\n" +
                "<code>"+id+"</code>\n" +
                "\n" +
                "</response>";
    }

    private static String getTotal(float total){
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<response>\n");
        builder.append("<code>0</code>\n");
        builder.append("<balance>");
        builder.append(String.valueOf(total));
        builder.append("</balance>\n");
        builder.append("</response>");
        if(true)return builder.toString();
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<response>\n" +
                "<code>"+0+"</code>\n" +
                "<balance>" + total + "</balance>\n"+
                "</response>";
    }

    private static String registerCustomer(NodeList nodeList) throws Exception {

        String password = null;
        String login = null;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if(node.getNodeType() != Node.ELEMENT_NODE) continue;

            if(TEG_LOGIN.equals(node.getNodeName())) login = node.getTextContent();
            if(TEG_PASSWORD.equals(node.getNodeName())) password = node.getTextContent();
        }
        if(password != null && login != null){
            if(!validatePhone(login)) return getError(2);
            if(!validatePassword(password)) return getError(3);
            if(Service.isFree(login)){
                Balance balance = new Balance(0f);
                Balance eBalance = Service.addBalance(balance);
                Account account = new Account(login, Md5.getMd5Hash(password), eBalance);
                Service.addAccount(account);
                return getError(0);

            }else return getError(1);
        }


        return getError(0);
    }

    private static String setBalance(NodeList nodeList) throws Exception{
        String login = null;
        float total = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if(node.getNodeType() != Node.ELEMENT_NODE) continue;

            if(TEG_LOGIN.equals(node.getNodeName())) login = node.getTextContent();
            if(TEG_BALANCE.equals(node.getNodeName())) total = Float.parseFloat(node.getTextContent());
        }
        if(login != null){

            Account account = Service.getAccountByPhone(login);
            if(account == null) return getError(1);
            account.setBalance(Service.setTotalBalance(account.getBalance(), total));
            return getTotal(account.getBalance().getTotal());
        }


        return getError(4);
    }

    private static String getBalance(NodeList nodeList) throws Exception{
        String password = null;
        String login = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if(node.getNodeType() != Node.ELEMENT_NODE) continue;

            if(TEG_LOGIN.equals(node.getNodeName())) login = node.getTextContent();
            if(TEG_PASSWORD.equals(node.getNodeName())) password = node.getTextContent();
        }
        if(password != null && login != null){
            Account account = Service.getAccountByPhone(login);
            if(account == null) return getError(1);
            if(!account.getPassword().equals(Md5.getMd5Hash(password)))return getError(3);
            return getTotal(account.getBalance().getTotal());
        }
        return getError(4);
    }

    private static boolean validatePhone(String phone){
        Pattern p = Pattern.compile(PHONE_FORMAT);
        return p.matcher(phone).find();
    }

    private static boolean validatePassword(String password){
        Pattern p = Pattern.compile(PASSWORD_FORMAT);
        return p.matcher(password).find();
    }

    private static String parseJson(String request) throws Exception{
        JsonElement element = new JsonParser().parse(request);
        JsonObject req = element.getAsJsonObject();

        String type = req.get(TEG_TYPE).getAsString();
        switch (type){
            case TEG_REGISTER_CUSTOMER:
                return jRegisterCustomer(req);
            case TEG_SET_BALANCE:
                return jSetBalance(req);
            case TEG_GET_BALANCE:
                return jGetBalance(req);
        }
        return jGetError(4);
    }

    private static String jRegisterCustomer(JsonObject req)throws Exception{
        String login = req.get(TEG_LOGIN).getAsString();
        String password = req.get(TEG_PASSWORD).getAsString();
        if(password != null && login != null){
            if(!validatePhone(login)) return jGetError(2);
            if(!validatePassword(password)) return jGetError(3);
            if(Service.isFree(login)){
                Balance balance = new Balance(0f);
                Balance eBalance = Service.addBalance(balance);
                Account account = new Account(login, Md5.getMd5Hash(password), eBalance);
                Service.addAccount(account);
                return jGetError(0);

            }else return jGetError(1);
        }
        return jGetError(0);
    }

    private static String jSetBalance(JsonObject req)throws Exception{
        String login = req.get(TEG_LOGIN).getAsString();
        float total = req.get(TEG_BALANCE).getAsFloat();
        if(login != null){
            Account account = Service.getAccountByPhone(login);
            if(account == null) return jGetError(1);
            account.setBalance(Service.setTotalBalance(account.getBalance(), total));
            return jGetTotal(account.getBalance().getTotal());
        }
        return jGetError(4);
    }

    private static String jGetBalance(JsonObject req)throws Exception{
        String login = req.get(TEG_LOGIN).getAsString();
        String password = req.get(TEG_PASSWORD).getAsString();
        if(password != null && login != null){
            Account account = Service.getAccountByPhone(login);
            if(account == null) return jGetError(1);
            if(!account.getPassword().equals(Md5.getMd5Hash(password)))return jGetError(3);
            return jGetTotal(account.getBalance().getTotal());
        }
        return jGetError(4);
    }

    private static String jGetError(int id){
        StringBuilder builder = new StringBuilder();
        builder.append("{\"code\":");
        builder.append(String.valueOf(id));
        builder.append("}");
        if(true)return builder.toString();
        return "{\"code\":"+id+"}";
    }

    private static String jGetTotal(float total){
        StringBuilder builder = new StringBuilder();
        builder.append("{\"code\":0,\"balance\":");
        builder.append(String.valueOf(total));
        builder.append("}");
        if(true) return builder.toString();
        return "{\"code\":0,\"balance\":"+total+"}";
    }


}
