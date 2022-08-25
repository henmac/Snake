import com.googlecode.lanterna.terminal.Terminal;

import java.util.Random;

public class Actor {
    private int xPos;
    private int yPos;

    public Actor(int x, int y) {
        this.xPos = x;
        this.yPos = y;
    }

    public void generatePosition() {
        Random rand = new Random();
        xPos = (rand.nextInt(40));
        yPos = (rand.nextInt(2,24));
    }

    public void putCharacter(char actor, Terminal terminal) throws Exception {
        terminal.setCursorPosition(xPos, yPos);
        terminal.putCharacter(actor);
    }



}
