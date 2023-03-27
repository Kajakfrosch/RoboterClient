package de.lbank.ausbildung;

import com.google.gson.*;

public class ExoplanetParser {
    private Gson gson;
    private JsonParser parser;

    public ExoplanetParser() {
        gson = new Gson();
        parser = new JsonParser();
    }

    public Object parse(String json) {
        JsonElement element = parser.parse(json);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
        JsonObject obj = element.getAsJsonObject();
        String cmd = obj.get("CMD").getAsString();

        switch (cmd) {
            case "init":
                JsonObject size = obj.get("SIZE").getAsJsonObject();
                int width = size.get("WIDTH").getAsInt();
                int height = size.get("HEIGHT").getAsInt();
                return new Size(width, height);
            case "landed":
                JsonObject measure = obj.get("MEASURE").getAsJsonObject();
                Ground ground = Ground.valueOf(measure.get("GROUND").getAsString());
                float temp = (float) measure.get("TEMP").getAsDouble();
                return new Measure(ground, temp);

            case "scaned":
                measure = obj.get("MEASURE").getAsJsonObject();
                ground = Ground.valueOf(measure.get("GROUND").getAsString());
                temp = (float) measure.get("TEMP").getAsDouble();
                return new Measure(ground, temp);

            case "moved":
                JsonObject position = obj.get("POSITION").getAsJsonObject();
                int x = position.get("X").getAsInt();
                int y = position.get("Y").getAsInt();
                Direction direction = Direction.valueOf(position.get("DIRECTION").getAsString());
                return new Position(x, y, direction);

          //  case "mvscaned":
           //     measure = obj.get("MEASURE").getAsJsonObject();
            //    ground = Ground.valueOf(measure.get("GROUND").getAsString());
              //  temp = (float) measure.get("TEMP").getAsDouble();
                //position = obj.get("POSITION").getAsJsonObject();
                //x = position.get("X").getAsInt();
                //y = position.get("Y").getAsInt();
                //direction = Direction.valueOf(position.get("DIRECTION").getAsString());
                //return new Position(x, y, direction);new Measure(ground, temp);
            case "rotated":
                direction = Direction.valueOf(obj.get("DIRECTION").getAsString());
                return direction.toString();

            case "exit":
                return "exit";
            case "error":
                String error = obj.get("ERROR").getAsString();
                return error;
            case "pos":
                position = obj.get("POSITION").getAsJsonObject();
                x = position.get("X").getAsInt();
                y = position.get("Y").getAsInt();
                direction = Direction.valueOf(position.get("DIRECTION").getAsString());
                return new Position(x, y, direction);
        }
        return null;
    };
    public String createOrbitJson(String robotname) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("CMD", "orbit");
        jsonObj.addProperty("NAME", robotname);
        Gson gson = new GsonBuilder().create();
        return gson.toJson(jsonObj);
    }


    // Methode zum Erstellen des "land"-JSON-Objekts
    public String createLandJson(int x, int y, String direction) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("CMD", "land");
        JsonObject positionObj = new JsonObject();
        positionObj.addProperty("X", x);
        positionObj.addProperty("Y", y);
        positionObj.addProperty("DIRECTION", direction);
        jsonObj.add("POSITION", positionObj);
        Gson gson = new GsonBuilder().create();
        return gson.toJson(jsonObj);
    }

    // Methode zum Erstellen des "scan"-JSON-Objekts
    public String createScanJson() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("CMD", "scan");
        Gson gson = new GsonBuilder().create();
        return gson.toJson(jsonObj);
    }

    // Methode zum Erstellen des "move"-JSON-Objekts
    public String createMoveJson() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("CMD", "move");
        Gson gson = new GsonBuilder().create();
        return gson.toJson(jsonObj);
    }

    // Methode zum Erstellen des "rotate"-JSON-Objekts
    public String createRotateJson(String rotation) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("CMD", "rotate");
        jsonObj.addProperty("ROTATION", rotation);
        Gson gson = new GsonBuilder().create();
        return gson.toJson(jsonObj);
    }

    // Methode zum Erstellen des "exit"-JSON-Objekts
    public String createExitJson() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("CMD", "exit");
        Gson gson = new GsonBuilder().create();
        return gson.toJson(jsonObj);
    }


}
