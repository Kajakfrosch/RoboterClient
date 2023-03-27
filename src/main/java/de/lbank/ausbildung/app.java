package de.lbank.ausbildung;

import java.util.Scanner;

public class app {
    public static void main(String[] args) {
        Scanner t = new Scanner(System.in);
        RoboterClient r  = new RoboterClient();
        r.startAction();

        while(!t.next().equalsIgnoreCase("exit")){
            System.out.println("Roboterläuft");
            t.next();
        }
        r.stopAction();
    }
}
