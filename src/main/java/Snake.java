import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Snake {

    static List<Position> oldMoves = new ArrayList<>();
    static final char food = 'o';
    static Random r = new Random();
    static int foodCounter = 1;

    public static void main(String[] args) throws Exception {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);

        Position player = new Position(13,13);
        terminal.setCursorPosition(player.x, player.y);
        terminal.putCharacter('\u2588');

        Position foodPos = new Position(r.nextInt(40), r.nextInt(24));
        terminal.setCursorPosition(foodPos.x, foodPos.y);
        terminal.putCharacter(food);

        KeyStroke latestKeyStroke = null;

        while (true) {

            int index = 0;
            int foodCountIndex;
            int gameSpeed = 100;
            for (foodCountIndex = 0; foodCountIndex < foodCounter; foodCountIndex++){
                gameSpeed-=20;
            }
            KeyStroke keyStroke = null;
            do {
                index++;
                if (index % gameSpeed == 0) {
                    if (latestKeyStroke != null) {
                        handlePlayer(player, latestKeyStroke, terminal, foodPos);
                    }
                }
                Thread.sleep(5); // might throw InterruptedException
                keyStroke = terminal.pollInput();
            } while (keyStroke == null);
            latestKeyStroke = keyStroke;
        }
    }

    private static void handlePlayer (Position player, KeyStroke keyStroke, Terminal terminal, Position foodPos) throws Exception {
        // Handle player
        Position oldPosition = new Position(player.x, player.y);
        oldMoves.add(oldPosition);

        switch (keyStroke.getKeyType()) {
            case ArrowDown -> player.y += 1;
            case ArrowUp -> player.y -= 1;
            case ArrowRight -> player.x += 1;
            case ArrowLeft -> player.x -= 1;
        }
        if (player.x > 80) {
            player.x = 1;
        } else if (player.x < 1) {
            player.x = 80;
        }
        if (player.y > 24) {
            player.y = 2;
        } else if (player.y < 2) {
            player.y = 24;
        }
        terminal.setCursorPosition(player.x, player.y);
        terminal.putCharacter('\u2588');

        for (Position tail : oldMoves) {
            if (player.x == tail.getX() && player.y == tail.getY()) {
                terminal.close();
                break;
            }
        }

        if (oldMoves.size() == foodCounter * 3) {
            terminal.setCursorPosition(oldMoves.get(0).getX(), oldMoves.get(0).getY());
            terminal.putCharacter(' ');
            oldMoves.remove(0);
        }
        foodMovement(terminal, player, foodPos);
        terminal.flush();
    }
    public static void foodMovement(Terminal terminal, Position player, Position foodPos) throws Exception {
        // check if player runs into the food

        if (foodPos.x == player.x && foodPos.y == player.y) {
            foodPos.x = (r.nextInt(80));
            foodPos.y = (r.nextInt(24));
            terminal.setCursorPosition(foodPos.x, foodPos.y);
            terminal.putCharacter(food);

            foodCounter++;
            terminal.flush();
        }
    }
}

class Position {
    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
