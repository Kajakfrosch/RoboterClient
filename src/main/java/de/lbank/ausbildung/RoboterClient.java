package de.lbank.ausbildung;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RoboterClient extends Thread{
    private  String robotname = "Namour";
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
    public RoboterClient(String robotname){
        this.robotname = robotname;
        startAction();
    }
    public boolean startAction() {
        //robotname = robotname + (Math.random()*10);
        r = new Receiver(robotname, "127.0.0.1", port);
        base = new Receiver2base(robotname, "127.0.0.1", 6000);
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

            sendtexttobase("initworld|pandora|" + size.getWidth() + "|" +  size.getHeight()+ "|" + robotname);
            mylandingpos = generateLandingPosition(size);
            sendtexttoExo(p.createLandJson(mylandingpos.getX(), mylandingpos.getY(),mylandingpos.getDir().toString() ));
            gelandet = true;
            text = listen2exo();
            System.out.println("INFO:"+text);
        }else {
            System.out.println("der Roboter ist schon gelandet");
        }

    };
    public void ablauf(){
        startlanding();
        genMessdaten();
        move();
    }
    public void run(){
        while (!Thread.currentThread().isInterrupted()){
        try {
            ablauf();
        }catch (Exception e){
            e.printStackTrace();
            gelandet = false;
            p.createExitJson();
            System.out.println("Bei der verarbeitung ist ein Fehler aufgetreten");
            stopAction();
        }
        ;}
    }
    public void move(){
        mylandingpos = generatenewPosition(mylandingpos, 1);
        sendtexttoExo(p.createMoveJson());

        sendtexttobase("updateKoordinaten|"+mylandingpos.getX()+"|"+mylandingpos.getY()+"|"+robotname+"|"+mylandingpos.getDir().toString()+"|"+"alive");
       // String test =listen2exo();
        String text =listen2exo();
        try {
            String text2 = (String) p.parse(text);
            if (text2.equalsIgnoreCase("crashed")){
                stopAction();
            }
        }catch (Exception e){

        }


    }
    public void genMessdaten(){
        sendtexttoExo(p.createScanJson());
        String text = listen2exo();

            measure = (Measure) p.parse(text);
            sendtexttobase("saveMessdaten|" + mylandingpos.getX() + "|" + mylandingpos.getY() + "|" + robotname + "|" + measure.getTemperature() + "|" + measure.getGround());

            //e.printStackTrace();

    }
    public void exit(){
        sendtexttoExo(p.createExitJson());
        System.out.println("Exit");
        sendtexttobase("crashedRoboter|"+robotname);
    }
    public Position generateLandingPosition(Size size){
        String text = "";
        Double datax = Math.random() * 10;
        Double datay = Math.random() * 10;
        Double newDatax = new Double(datax);
        Double newDatay = new Double(datay);
        int x = newDatax.intValue();
        int y = newDatay.intValue();
        int maxHeight = size.getHeight();
        int maxWidth = size.getWidth();
        while (!(x <=size.getWidth()) || !(y <= size.getHeight())){
            datax = Math.random() * 10;
            datay = Math.random() * 10;
            newDatax = null;
            newDatay = null;
            newDatax = new Double(datax);
            newDatay = new Double(datay);
            x = newDatax.intValue();
            y = newDatay.intValue();


        }
        Position pos = null;
        while (!text.equalsIgnoreCase("1005")){
        sendtexttobase("isChunkFree|"+(size.getHeight()-x)+"|"+(size.getHeight()-y)+"|"+robotname);
        text =listen2base();
            if(!text.equalsIgnoreCase("1005")){
                x = x + 1;
                y = y + 1;
            }else {
                pos = new Position(size.getWidth()-x,(size.getHeight()-y),Direction.EAST);
            };
        }
        
        
      return pos;
    };
    public Position generatenewPosition(@NotNull Position pos, int step) {
        int x = pos.getX();
        int y = pos.getY();
        Direction direction = pos.getDir();
        int maxHeight = size.getHeight();
        int maxWidth = size.getWidth();
        String text = "";
        do {
            if (pos.getDir().toString().equals("EAST") && x + step <= maxWidth) {
                x += step; // Nach EAST gehen
            } else if (pos.getDir().toString().equals("WEST") && x - step >= 0) {
                x -= step; // Nach WEST gehen
            } else if (pos.getDir().toString().equals("NORTH") && y + step <= maxHeight) {
                y += step; // Nach SOUTH gehen
            } else if (pos.getDir().toString().equals("SOUTH") && y - step >= 0) {
                y -= step; // Nach NORTH gehen
            }
            if (x >= 0 && x <= maxWidth && y >= 0 && y <= maxWidth) {
                sendtexttobase("isChunkFree|" + x + "|" + y + "|" + robotname);
                text = listen2base();
                if(!text.equalsIgnoreCase("1005")){
                    pos.setDir(getRandomDirection(pos.getDir(),"Right"));

                    sendtexttoExo(p.createRotateJson("RIGHT"));
                    listen2exo();

                }


            }
        } while (!text.equalsIgnoreCase("1005"));
        pos.setX(x);
        pos.setY(y);
        return pos;
    }
    ;
    public Direction getRandomDirection(Direction d,String richtung) {
        switch (d.toString()){
            case "NORTH":
                if(richtung.equalsIgnoreCase("left")){
                    return Direction.WEST;
                }
                if (richtung.equalsIgnoreCase("right")) {
                    return Direction.EAST;
                }

            case "EAST":
                if(richtung.equalsIgnoreCase("left")){
                    return Direction.NORTH;
                }
                if (richtung.equalsIgnoreCase("right")) {
                    return Direction.SOUTH;
                }
            case "SOUTH":
                if(richtung.equalsIgnoreCase("left")){
                    return Direction.EAST;
                }
                if (richtung.equalsIgnoreCase("right")) {
                    return Direction.WEST;
                }
            case "WEST":
                if(richtung.equalsIgnoreCase("left")){
                    return Direction.SOUTH;
                }
                if (richtung.equalsIgnoreCase("right")) {
                    return Direction.NORTH;
                }
        }
        return Direction.WEST;
    }
    public boolean stopAction() {
        sendtexttobase("crashedRoboter|"+robotname);
        base.interrupt();
        r.interrupt();
        this.interrupt();
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
                while ((line = in.readLine()) != null ) {
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

