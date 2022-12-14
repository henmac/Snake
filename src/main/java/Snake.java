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
    static boolean play = true;

    static final char actorFood = 'o';
    static final char actorSnake = '\u2588';
    static final char actorPoison = '\u2620';
    static Random r = new Random();
    static int foodCounter = 1;
    static int everySecond = 2;
    static int lifeCounter = 3;

    public static void main(String[] args) throws Exception {
        startGame();
    }

    public static void startGame() throws Exception {
        foodCounter = 1;
        everySecond = 2;
        lifeCounter = 3;
        play = true;
        oldMoves.clear();
        allPoisonPos.clear();
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);
        terminal.setForegroundColor(TextColor.ANSI.GREEN);

        updateMenu(terminal);

        Position playerPos = new Position(r.nextInt(21,59), r.nextInt(4,22));
        playerPos.putCharacter(actorSnake, terminal);

        Position foodPos = new Position(r.nextInt(21,59), r.nextInt(4,22));
        foodPos.putCharacter(actorFood, terminal);

        Position poisonPos = new Position(r.nextInt(21,59), r.nextInt(4,22));
        allPoisonPos.add(poisonPos);
        poisonPos.putCharacter(actorPoison, terminal);

        KeyStroke latestKeyStroke = null;

        while (play) {

            int index = 0;
            int foodCountIndex;
            int gameSpeed = 150;
            for (foodCountIndex = 0; foodCountIndex < foodCounter; foodCountIndex++){
                gameSpeed -= (gameSpeed/10);
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
                poison(playerPos, poisonPos, terminal, foodPos);
                Thread.sleep(3); // might throw InterruptedException
                keyStroke = terminal.pollInput();
            } while (keyStroke == null);

            Character c = keyStroke.getCharacter(); // used Character instead of char because it might be null
            if (c == Character.valueOf('q')) {
                terminal.close();
                play = false;
            } else if (c == Character.valueOf('p')) {
                terminal.readInput();
                continue;
            }

            latestKeyStroke = keyStroke;
        }
    }
    private static void playGame(Position player, KeyStroke keyStroke, Terminal terminal) throws Exception {
        // Handle player
        Position oldPosition = new Position(player.x, player.y);
        oldMoves.add(oldPosition);

        switch (keyStroke.getKeyType()) {
            case ArrowDown -> player.y += 1;
            case ArrowUp -> player.y -= 1;
            case ArrowRight -> player.x += 1;
            case ArrowLeft -> player.x -= 1;
        }
        if (player.x > 59) {
            player.x = 20;
        } else if (player.x < 20) {
            player.x = 59;
        }
        if (player.y > 22) {
            player.y = 4;
        } else if (player.y < 4) {
            player.y = 22;
        }

        terminal.setCursorPosition(player.x, player.y);
        terminal.putCharacter(actorSnake);

        for (Position tail : oldMoves) {
            terminal.setCursorPosition(tail.getX(), tail.getY());
            terminal.putCharacter('\u2592');
        }

        for (Position tail : oldMoves) {
            if (player.x == tail.getX() && player.y == tail.getY()) {
                loseLife(terminal);
            }
        }

        if (oldMoves.size() == foodCounter * 3) {
            terminal.setCursorPosition(oldMoves.get(0).getX(), oldMoves.get(0).getY());
            terminal.putCharacter(' ');
            oldMoves.remove(0);
        }

        terminal.flush();
    }
    public static void foodMovement(Terminal terminal, Position snake, Position foodPos) throws Exception {
        // check if snake runs into the food


        if (foodPos.x == snake.x && foodPos.y == snake.y) {

            boolean isTaken = true;
            while (isTaken) {
                foodPos.x = (r.nextInt(21, 59));
                foodPos.y = (r.nextInt(4, 22));
                for (Position poisonPos : allPoisonPos) {
                    if (foodPos.x == poisonPos.x && foodPos.y == poisonPos.y) {
                        continue;
                    } else {
                        isTaken = false;
                    }
                }
            }
            terminal.setCursorPosition(foodPos.x, foodPos.y);
            terminal.putCharacter(actorFood);
            terminal.flush();
            foodCounter++;

        }
    }
    public static void poison(Position snake, Position poison, Terminal terminal, Position foodPos) throws Exception {
        //check if position is not taken by food, generate poison after every 2nd food
        if (foodCounter % everySecond == 0) {

            for (int i = 0; i < 2; i++) {

                boolean isTaken = true;
                while (isTaken) {
                    Position poisonPos = new Position(r.nextInt(21, 59), r.nextInt(4, 22));
                    if (foodPos.x == poisonPos.x && foodPos.y == poisonPos.y) {
                        continue;
                    } else {
                        allPoisonPos.add(poisonPos);
                        poisonPos.putCharacter(actorPoison, terminal);
                        isTaken = false;
                    }
                }
            }
            everySecond += 2;
        }
//loss of life and generation of a new poison after eating one
        Position removePoison = null;
        Position addPoison = null;

        for (Position poisonBite : allPoisonPos) {
            if (snake.x == poisonBite.getX() && snake.y == poisonBite.getY()) {
                loseLife(terminal);

                removePoison = poisonBite;
                Position poisonPos1;

                boolean isTaken = true;
                while (isTaken) {
                    poisonPos1 = new Position(r.nextInt(21,59), r.nextInt(4,22));
                    if (foodPos.x == poisonPos1.x && foodPos.y == poisonPos1.y) {
                        continue;
                    } else {
                        poisonPos1.putCharacter(actorPoison,terminal);
                        addPoison = poisonPos1;
                        isTaken = false;
                    }
                }
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
        terminal.setCursorPosition(19, 1);
        String message = "* Food " + (foodCounter - 1);
        for (int i = 0; i < message.length(); i++) {

            terminal.putCharacter(message.charAt(i));

        }
        terminal.setCursorPosition(32, 1);
        String message2 = " Daggmasken 3310 ";
        for (int i = 0; i < message2.length(); i++) {

            terminal.putCharacter(message2.charAt(i));

        }
        terminal.setCursorPosition(51, 1);
        String message3 = " Lives " + lifeCounter + " * ";
        for (int i = 0; i < message3.length(); i++) {

            terminal.putCharacter(message3.charAt(i));

        }
        terminal.setCursorPosition(19, 0);
        for (int i = 0; i < 42; i++) {

            terminal.putCharacter('*');
        }
        terminal.setCursorPosition(19, 2);
        for (int i = 0; i < 42; i++) {

            terminal.putCharacter('*');
        }
        terminal.setCursorPosition(20, 3);
        for (int i = 0; i < 40; i++) {

            terminal.putCharacter('\u2591');
        }terminal.setCursorPosition(20, 24);
        for (int i = 0; i < 40; i++) {

            terminal.putCharacter('\u2591');
        }
        for (int i = 3; i < 25; i++) {
            terminal.setCursorPosition(19, i);
            terminal.putCharacter('\u2591');
        }
        for (int i = 3; i < 25; i++) {
            terminal.setCursorPosition(60, i);
            terminal.putCharacter('\u2591');
        }

    }

    public static void loseLife(Terminal terminal) throws Exception {
        lifeCounter--;
        terminal.bell();

        if (lifeCounter == 0) {

            terminal.setCursorPosition(34, 12);
            String message = "* GAME OVER *";
            for (int i = 0; i < message.length(); i++) {

                terminal.putCharacter(message.charAt(i));
            }
            terminal.setCursorPosition(31, 14);
            String messageAgain = "* PLAY AGAIN? Y/N *";
            for (int i = 0; i < messageAgain.length(); i++) {

                terminal.putCharacter(messageAgain.charAt(i));
            }

            updateMenu(terminal);
            terminal.flush();
            while (true) {
                KeyStroke keyStroke = terminal.readInput();
                Character c = keyStroke.getCharacter(); // used Character instead of char because it might be null
                if (c == Character.valueOf('y')) {
                    terminal.close();
                    play = false;
                    startGame();
                    break;
                } else if (c == Character.valueOf('n')) {
                    terminal.close();
                    play = false;
                    break;
                }
            }
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

    public void putCharacter(char actor, Terminal terminal) throws Exception {
        terminal.setCursorPosition(x, y);
        terminal.putCharacter(actor);
    }
}
