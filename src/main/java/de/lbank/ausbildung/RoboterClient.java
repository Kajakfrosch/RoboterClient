package de.lbank.ausbildung;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RoboterClient extends Thread{
    private static String robotname = "Namour";
    private static int x = 1;
    private static int y = 1;
    private Position position;
    private boolean gelandet = false;
    private Measure measure;

    private String gethost = "127.0.0.1";
    private int port = 8150;
    private ExoplanetParser p;
    private Size size;
    private Receiver r;
    private Gson gson;
    private JsonObject jsonObj;
    private RoboterClient q;
    private Position mylandingpos;
    private Receiver2base base;
    public boolean startAction() {
        r = new Receiver(robotname, "127.0.0.1", port);
        base = new Receiver2base(robotname, "127.0.0.1", 1222);
        measure = new Measure();
        p = new ExoplanetParser();
        q = this;
        base.start();
        r.start();
        q.start();

        if (r.isAlive()&& base.isAlive()) {
            return true;
        }
        return false;

    }
    public void startlanding(){
        if(gelandet == false) {
            sendtexttoExo(p.createOrbitJson(robotname));
            //sendtexttoExo("orbit:"+robotname);
            String text = listen2exo();

            size = (Size) p.parse(text);

            sendtexttobase("initworld|pandora|" + size.getHeight() + "|" + size.getWidth() + "|" + robotname);
            mylandingpos = generateLandingPosition(size);
            sendtexttoExo(p.createLandJson(mylandingpos.getX(), mylandingpos.getY(),mylandingpos.getDir().toString() ));
            gelandet = true;
        }else {
            System.out.println("der Roboter ist schon gelandet");
        }

    };
    public void ablauf(){
        startlanding();
        move();
        genMessdaten();
    }
    public void run(){
        while (!Thread.currentThread().isInterrupted()){
        try {
            ablauf();
        }catch (Exception e){
            e.printStackTrace();
        }
        ;}
    }
    public void move(){
        mylandingpos = generatenewPosition(mylandingpos, 1);
        sendtexttoExo(p.createMoveJson());
        sendtexttobase("updateKoordinaten|"+mylandingpos.getX()+"|"+mylandingpos.getY()+"|"+robotname+"|"+mylandingpos.getDir().toString()+"|"+"alive");

    }
    public void genMessdaten(){
        sendtexttoExo(p.createScanJson());
        String text = listen2exo();
        measure= (Measure) p.parse(text);
        sendtexttobase("saveMessdaten|"+ mylandingpos.getX()+"|"+mylandingpos.getY()+"|"+robotname+"|"+measure.getTemperature()+"|"+ measure.getGround());
    }
    public void exit(){
        sendtexttoExo(p.createExitJson());
        System.out.println("Exit");
        sendtexttobase("crashedRoboter|"+robotname);
    }
    public Position generateLandingPosition(Size size){
        String text = "";
        int x = 1;
        int y = 1;
        Position pos = null;
        while (!text.equalsIgnoreCase("1005")){
        sendtexttobase("isChunkFree|"+(size.getHeight()-x)+"|"+(size.getHeight()-y)+"|"+robotname);
        text =listen2base();
            if(!text.equalsIgnoreCase("1005")){
                x = x + 1;
                y = y + 1;
            }else {
                pos = new Position(size.getHeight()-x,size.getWidth()-y,Direction.valueOf("SOUTH"));
            };
        }
        
        
      return pos;
    };
    public Position generatenewPosition(Position pos, int step) {
        int x = pos.getX();
        int y = pos.getY();
        Direction direction = pos.getDir();
        int maxHeight = size.getHeight();
        int maxWidth = size.getWidth();
        String text = "";
        do {
            if (pos.getDir().toString().equals("EAST") && x + step <= maxHeight) {
                x += step; // Nach EAST gehen
            } else if (pos.getDir().toString().equals("WEST") && x - step >= 0) {
                x -= step; // Nach WEST gehen
            } else if (pos.getDir().toString().equals("SOUTH") && y + step <= maxWidth) {
                y += step; // Nach SOUTH gehen
            } else if (pos.getDir().toString().equals("NORTH") && y - step >= 0) {
                y -= step; // Nach NORTH gehen
            }
            if (x >= 0 && x <= maxWidth && y >= 0 && y <= maxWidth) {
                sendtexttobase("isChunkFree|" + x + "|" + y + "|" + robotname);
                text = listen2base();
                if(!text.equalsIgnoreCase("1005")){
                    pos.setDir(Direction.valueOf(getRandomDirection()));
                    sendtexttoExo(p.createRotateJson(pos.getDir().toString()));

                }
            }
        } while (!text.equalsIgnoreCase("1005"));

        return pos;
    }
    ;
    public String getRandomDirection() {
        int rand = (int) (Math.random() * 4); // Zufällige Zahl zwischen 0 und 3 generieren
        String[] directions = {"NORTH", "SOUTH", "WEST", "EAST"}; // Array mit den 4 Richtungen
        return directions[rand]; // Zufällige Richtung zurückgeben
    }
    public boolean stopAction() {
        base.interrupt();
        r.interrupt();
        if (r.isAlive()&&base.isAlive()) {
            return false;
        }
        return true;

    }
    public void sendtexttoExo(String text){
        r.sendtext(text);
    }
    public void sendtexttobase(String text){
        base.sendtext(text);
    }
    public String listen2exo(){
      return r.liste2exo();
    };
    public String listen2base(){
        return base.liste2base();
    };

    public boolean isGelandet() {
        return gelandet;
    }

    public Position getMylandingpos() {
        return mylandingpos;
    }

    public Object getMeasure() {
        return measure;
    }

    // Receiver 2 Exoplanet
    class Receiver extends Thread {

        private Socket remoteRobottoExoplanet;
        private PrintWriter out;
        private BufferedReader in;
        private String name;
        private String host;
        private int port;
        public String liste2exo() {
            String listen = "";
            try {
                boolean done = false;

                String line;
                while ((line = in.readLine()) != null || line.equalsIgnoreCase("/n")) {
                    System.out.println(line);
                    return line;
                }




            } catch (IOException e) {
                e.printStackTrace();
                return "Fehler beim Lesen der Eingabe: " + e.getMessage(); // besser Fehlermeldung zurückgeben;
                }
            return "null";
        }
        public Receiver(String name, String host, int port) {
            this.host = host;
            this.name = name;
            this.port = port;
            try {
                remoteRobottoExoplanet = new Socket(gethost, port);
                out = new PrintWriter(remoteRobottoExoplanet.getOutputStream());
                in = new BufferedReader(new InputStreamReader(remoteRobottoExoplanet.getInputStream()));


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendtext(String text) {

            out.println(text);
            out.flush();
        }

        public void run() {

            while (!Thread.interrupted()) {
                //String read = in.readLine();
                //if(read != null) {
                //  p.parse(read);
                // System.out.println(in.readLine());

            }

        }
    }
    class Receiver2base extends Thread{
        private Socket remoteRobottobase;
        private PrintWriter out;
        private BufferedReader in;
        private String name;
        private String host;
        private int port;

        public Receiver2base(String name, String host, int port) {
            this.host = host;
            this.name = name;
            this.port = port;
            try {
                remoteRobottobase = new Socket(gethost, port);
                out = new PrintWriter(remoteRobottobase.getOutputStream());
                in = new BufferedReader(new InputStreamReader(remoteRobottobase.getInputStream()));


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public String liste2base() {
            try {
                String line;
                while ((line = in.readLine()) != null || line.equalsIgnoreCase("/n")) {
                    System.out.println(line);
                    return line;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return "Fehler";
        }
        public void sendtext(String text) {

            out.println(text);
            out.flush();
        }

        public void run() {

            while (!Thread.interrupted()) {

            }

        }
    }
}

