package client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by USER on 23.07.2017.
 */
public class Client {
    //logback
    private static final String INPUT_MESSAGE =
            "Print the number to execute operation\n" +
            "1 - Register new user (LOGIN, PASSWORD)\n" +
            "2 - Set balance to the account (LOGIN, BALANCE)\n" +
            "3 - Get balance of account (LOGIN, PASSWORD)\n" +
            "\"smth else\" - Stop the program\n";

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        if(args.length == 0)
                while (jCycleIteration(reader));
        else
                while (cycleIteration(reader));
    }

    private static boolean cycleIteration(BufferedReader reader){
        int request = 0;
        try {
            System.out.println(INPUT_MESSAGE);
            String temp = reader.readLine();
            String message = null;
            if(temp.equals("1")){
                System.out.println("Enter login:");
                String login = reader.readLine();
                System.out.println("Enter password:");
                String password = reader.readLine();
                message = getRegisterMessage(login, password);
                request = 1;
            }else if(temp.equals("2")){
                System.out.println("Enter login:");
                String login = reader.readLine();
                System.out.println("Enter total");
                String total = reader.readLine();
                message = getSetBalanceMessage(login, total);
                request = 2;
            }else if(temp.equals("3")){
                System.out.println("Enter login:");
                String login = reader.readLine();
                System.out.println("Enter password:");
                String password = reader.readLine();
                message = getGetBalanceMessage(login, password);
                request = 3;
            }else{
                reader.close();
                return false;
            }

            if(message == null){
                System.err.println("SMTH EXCEPTION, try again");//TODO:Unreachable
            }else {
                String result = sendMessage(message,"XML");
                //System.out.println("----------\nRESULT\n"+result+"----------\n");
                parseXMLandPrint(result, request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static String sendMessage(String message, final String CONT_TYPE){
        Socket s = null;
        String result = "ERROR";
        try{

            s = new Socket("localhost", 8080);
            String path = "/mainServlet";
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            w.write(new StringBuilder().append("POST ").append(path).append(" HTTP/1.0\r\n").toString());
            w.write(new StringBuilder().append("Content-Length: ").append(message.length()).append("\r\n").toString());
            w.write(new StringBuilder().append("Content-Type: application/").append(CONT_TYPE).append("\r\n").toString());


//            w.write("POST " + path + " HTTP/1.0\r\n");
//            w.write("Content-Length: " + message.length() + "\r\n");
//            w.write("Content-Type: application/" + CONT_TYPE + "\r\n");
            w.write("\r\n");

            w.write(message);
            w.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String temp = null;
            String tempResult = "";
            boolean flag = false;
            while ((temp = reader.readLine())!= null){
                if(temp.startsWith("<") || temp.startsWith("{"))
                    flag = true;
                if(flag)
                    tempResult += temp + "\n";
            }
            result = tempResult;
            //System.out.println(result);


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(s != null){
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static String getRegisterMessage(final String login, final String password){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
                "<request>\n" +
                "<type>registerCustomer</type>\n" +
                "<login>"+ login +"</login>\n" +
                "<password>"+ password +"</password>\n" +
                "</request>";
    }

    private static String getSetBalanceMessage(final String login, final String total){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
                "<request>\n" +
                "<type>setBalance</type>\n" +
                "<login>"+ login +"</login>\n" +
                "<balance>"+ total +"</balance>\n" +
                "</request>";
    }

    private static String getGetBalanceMessage(final String login, final String password){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
                "<request>\n" +
                "<type>getBalance</type>\n" +
                "<login>"+ login +"</login>\n" +
                "<password>"+ password +"</password>\n" +
                "</request>";
    }

    private static void parseXMLandPrint(final String message, int request){
        try {
            String balance = null;
            String code = null;
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message.getBytes("UTF-8"));
            Document document = builder.parse(byteArrayInputStream);
            Node root = document.getDocumentElement();
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    if(node.getNodeName().equals("code"))code  = node.getTextContent();
                    if(node.getNodeName().equals("balance"))balance = node.getTextContent();
                }
            }
            int codeId = Integer.parseInt(code);
            System.out.println("------------");
            System.out.println("RESULT:\n");
            if(codeId !=0 ){
                printErrorInfo(request, codeId);
            }else{
                if(balance == null){
                    System.out.println("Registration completed successfully");
                }else{
                    System.out.println("Balance:  " + balance);
                }
            }
            System.out.println("------------");



        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printErrorInfo(int request, int id){
        switch (id){

            case 1:
                switch (request){
                    case 1:
                        System.out.println("The login has already used");
                        break;
                    case 2:
                    case 3:
                        System.out.println("User is not found");
                        break;
                }
                break;


            case 2:
                switch (request){
                    case 1:
                        System.out.println("Wrong phone format.   +x/xx(xxx)xxx-xx-xx");
                        break;
                }
                break;


            case 3:
                switch (request){
                    case 1:
                        System.out.println("Weak password. Use more 8 symbols");
                        break;
                    case 2:
                    case 3:
                        System.out.println("Wrong password");
                        break;
                }
                break;
            //used for 4
            default:
                System.out.println("Another exception. Try again");
                break;
        }
    }

    private static boolean jCycleIteration(BufferedReader reader){
        int request = 0;
        try {
            System.out.println(INPUT_MESSAGE);
            String temp = reader.readLine();
            String message = null;
            if(temp.equals("1")) {
                System.out.println("Enter login:");
                String login = reader.readLine();
                System.out.println("Enter password:");
                String password = reader.readLine();
                message = jGetRegisterMessage(login, password);
                request = 1;
            }else if(temp.equals("2")){
                System.out.println("Enter login:");
                String login = reader.readLine();
                System.out.println("Enter total");
                String total = reader.readLine();
                message = jGetSetBalanceMessage(login, total);
                request = 2;
            }else if(temp.equals("3")){
                System.out.println("Enter login:");
                String login = reader.readLine();
                System.out.println("Enter password:");
                String password = reader.readLine();
                message = jGetGetBalanceMessage(login, password);
                request = 3;
            }else{
                reader.close();
                return false;
            }
//            System.out.println("\t\t"+message);
            String result = sendMessage(message, "JSON");
            System.out.println(result);
            jParse(result,request);

        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    private static void jParse(String message, int request){
        JsonElement element = new JsonParser().parse(message);
        JsonObject res = element.getAsJsonObject();
        int codeId = res.get("code").getAsInt();
        System.out.println("------------");
        System.out.println("RESULT:\n");
        if(codeId !=0 ){
            printErrorInfo(request, codeId);
        }else{
            String balance = null;

            try{
                balance = res.get("balance").getAsString();
            }catch (Exception e){

            }

            if(balance == null){
                System.out.println("Registration completed successfully");
            }else{
                System.out.println("Balance:  " + balance);
            }
        }
        System.out.println("------------");
    }

    private static String jGetRegisterMessage(final String login, final String password) {
        return "{\"type\":\"registerCustomer\",\"login\":\""+login+"\",\"password\":\""+password+"\"}";
    }

    private static String jGetSetBalanceMessage(final String login, final String total){
        return "{\"type\":\"setBalance\",\"login\":\""+login+"\",\"balance\":"+total+"}";
    }

    private static String jGetGetBalanceMessage(final String login, final  String password) {
        return "{\"type\":\"getBalance\",\"login\":\""+login+"\",\"password\":\""+password+"\"}";
    }

}
