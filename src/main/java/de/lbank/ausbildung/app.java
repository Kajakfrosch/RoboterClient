package de.lbank.ausbildung;

import java.util.Locale;
import java.util.Scanner;

public class app {
    public static void main(String[] args) {
        Scanner t = new Scanner(System.in);
        System.out.println("Wie lautet ihr Robotername?");
        String robotername = t.next();
        System.out.println("Wie lautet die IPv4 Adresse vom Exoplanet?");
        String ipadresseExoplanet = t.next();
        System.out.println("Wie lautet die IPv4 Adresse von der Basisstation?");
        String ipadresseBasisstation = t.next();
        RoboterClient r;
        if(!robotername.isBlank() && !ipadresseExoplanet.isBlank() && !ipadresseBasisstation.isBlank()) {
             r = new RoboterClient(robotername,ipadresseBasisstation,ipadresseExoplanet);
        }else {
            System.out.println("die eingaben waren ungültig" +
                    "bitte geben sie alle daten nochmals ein");
            System.out.println("Wie lautet ihr Robotername?");
            robotername = t.next();
            System.out.println("Wie lautet die IPv4 Adresse vom Exoplanet?");
            ipadresseExoplanet = t.next();
            System.out.println("Wie lautet die IPv4 Adresse von der Basisstation?");
            ipadresseBasisstation = t.next();
            if (!robotername.isBlank() && !ipadresseExoplanet.isBlank() && !ipadresseBasisstation.isBlank()) {
                 r = new RoboterClient(robotername,ipadresseBasisstation,ipadresseExoplanet);
            }else return;

        }
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
