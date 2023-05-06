package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class VoCTrainer extends JFrame {
    private final JLabel word;
    private final JLabel Score;
    private final JLabel Point;
    private final JLabel TimerLebel;
    private final JTextField ans;
    private final JButton sbmtBtn;
    private final JButton nxtbtn;
    private final Timer timerObj;
    private Connection conn;
    private String RecentWord; // Currentword
    private int currentScore; // updates Realtime locally
    private JPanel mypanel;
    private int timeRemaining = 60; // in seconds


    public VoCTrainer() {
        // Set up the database connection
        try {
            String url = "jdbc:mysql://localhost:3306/vocabulary";
            String user = "root";
            String password = "local";
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set up the GUI
        mypanel = new JPanel();
        mypanel.setLayout(new GridLayout(5, 7));
        mypanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add some padding
        Font labelFont = new Font("SansSerif", Font.PLAIN, 25); // Use a larger font for the labels
        word = new JLabel();
        word.setFont(labelFont);
        Score = new JLabel();
        Score.setFont(labelFont);

        Point = new JLabel();
        Point.setFont(labelFont);

        ans = new JTextField();
        ans.setFont(labelFont);
        sbmtBtn = new JButton("Submit");
        sbmtBtn.setFont(labelFont);
        nxtbtn = new JButton("Next");
        nxtbtn.setFont(labelFont);

        // Showing Timer.
        TimerLebel = new JLabel("Time :");
        TimerLebel.setFont(new Font("Times New Roman", Font.PLAIN, 30));
        mypanel.add(TimerLebel);

        // line break
        mypanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Setup other  lebels
        mypanel.add(new JLabel("Word:"));
        mypanel.add(word);

        mypanel.add(new JLabel("Word Score:"));
        mypanel.add(Score);
        mypanel.add(new JLabel("Your Points: "));
        mypanel.add(Point);
        mypanel.add(new JLabel("Your Answer:"));
        mypanel.add(ans);


        add(mypanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(sbmtBtn);
        buttonPanel.add(nxtbtn);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add some padding
        add(buttonPanel, BorderLayout.SOUTH);
        pack();

        // Set up button actions

        timerObj = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                if (timeRemaining > 0) {
                    TimerLebel.setText(String.format("Time: %02d:%02d Sec", timeRemaining / 60, timeRemaining % 60));
                } else if (timeRemaining == 0) {
                    timerObj.stop();
                    JOptionPane.showMessageDialog(VoCTrainer.this, "Time's up!");
                    setNextWord();
                    TimerLebel.setText(String.format("Time: %02d:%02d Sec", timeRemaining / 60, timeRemaining % 60));

                }

            }
        });

        sbmtBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chkans();
            }
        });
        ans.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sbmtBtn.doClick();
            }
        });

        nxtbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNextWord();
            }
        });

        // Initialize with first word
        setNextWord();
        setLocationRelativeTo(null);
        setSize(700, 500);
        setTitle("Welcome to Vocabulary Training Program");
        /*
         * Constructor Ended Here.
         */
    }

    public static void main(String[] args) {
        VoCTrainer gui = new VoCTrainer();
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setVisible(true);
    }

    private void startTimer() {
        timeRemaining = 5;
        timerObj.start();


    }

    private void setNextWord() {
        try {
            Statement QueryS = conn.createStatement();
            ResultSet resultSet = QueryS.executeQuery("SELECT * FROM word_list ORDER BY RAND() LIMIT 1");
            if (resultSet.next()) {
                RecentWord = resultSet.getString("word");
                //currentScore = resultSet.getInt("word_score");
                int local = resultSet.getInt("word_score");
                word.setText(RecentWord);
                Score.setText(Integer.toString(local));
                Point.setText(Integer.toString(currentScore));
                ans.setText("");
                startTimer();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void incrementCurrentScore() {
        currentScore += 1;
    }

    private void chkans() {
        String answer = this.ans.getText().trim();
        timerObj.stop();
        if (answer.equalsIgnoreCase(RecentWord)) {
            JOptionPane.showMessageDialog(this, "Correct!");
            try {
                PreparedStatement updateDB = conn.prepareStatement("UPDATE word_list SET word_score = ? WHERE word = ?");
                updateDB.setInt(1, currentScore + 1);
                updateDB.setString(2, RecentWord);
                updateDB.executeUpdate();
                incrementCurrentScore(); // increment current score
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Wrong. correct answer is  " + RecentWord);
        }
        setNextWord();
    }
}
