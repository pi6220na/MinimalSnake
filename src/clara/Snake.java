package clara;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

/** Created by Clara on 3/31/16 */

public class Snake extends TimerTask implements KeyListener {

    boolean blkColor = true;

    int height = 300; int width = 400;   //pixels
    int squareSize = 25;    // was 50

    int xSquares = width/squareSize;
    int ySquares = height/squareSize;

    int score = 0;

    int[] kibble;                //x and y of kibble
    boolean ateKibble = false;

    int gameOver = 0;   // 0 = game playing, greater than 0 = game over. Set by run() to indicate state of game and read by paintComponent() to figure out what to draw - game, or the game over scree, or game won screen?

    int clockTicksToRestart = 6;    //How many ticks after game over before restart?
    int youWin = 10;    // Wait a little longer if user wins the game, to allow time to display 'you win' message

    LinkedList<int[]> snake = new LinkedList<int[]>();
    SnakePanel snakePanel;

    int[] nextMove;   //What to add to x and y of snake head to create new snake head for next move
    int[] currentMove;   //current move made by snake. Need to know current heading to validate next move, to prevent snake reversing into itself.

    public static void main(String args[]) {
        Snake snakeGame = new Snake();
    }

    public Snake() {

        SwingUtilities.invokeLater(new Runnable() {  //An anonymous (un-named) inner class
            @Override
            public void run() {
                resetGame();    //also sets everything up

                JFrame frame = new JFrame();   //create and configure GUI window
                frame.setUndecorated(true);
                frame.setSize(width, height);
                frame.setResizable(false);
                frame.addKeyListener(Snake.this);  //Add containing object as key listener

                snakePanel = new SnakePanel();         // panel will contain graphics
                frame.add(snakePanel);

                frame.setVisible(true);
                java.util.Timer timer = new java.util.Timer();   // set up timer - update game every tick
                timer.scheduleAtFixedRate(Snake.this, 0, 300);   // 300 = update every 300 ms.  Larger number = slower game
            }
        });
    }

    class SnakePanel extends JPanel {

        @Override
        public void paintComponent(Graphics g) {

            super.paintComponent(g);



            g.clearRect(0, 0, width, height);    //Clear panel, fill with black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);

            if (gameOver > 6) {                 // If gameOver indicates game is won, display message
                g.setColor(Color.GREEN);
                g.drawString(">-o~~~~~~~~~~~~~  SNAKE  ~~~~~~~~~~~~~o-<", 50, 50);    //  "art"
                g.drawString("!!!! YOU WON !!! score: " + score, 100, 100);
            }

            else if (gameOver > 0 ) {          // If gameOver indicates game is over (won, lost, whatever) display score and countdown to next game
                g.setColor(Color.GREEN);
                g.drawString(">-o~~~~~~~~~~~~~  SNAKE  ~~~~~~~~~~~~~o-<", 50, 50);

                g.drawString("GAME OVER score: " + score, 120, 100);
                g.drawString("try again in " + (gameOver+1)/2 + "...", 120,150);
                g.drawString("press q to quit", 120, 200);
            }

            else {                             // Game is not over. Draw snake and kibble, wherever they are.
                g.setColor(Color.YELLOW);
                g.fillRect(kibble[0] * squareSize, kibble[1] * squareSize, squareSize, squareSize);

                for (int[] square : snake) {
                    if (blkColor) {
                         g.setColor(Color.WHITE);
                         g.fillRect(square[0] * squareSize, square[1] * squareSize, squareSize, squareSize);
                         blkColor = false;
                    } else {
                        g.setColor(Color.GREEN);
                        g.fillRect(square[0] * squareSize, square[1] * squareSize, squareSize, squareSize);
                        blkColor = true;
                    }
                }
            }
        }
    }


    @Override
    public void run() {    //This is called every clock tick. Update the things.

        if (gameOver > 0) {                         // gameOver > 0 means game is over. If so, decrease by 1.
            if (gameOver == 1) {  resetGame();  }      // If it's almost time to start again, then reset.
            gameOver--;
        }

        else {

            currentMove = nextMove;     //Accept the nextMove value generated by KeyListener

            int headX = snake.get(0)[0];    //Where's the head? New head square is relative to exising head.
            int headY = snake.get(0)[1];

            int[] newHead = {headX + nextMove[0], headY + nextMove[1]};   //create new head

            if (contains(newHead, snake)) {   //Is new head in snake? Snake ran into it's own body, game over.
                gameOver = clockTicksToRestart;                 // A positive value means the game is considered over. If this is positive, run() decreases it by 1 every time to provide a 'countdown' to the next game.
            }

            snake.add(0, newHead);   //Otherwise, add new head to snake

            if (snake.size() == xSquares * ySquares) {    //If snake fills board, then win!
                //you won!
                gameOver = youWin;    //Big value so can be distinguished from gameOver = 6 which means game is lost.  You could change these numbers to change the delay between games.
                return;
            }

            if (!ateKibble) {           //If snake did not eat kibble, remove last element of snake so it appears to move.
                snake.removeLast();     //If snake did eat kibble, don't remove last element so it appears to grow by 1.
            }
            ateKibble = false;   //reset.

            headX = newHead[0];    //Convenience variables for new head x and y
            headY = newHead[1];

            // if snake has run into wall, move snake to opposite side
            if (headX < 0) {
                headX = xSquares;
            }
            if (headY < 0) {
                headY = ySquares;
            }
            if (headX > xSquares) {
                headX = 0;
            }
            if (headY > ySquares) {
                headY = 0;
            }
            // update snake head location with new location
            newHead[0] = headX;
            newHead[1] = headY;

            if (headX == kibble[0] && headY == kibble[1]) {      //Is kibble in same square as snake head? Snake ate kibble.
                score++;                              // increase score
                ateKibble = true;                     // set flag, so snake grows on next clock tick

                do {                                  // Move kibble to any random location, as long as it is not in the snake.
                    kibble = new int[]{(int) (Math.random() * xSquares), (int) (Math.random() * ySquares)};
                } while (contains(kibble, snake));
            }
        }

        snakePanel.repaint();    // And in any case, repaint the snakePanel JPanel to redraw the game in the new state.
    }


    private void resetGame() {        // Set score to 0, make new snake, set move direction, create kibble.
        score = 0;
        snake = new LinkedList<int[]>();    //Create snake
        snake.add(new int[]{2, 2});         // Add two squares
        snake.add(new int[]{1, 2});
        nextMove = new int[]{1, 0};               // Set direction
        currentMove = new int[]{1, 0};

        do  {        //Create kibble in random location anywhere not in the snake.
            kibble = new int[]{(int) (Math.random() * xSquares), (int) (Math.random() * ySquares)};
        } while (contains(kibble, snake)) ;
    }


    public boolean contains(int[] test, LinkedList<int[]> list) {  // Convenience method - is this square X and Y in the list of X and Y points?
        for (int[] square : list) {
            if (Arrays.equals(test, square)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {     // Note this class implements KeyListener

        //If snake's current direction (currentMove) is {0, 1} should not permit {0, -1} to stop snake reversing. same for other directions
        if (e.getKeyCode() == KeyEvent.VK_DOWN && currentMove[1] != -1) {
            nextMove = new int[]{0, 1};
        }
        if (e.getKeyCode() == KeyEvent.VK_UP && currentMove[1] != 1) {
            nextMove = new int[]{0, -1};
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT && currentMove[0] != 1) {
            nextMove = new int[]{-1, 0};
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && currentMove[0] != -1) {
            nextMove = new int[]{1, 0};
        }

        if (e.getKeyChar() == 'q') {     // Quit
            System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}    // Program doesn't need these but KeyListener requires we implement all methods
    @Override
    public void keyTyped(KeyEvent e) {}

}
