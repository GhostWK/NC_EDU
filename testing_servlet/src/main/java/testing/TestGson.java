package testing;

import com.google.gson.*;

/**
 * Created by USER on 26.07.2017.
 */
public class TestGson {
    public static void main(String[] args) {
        MyTestGson m = new MyTestGson(1, "asd");

        Gson gson  = new GsonBuilder().setPrettyPrinting().create();
        String s = gson.toJson(m);
        System.out.println(s);

        JsonParser parser = new JsonParser();
        JsonElement e = parser.parse(s);

        JsonObject o = e.getAsJsonObject();
        System.out.println(o.get("id"));
        System.out.println(o.get("name").getAsString());


    }
}


class MyTestGson{
    int id;
    String name;

    public MyTestGson(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
