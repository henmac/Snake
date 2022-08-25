import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Snake {

    static List<Position> oldMoves = new ArrayList<>();
    static List<Position> allPoisonPos = new ArrayList<>();
    static final char actorFood = 'o';
    static final char actorSnake = '\u2588';
    static final char actorPoison = '\u2620';
    //ev. radera
    static Random r = new Random();
    static int foodCounter = 1;
    static int everySecond = 2;

    public static void main(String[] args) throws Exception {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);
        terminal.setForegroundColor(TextColor.ANSI.GREEN);

        updateMenu(terminal);

        Position playerPos = new Position(20,25);
        terminal.setCursorPosition(playerPos.x, playerPos.y);
        terminal.putCharacter(actorSnake);


        Position foodPos = new Position(15,15);
        terminal.setCursorPosition(foodPos.getX(), foodPos.getY());
        terminal.putCharacter(actorFood);

        Position poisonPos = new Position(13,13);
        allPoisonPos.add(poisonPos);
        terminal.setCursorPosition(poisonPos.x, poisonPos.y);
        terminal.putCharacter(actorPoison);



        KeyStroke latestKeyStroke = null;

        while (true) {

            int index = 0;
            int foodCountIndex;
            int gameSpeed = 200;
            for (foodCountIndex = 0; foodCountIndex < foodCounter; foodCountIndex++){
                gameSpeed-= (gameSpeed/10);
                if (gameSpeed <= 0) {
                    gameSpeed = 1;
                }
            }
            KeyStroke keyStroke = null;
            do {
                index++;
                if (index % gameSpeed == 0) {
                    if (latestKeyStroke != null) {
                        playGame(playerPos, latestKeyStroke, terminal);
                        updateMenu(terminal);
                    }
                }
                foodMovement(terminal, playerPos, foodPos);
                poison(playerPos, poisonPos, terminal);
                Thread.sleep(3); // might throw InterruptedException
                keyStroke = terminal.pollInput();
            } while (keyStroke == null);

            Character c = keyStroke.getCharacter(); // used Character instead of char because it might be null
            if (c == Character.valueOf('q')) {
                terminal.close();
                break;
            } else if (c == Character.valueOf('p')) {
                terminal.readInput();
                continue;
            }

            latestKeyStroke = keyStroke;
        }
    }

    private static void playGame (Position player, KeyStroke keyStroke, Terminal terminal) throws Exception {
        // Handle player
        Position oldPosition = new Position(player.x, player.y);
        oldMoves.add(oldPosition);

        switch (keyStroke.getKeyType()) {
            case ArrowDown -> player.y += 1;
            case ArrowUp -> player.y -= 1;
            case ArrowRight -> player.x += 1;
            case ArrowLeft -> player.x -= 1;
        }
        if (player.x > 40) {
            player.x = 0;
        } else if (player.x < 0) {
            player.x = 40;
        }
        if (player.y > 24) {
            player.y = 3;
        } else if (player.y < 3) {
            player.y = 24;
        }
        terminal.setCursorPosition(player.x, player.y);
        terminal.putCharacter(actorSnake);

        for (Position tail : oldMoves) {
            terminal.setCursorPosition(tail.getX(), tail.getY());
            terminal.putCharacter('\u2592');
        }

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

        terminal.flush();
    }
    public static void foodMovement(Terminal terminal, Position player, Position foodPos) throws Exception {
        // check if player runs into the food

        if (foodPos.x == player.x && foodPos.y == player.y) {
            foodPos.x = (r.nextInt(40));
            foodPos.y = (r.nextInt(3, 24));
            terminal.setCursorPosition(foodPos.x, foodPos.y);
            terminal.putCharacter(actorFood);
            terminal.flush();
            foodCounter++;

        }
    }

    public static void poison(Position player, Position poison, Terminal terminal) throws Exception {
        //remove
        int lives = 3;

        //generate poison after every 4th food
        if (foodCounter % everySecond == 0) {
            for (int i = 0; i < 2; i++) {
                Position poisonPos = new Position(r.nextInt(40), r.nextInt(2,24));
                allPoisonPos.add(poisonPos);

                terminal.setCursorPosition(poisonPos.x, poisonPos.y);
                terminal.putCharacter(actorPoison);
            }
            everySecond += 2;
        }
//loss of life and generation of a new poison after eating one
        Position removePoison = null;
        Position addPoison = null;

        for (Position poisonBite : allPoisonPos) {
            if (poisonBite != null && player.x == poisonBite.getX() && player.y == poisonBite.getY()) {
                lives--;
                terminal.bell();
                removePoison = poisonBite;

                Position poisonPos1 = new Position(r.nextInt(40), r.nextInt(2,24));
                addPoison = poisonPos1;

                terminal.setCursorPosition(poisonPos1.x, poisonPos1.y);
                terminal.putCharacter(actorPoison);
            }
        }

        if(removePoison != null) {
            allPoisonPos.add(addPoison);
        }
        if(removePoison != null) {
            allPoisonPos.remove(removePoison);
        }
    }

    private static void updateMenu(Terminal terminal) throws IOException {
        terminal.setCursorPosition(25, 1);

        String message = "* SNAKE Level 1 Food " + (foodCounter-1) + " Lives 3 *";
        for (int i = 0; i < message.length(); i++) {

            terminal.putCharacter(message.charAt(i));
        }
        terminal.setCursorPosition(25, 0);
        for (int i = 0; i < message.length(); i++) {

            terminal.putCharacter('*');
        }
        terminal.setCursorPosition(25, 2);
        for (int i = 0; i < message.length(); i++) {

            terminal.putCharacter('*');
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
