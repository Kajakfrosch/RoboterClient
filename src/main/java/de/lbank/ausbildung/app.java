package de.lbank.ausbildung;

import java.util.Locale;
import java.util.Scanner;

public class app {
    public static void main(String[] args) {
        Scanner t = new Scanner(System.in);
        System.out.println("Wie lautet ihr Robotername?");
        RoboterClient r  = new RoboterClient(t.next());

        String abc = t.next();
        while(!abc.equalsIgnoreCase("exit")){

            switch (abc.toLowerCase()){
                case "help":
                    System.out.println("Sie können folgenden " +
                            "Befehle ausführen:" +
                            "help für hilfe" +
                            "status um den Status der Anwendung zubekommen" +
                            "exit um die Anwendung zu Beenden");
                    break;
                case "status":
                    System.out.println("Roboter ist gelandet ?:"+r.isGelandet());
                    break;
                case "exit":
                    System.out.println("die Software wird Beendet");
                    r.stopAction();
                    return;

                default: System.out.println("die Software ist läuft noch");
            }
            abc = t.next();
        }

    }
}
