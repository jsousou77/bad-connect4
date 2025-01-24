import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

public class Connect4 {
  public static JFrame window = new JFrame();
  
  public static int turn = 0;
  public static int[] playerPos = {-1, -1};
  public static int[] cursor = {-1, -1};
  public static int[][] circles = new int[7][6]; // 0 = empty, 1 = red, 2 = yellow
  
  public static JLabel turnLabel = new JLabel("", SwingConstants.CENTER);
  public static JButton left = new JButton("\u25c0");
  public static JButton right = new JButton("\u25b6");
  public static JButton drop = new JButton("Drop");
  
  public static void main(String[] args) {
    window.setLayout(null);
    window.setResizable(false);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setSize(800, 600);
    window.setTitle("Bad Connect 4: Now with winner detection!");
    window.setBackground(Color.white);
    
    for (int x = 0; x < 7; x++) {
      for (int y = 0; y < 6; y++) {
        circles[x][y] = 0;
      }
    }
    
    JButton start = new JButton("Start Game");
    start.setBounds(585, 180, 100, 40);
    window.add(start);
    JButton reset = new JButton("Reset");
    reset.setBounds(585, 250, 100, 40);
    reset.setEnabled(false);
    window.add(reset);
    turnLabel.setBounds(585, 320, 100, 40);
    window.add(turnLabel);
    left.setBounds(70, 80, 50, 38);
    right.setBounds(490, 80, 50, 38);
    drop.setBounds(270, 32, 70, 38);
    
    JPanel panel = new JPanel() {
      public void paintComponent(Graphics g) { // window.repaint() calls everything here again
        Graphics2D canvas = (Graphics2D) g;
        canvas.setColor(Color.blue);
        canvas.fillRect(130, 130, 350, 300); // game board
        canvas.fillRect(125, 430, 15, 50);
        canvas.fillRect(470, 430, 15, 50);
        canvas.fillRect(110, 480, 45, 15);
        canvas.fillRect(455, 480, 45, 15);
        
        for (int x = 0; x < 7; x++) {
          for (int y = 0; y < 6; y++) {
            canvas.setColor(chooseColor(circles[x][y]));
            canvas.fillOval(142 + x * 48, 140 + y * 48, 38, 38); // fill circles w/ correct colors
          }
        }
        
        if (turn != 0) {
          canvas.setColor(chooseColor(turn));
          canvas.fillRect(585, 320, 100, 40); // turn indicator
          canvas.fillOval(142 + playerPos[0] * 48, 80, 38, 38);
        }
      }
    };
    
    panel.setBounds(0, 0, 800, 600);
    window.add(panel);
    window.setVisible(true);
    
    start.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        reset.setEnabled(true);
        start.setEnabled(false);
        turn = 1;
        updateTurn();
        playerPos[0] = 0;
        window.add(left);
        window.add(right);
        window.add(drop);
        move(0);
        window.repaint();
      }
    });
    
    reset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        reset.setEnabled(false);
        start.setEnabled(true);
        turn = 0;
        updateTurn();
        for (int x = 0; x < 7; x++) { // clear out circles
          for (int y = 0; y < 6; y++) {
            circles[x][y] = 0;
          }
        }
        window.remove(left);
        window.remove(right);
        window.remove(drop);
        window.repaint();
      }
    });
    
    left.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        move(-1);
      }
    });
    right.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        move(1);
      }
    });
    
    drop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        playerPos[1] = 0;
        while (circles[playerPos[0]][playerPos[1]] == 0 && playerPos[1] < 5) { // move down until we hit the bottom of the board or another piece
          playerPos[1]++;
        }
        if (circles[playerPos[0]][playerPos[1]] != 0) {
          playerPos[1]--; // idk why i need to do this
        }
        circles[playerPos[0]][playerPos[1]] = turn;
        
        int[] directions = new int[8];
        for (int i = 0; i < 8; i++) {
          int num = sameInARow(i); // check if we have 4 in a row in all 8 directions around piece
          directions[i] = num; // save results of direction checks for later
          if (num >= 4) {
            win();
            return;
          }
        }
        
        for (int i = 0; i < 4; i++) {
          if (directions[i] + directions[i + 4] > 4) { // checks again in 8 directions, but both ways in case we missed it
            win();
            return;
          }
        }
        
        move(0);
        if (turn == 1) {
          turn = 2;
        } else {
          turn = 1;
        }
        updateTurn();
        
        window.repaint();
      }
    });
  }
  
  public static void updateTurn() {
    switch (turn) {
      case 1: turnLabel.setText("Red's Turn"); break;
      case 2: turnLabel.setText("Yellow's Turn"); break;
      default: turnLabel.setText(""); break;
    }
  }
  
  public static Color chooseColor(int c) {
    switch (c) {
      case 1: return Color.red;
      case 2: return Color.yellow;
    }
    return Color.white;
  }
  
  public static void move(int direction) { // handles left and right buttons
    if (direction == -1 && playerPos[0] > 0) {
      playerPos[0]--;
      right.setEnabled(true);
    } else if (direction == 1 && playerPos[0] < 6) {
      playerPos[0]++;
      left.setEnabled(true);
    }
    
    if (playerPos[0] <= 0) {
      left.setEnabled(false);
    } else if (playerPos[0] >= 6) {
      right.setEnabled(false);
    }
    
    if (circles[playerPos[0]][0] != 0) {
      drop.setEnabled(false);
    } else {
      drop.setEnabled(true);
    }
    
    window.repaint();
  }
  
  public static int sameInARow(int direction) {
    cursor[0] = playerPos[0];
    cursor[1] = playerPos[1];
    int num = 0;
    boolean exit = false;
    while (circles[cursor[0]][cursor[1]] == turn && !exit) { // keep moving until the line ends
      switch (direction) { // 0 = up, 1 = up + right, 2 = right, 3 = down + right, 4 = down, 5 = down + left, 6 = left, 7 = up + left
        case 0 -> {
          if (cursor[1] > 0) { // don't try to move out of bounds
            cursor[1]--;
          } else {
            exit = true; // prevents infinite loop
          }
        }
        case 1 -> {
          if (cursor[0] < 6 && cursor[1] > 0) {
            cursor[0]++;
            cursor[1]--;
          } else {
            exit = true;
          }
        }
        case 2 -> {
          if (cursor[0] < 6) {
            cursor[0]++;
          } else {
            exit = true;
          }
        }
        case 3 -> {
          if (cursor[0] < 6 && cursor[1] < 5) {
            cursor[0]++;
            cursor[1]++;
          } else {
            exit = true;
          }
        }
        case 4 -> {
          if (cursor[1] < 5) {
            cursor[1]++;
          } else {
            exit = true;
          }
        }
        case 5 -> {
          if (cursor[0] > 0 && cursor[1] < 5) {
            cursor[0]--;
            cursor[1]++;
          } else {
            exit = true;
          }
        }
        case 6 -> {
          if (cursor[0] > 0) {
            cursor[0]--;
          } else {
            exit = true;
          }
        }
        case 7 -> {
          if (cursor[0] > 0 && cursor[1] > 0) {
            cursor[0]--;
          cursor[1]--;
          } else {
            exit = true;
          }
        }
      }
      num++;
    }
    return num;
  }
  
  public static void win() {
    String name;
    if (turn == 1) {
      name = "Red";
    } else {
      name = "Yellow";
    }
    turn = 0;
    window.remove(left);
    window.remove(right);
    window.remove(drop);
    updateTurn();
    window.repaint();
    JOptionPane.showMessageDialog(window, "Congratulations! " + name + " has won the game!", "We have a winner!", JOptionPane.INFORMATION_MESSAGE);
  }
}
