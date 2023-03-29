package de.lbank.ausbildung;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoboterClientTest {
    private final RoboterClient client = new RoboterClient("namor","127.0.0.1","127.0.0.1");

    @Test
    void testStartAction() {
        Assertions.assertTrue(client.startAction("127.0.0.1","127.0.0.1"));
    }

    @Test
    void testStartLanding() {
        client.startlanding();
        Assertions.assertTrue(client.isGelandet());
    }

    @Test
    void testMove() {
        client.startlanding();
        Position originalPosition = client.getMylandingpos();
        client.move();
        Position newPosition = client.getMylandingpos();
        Assertions.assertNotEquals(originalPosition, newPosition);
    }

    @Test
    void testGenMessdaten() {
        client.startlanding();
        client.move();
        client.genMessdaten();
        Assertions.assertNotNull(client.getMeasure());
    }

    @Test
    void testExit() {
        client.exit();
        Assertions.assertTrue(client.isInterrupted());
    }

    @Test
    void testGenerateLandingPosition() {
        Size size = new Size(3, 3);
        Position position = client.generateLandingPosition(size);
        Assertions.assertNotNull(position);
        Assertions.assertEquals(2, position.getX());
        Assertions.assertEquals(2, position.getY());
        Assertions.assertEquals(Direction.SOUTH, position.getDir());
    }

    @Test
    void testGenerateNewPosition() {
        Size size = new Size(3, 3);
        Position position = new Position(2, 2, Direction.NORTH);
        Position newPosition = client.generatenewPosition(position, 1);
        Assertions.assertNotNull(newPosition);
        Assertions.assertNotEquals(position, newPosition);
        Assertions.assertTrue(newPosition.getX() >= 1 && newPosition.getX() <= 3);
        Assertions.assertTrue(newPosition.getY() >= 1 && newPosition.getY() <= 3);
    }
}
