import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adaptive Quiz System for Children.
 * Java Swing frontend + Oracle Database through JDBC.
 *
 * Database tables expected:
 * Student, Subject, Question, Quiz_Attempt, Student_Response, Quiz_Result
 */
public class AdaptiveQuizApp extends JFrame {
    private final StudentPanel studentPanel = new StudentPanel();
    private final QuestionPanel questionPanel = new QuestionPanel();
    private final QuizPanel quizPanel = new QuizPanel();
    private final HistoryPanel historyPanel = new HistoryPanel();

    public AdaptiveQuizApp() {
        super("Adaptive Quiz System for Children");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Student Registration", studentPanel);
        tabs.addTab("Question Management", questionPanel);
        tabs.addTab("Take Quiz", quizPanel);
        tabs.addTab("Results / History", historyPanel);
        tabs.addChangeListener(e -> {
            studentPanel.refreshStudents();
            questionPanel.refreshSubjects();
            quizPanel.refreshSubjects();
            historyPanel.refreshHistory();
        });
        add(tabs);

        studentPanel.refreshStudents();
        questionPanel.refreshSubjects();
        quizPanel.refreshSubjects();
        historyPanel.refreshHistory();
    }

    private static void showError(Component parent, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(parent,
                ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        return p;
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc,
                               int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(field, gbc);
    }

    private static GridBagConstraints constraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    // -------------------- STUDENT REGISTRATION --------------------
    private class StudentPanel extends JPanel {
        private final JTextField name = new JTextField(24);
        private final JTextField age = new JTextField(24);
        private final JTextField className = new JTextField(24);
        private final JTextField village = new JTextField(24);
        private final JTextField searchId = new JTextField(10);
        private final DefaultListModel<String> studentListModel = new DefaultListModel<>();
        private final JList<String> studentList = new JList<>(studentListModel);

        StudentPanel() {
            setLayout(new BorderLayout(10, 10));
            JPanel form = formPanel();
            GridBagConstraints gbc = constraints();
            addRow(form, gbc, 0, "Student name:", name);
            addRow(form, gbc, 1, "Age:", age);
            addRow(form, gbc, 2, "Class:", className);
            addRow(form, gbc, 3, "Village / School:", village);

            JButton register = new JButton("Register Student");
            JButton clear = new JButton("Clear");
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttons.add(register);
            buttons.add(clear);
            gbc.gridx = 1; gbc.gridy = 4;
            form.add(buttons, gbc);

            JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
            search.add(new JLabel("Search by Student ID:"));
            search.add(searchId);
            JButton find = new JButton("Search");
            JButton showAll = new JButton("Show All");
            search.add(find);
            search.add(showAll);

            add(form, BorderLayout.NORTH);
            add(new JScrollPane(studentList), BorderLayout.CENTER);
            add(search, BorderLayout.SOUTH);

            register.addActionListener(e -> registerStudent());
            clear.addActionListener(e -> {
                name.setText(""); age.setText("");
                className.setText(""); village.setText("");
            });
            find.addActionListener(e -> searchStudent());
            showAll.addActionListener(e -> refreshStudents());
        }

        private void registerStudent() {
            String studentName = name.getText().trim();
            if (studentName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter the student's name.");
                return;
            }
            int studentAge;
            try {
                studentAge = age.getText().trim().isEmpty()
                        ? 0 : Integer.parseInt(age.getText().trim());
                if (studentAge < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age must be a valid non-negative number.");
                return;
            }

            String sql = "INSERT INTO Student " +
                    "(student_name, age, class_name, village_school) VALUES (?, ?, ?, ?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, studentName);
                if (age.getText().trim().isEmpty()) ps.setNull(2, Types.NUMERIC);
                else ps.setInt(2, studentAge);
                ps.setString(3, className.getText().trim());
                ps.setString(4, village.getText().trim());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student registered successfully.");
                name.setText(""); age.setText(""); className.setText(""); village.setText("");
                refreshStudents();
            } catch (Exception ex) {
                showError(this, ex);
            }
        }

        private void searchStudent() {
            try {
                int id = Integer.parseInt(searchId.getText().trim());
                String sql = "SELECT student_id, student_name, age, class_name, village_school " +
                        "FROM Student WHERE student_id = ?";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        studentListModel.clear();
                        if (rs.next()) {
                            studentListModel.addElement(formatStudent(rs));
                        } else {
                            studentListModel.addElement("No student found with ID " + id);
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a numeric Student ID.");
            } catch (Exception ex) {
                showError(this, ex);
            }
        }

        private String formatStudent(ResultSet rs) throws SQLException {
            return "ID: " + rs.getInt("student_id")
                    + " | Name: " + rs.getString("student_name")
                    + " | Age: " + rs.getString("age")
                    + " | Class: " + rs.getString("class_name")
                    + " | Village/School: " + rs.getString("village_school");
        }

        void refreshStudents() {
            if (!isDisplayable() && studentListModel == null) return;
            studentListModel.clear();
            String sql = "SELECT student_id, student_name, age, class_name, village_school " +
                    "FROM Student ORDER BY student_id";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) studentListModel.addElement(formatStudent(rs));
            } catch (Exception ex) {
                // A connection error is also shown when the user performs an operation.
                studentListModel.addElement("Could not load students. Check Oracle connection settings.");
            }
        }
    }

    // -------------------- QUESTION MANAGEMENT --------------------
    private class QuestionPanel extends JPanel {
        private final JComboBox<SubjectItem> subject = new JComboBox<>();
        private final JTextField questionText = new JTextField(35);
        private final JTextField optionA = new JTextField(28);
        private final JTextField optionB = new JTextField(28);
        private final JTextField optionC = new JTextField(28);
        private final JTextField optionD = new JTextField(28);
        private final JComboBox<String> correct = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        private final JComboBox<String> difficulty = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        private final JTextArea questionList = new JTextArea();

        QuestionPanel() {
            setLayout(new BorderLayout(8, 8));
            JPanel form = formPanel();
            GridBagConstraints gbc = constraints();
            addRow(form, gbc, 0, "Subject:", subject);
            addRow(form, gbc, 1, "Question:", questionText);
            addRow(form, gbc, 2, "Option A:", optionA);
            addRow(form, gbc, 3, "Option B:", optionB);
            addRow(form, gbc, 4, "Option C:", optionC);
            addRow(form, gbc, 5, "Option D:", optionD);
            addRow(form, gbc, 6, "Correct option:", correct);
            addRow(form, gbc, 7, "Difficulty:", difficulty);

            JButton add = new JButton("Add Question");
            JButton refresh = new JButton("Refresh Questions");
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttons.add(add); buttons.add(refresh);
            gbc.gridx = 1; gbc.gridy = 8; form.add(buttons, gbc);

            questionList.setEditable(false);
            questionList.setLineWrap(true);
            questionList.setWrapStyleWord(true);
            add(form, BorderLayout.NORTH);
            add(new JScrollPane(questionList), BorderLayout.CENTER);
            add.addActionListener(e -> addQuestion());
            refresh.addActionListener(e -> refreshQuestions());
        }

        void refreshSubjects() {
            subject.removeAllItems();
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT subject_id, subject_name FROM Subject ORDER BY subject_name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) subject.addItem(
                        new SubjectItem(rs.getInt(1), rs.getString(2)));
            } catch (Exception ex) {
                // Shown when database operations are attempted.
            }
        }

        private void addQuestion() {
            SubjectItem selected = (SubjectItem) subject.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Add a subject to the Subject table first.");
                return;
            }
            if (questionText.getText().trim().isEmpty()
                    || optionA.getText().trim().isEmpty()
                    || optionB.getText().trim().isEmpty()
                    || optionC.getText().trim().isEmpty()
                    || optionD.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill in the question and all four options.");
                return;
            }
            String sql = "INSERT INTO Question " +
                    "(subject_id, question_text, option_a, option_b, option_c, option_d, " +
                    "correct_option, difficulty) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, selected.id);
                ps.setString(2, questionText.getText().trim());
                ps.setString(3, optionA.getText().trim());
                ps.setString(4, optionB.getText().trim());
                ps.setString(5, optionC.getText().trim());
                ps.setString(6, optionD.getText().trim());
                ps.setString(7, (String) correct.getSelectedItem());
                ps.setString(8, (String) difficulty.getSelectedItem());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Question added successfully.");
                questionText.setText(""); optionA.setText(""); optionB.setText("");
                optionC.setText(""); optionD.setText("");
                refreshQuestions();
            } catch (Exception ex) {
                showError(this, ex);
            }
        }

        private void refreshQuestions() {
            StringBuilder out = new StringBuilder();
            String sql = "SELECT q.question_id, s.subject_name, q.question_text, q.difficulty " +
                    "FROM Question q JOIN Subject s ON s.subject_id=q.subject_id " +
                    "ORDER BY q.question_id";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.append("ID: ").append(rs.getInt(1))
                       .append(" | ").append(rs.getString(2))
                       .append(" | ").append(rs.getString(4)).append("\n")
                       .append(rs.getString(3)).append("\n\n");
                }
                questionList.setText(out.length() == 0 ? "No questions added yet." : out.toString());
            } catch (Exception ex) {
                questionList.setText("Could not load questions. Check Oracle connection settings.");
            }
        }
    }

    // -------------------- TAKE QUIZ --------------------
    private class QuizPanel extends JPanel {
        private final JTextField studentId = new JTextField(8);
        private final JComboBox<SubjectItem> subject = new JComboBox<>();
        private final JComboBox<Integer> questionCount =
                new JComboBox<>(new Integer[]{5, 10, 15, 20});
        private final JLabel status = new JLabel("Start a quiz to begin.");
        private final JLabel questionLabel = new JLabel("Question will appear here.");
        private final JRadioButton[] options = {
                new JRadioButton(), new JRadioButton(),
                new JRadioButton(), new JRadioButton()
        };
        private final ButtonGroup optionGroup = new ButtonGroup();
        private final JButton startButton = new JButton("Start Quiz");
        private final JButton submitButton = new JButton("Submit Answer");
        private final JButton nextButton = new JButton("Next Question");
        private QuestionRecord current;
        private int attemptId;
        private int attempted;
        private int correctCount;
        private int targetCount;
        private String currentDifficulty = "Easy";
        private boolean answerSubmitted;
        private final Set<Integer> usedQuestionIds = new HashSet<>();

        QuizPanel() {
            setLayout(new BorderLayout(10, 10));
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.setBorder(new EmptyBorder(10, 10, 0, 10));
            top.add(new JLabel("Student ID:")); top.add(studentId);
            top.add(new JLabel("Subject:")); top.add(subject);
            top.add(new JLabel("Questions:")); top.add(questionCount);
            top.add(startButton);

            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(new EmptyBorder(20, 24, 20, 24));
            status.setFont(status.getFont().deriveFont(Font.BOLD, 15f));
            questionLabel.setFont(questionLabel.getFont().deriveFont(Font.BOLD, 18f));
            questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            center.add(status); center.add(Box.createVerticalStrut(20));
            center.add(questionLabel); center.add(Box.createVerticalStrut(16));
            for (JRadioButton option : options) {
                option.setAlignmentX(Component.LEFT_ALIGNMENT);
                optionGroup.add(option);
                center.add(option);
                center.add(Box.createVerticalStrut(8));
            }
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(submitButton); bottom.add(nextButton);
            add(top, BorderLayout.NORTH);
            add(center, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            submitButton.setEnabled(false);
            nextButton.setEnabled(false);
            startButton.addActionListener(e -> startQuiz());
            submitButton.addActionListener(e -> submitAnswer());
            nextButton.addActionListener(e -> nextQuestion());
        }

        void refreshSubjects() {
            SubjectItem previous = (SubjectItem) subject.getSelectedItem();
            subject.removeAllItems();
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT subject_id, subject_name FROM Subject ORDER BY subject_name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) subject.addItem(
                        new SubjectItem(rs.getInt(1), rs.getString(2)));
            } catch (Exception ex) {
                // Connection error is displayed if Start Quiz is clicked.
            }
            if (previous != null) {
                for (int i = 0; i < subject.getItemCount(); i++) {
                    if (subject.getItemAt(i).id == previous.id) subject.setSelectedIndex(i);
                }
            }
        }

        private void startQuiz() {
            try {
                int sid = Integer.parseInt(studentId.getText().trim());
                SubjectItem selectedSubject = (SubjectItem) subject.getSelectedItem();
                if (selectedSubject == null) {
                    JOptionPane.showMessageDialog(this, "No subjects found. Add a subject first.");
                    return;
                }
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement check = con.prepareStatement(
                             "SELECT student_name FROM Student WHERE student_id=?")) {
                    check.setInt(1, sid);
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) {
                            JOptionPane.showMessageDialog(this, "Student ID was not found.");
                            return;
                        }
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO Quiz_Attempt (student_id, subject_id) VALUES (?, ?)",
                            new String[]{"ATTEMPT_ID"})) {
                        ps.setInt(1, sid);
                        ps.setInt(2, selectedSubject.id);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) attemptId = keys.getInt(1);
                            else throw new SQLException("Could not retrieve attempt ID.");
                        }
                    }
                }
                targetCount = (Integer) questionCount.getSelectedItem();
                attempted = 0; correctCount = 0; currentDifficulty = "Easy";
                usedQuestionIds.clear();
                current = null;
                startButton.setEnabled(false);
                showNextQuestion();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid numeric Student ID.");
            } catch (Exception ex) {
                showError(this, ex);
            }
        }

        private QuestionRecord findQuestion() throws SQLException {
            SubjectItem selected = (SubjectItem) subject.getSelectedItem();
            String base = "SELECT q.question_id, q.question_text, q.option_a, q.option_b, " +
                    "q.option_c, q.option_d, q.correct_option, q.difficulty " +
                    "FROM Question q WHERE q.subject_id=? " +
                    "AND NOT EXISTS (SELECT 1 FROM Student_Response r " +
                    "WHERE r.attempt_id=? AND r.question_id=q.question_id) ";
            String exact = base + "AND q.difficulty=? ORDER BY q.question_id";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(exact)) {
                ps.setInt(1, selected.id);
                ps.setInt(2, attemptId);
                ps.setString(3, currentDifficulty);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return readQuestion(rs);
                }
            }
            // If no question exists at the requested level, use any unused question
            // in the selected subject so the quiz can continue.
            String fallback = base + "ORDER BY CASE q.difficulty " +
                    "WHEN 'Easy' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END, q.question_id";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(fallback)) {
                ps.setInt(1, selected.id);
                ps.setInt(2, attemptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        QuestionRecord q = readQuestion(rs);
                        currentDifficulty = q.difficulty;
                        return q;
                    }
                }
            }
            return null;
        }

        private QuestionRecord readQuestion(ResultSet rs) throws SQLException {
            return new QuestionRecord(rs.getInt("question_id"),
                    rs.getString("question_text"),
                    rs.getString("option_a"), rs.getString("option_b"),
                    rs.getString("option_c"), rs.getString("option_d"),
                    rs.getString("correct_option"), rs.getString("difficulty"));
        }

        private void showNextQuestion() {
            try {
                if (attempted >= targetCount) {
                    finishQuiz();
                    return;
                }
                current = findQuestion();
                if (current == null) {
                    finishQuiz();
                    return;
                }
                usedQuestionIds.add(current.id);
                questionLabel.setText("<html><div style='width:650px'>"
                        + escapeHtml(current.text) + "</div></html>");
                options[0].setText("A. " + current.a);
                options[1].setText("B. " + current.b);
                options[2].setText("C. " + current.c);
                options[3].setText("D. " + current.d);
                optionGroup.clearSelection();
                for (JRadioButton option : options) option.setEnabled(true);
                answerSubmitted = false;
                status.setText("Question " + (attempted + 1) + " of " + targetCount
                        + "  |  Difficulty: " + current.difficulty);
                submitButton.setEnabled(true);
                nextButton.setEnabled(false);
            } catch (Exception ex) {
                showError(this, ex);
                startButton.setEnabled(true);
            }
        }

        private String escapeHtml(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        private void submitAnswer() {
            if (current == null || answerSubmitted) return;
            String selected = null;
            for (int i = 0; i < options.length; i++) {
                if (options[i].isSelected()) selected = String.valueOf((char) ('A' + i));
            }
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select an answer first.");
                return;
            }
            boolean isCorrect = selected.equalsIgnoreCase(current.correct);
            String sql = "INSERT INTO Student_Response " +
                    "(attempt_id, question_id, selected_option, is_correct) VALUES (?, ?, ?, ?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, attemptId);
                ps.setInt(2, current.id);
                ps.setString(3, selected);
                ps.setInt(4, isCorrect ? 1 : 0);
                ps.executeUpdate();
            } catch (Exception ex) {
                showError(this, ex);
                return;
            }
            attempted++;
            if (isCorrect) {
                correctCount++;
                currentDifficulty = increaseDifficulty(current.difficulty);
                status.setText("Correct! Next difficulty: " + currentDifficulty);
            } else {
                currentDifficulty = decreaseDifficulty(current.difficulty);
                status.setText("Not quite. Correct answer: " + current.correct
                        + "  |  Next difficulty: " + currentDifficulty);
            }
            for (JRadioButton option : options) option.setEnabled(false);
            answerSubmitted = true;
            submitButton.setEnabled(false);
            nextButton.setEnabled(true);
        }

        private String increaseDifficulty(String difficulty) {
            if ("Easy".equals(difficulty)) return "Medium";
            if ("Medium".equals(difficulty)) return "Hard";
            return "Hard";
        }

        private String decreaseDifficulty(String difficulty) {
            if ("Hard".equals(difficulty)) return "Medium";
            if ("Medium".equals(difficulty)) return "Easy";
            return "Easy";
        }

        private void nextQuestion() {
            if (!answerSubmitted) return;
            showNextQuestion();
        }

        private void finishQuiz() {
            try {
                int wrong = attempted - correctCount;
                double score = attempted == 0 ? 0.0 : correctCount * 100.0 / attempted;
                String level;
                if (score >= 80) level = "Excellent";
                else if (score >= 60) level = "Good";
                else if (score >= 40) level = "Needs Practice";
                else level = "Needs Support";

                if (attemptId > 0) {
                    String sql = "INSERT INTO Quiz_Result " +
                            "(attempt_id, total_questions, correct_answers, wrong_answers, score, performance_level) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setInt(1, attemptId);
                        ps.setInt(2, attempted);
                        ps.setInt(3, correctCount);
                        ps.setInt(4, wrong);
                        ps.setDouble(5, score);
                        ps.setString(6, level);
                        ps.executeUpdate();
                    }
                }
                questionLabel.setText("<html><div style='width:650px'>Quiz completed!</div></html>");
                for (JRadioButton option : options) {
                    option.setText(" ");
                    option.setEnabled(false);
                }
                status.setText("Attempted: " + attempted + " | Correct: " + correctCount
                        + " | Wrong: " + wrong + " | Score: "
                        + String.format("%.2f", score) + "% | " + level);
                submitButton.setEnabled(false);
                nextButton.setEnabled(false);
                startButton.setEnabled(true);
                current = null;
                JOptionPane.showMessageDialog(this,
                        "Quiz completed!\nAttempted: " + attempted
                                + "\nCorrect: " + correctCount
                                + "\nWrong: " + wrong
                                + "\nScore: " + String.format("%.2f", score) + "%"
                                + "\nPerformance: " + level,
                        "Quiz Result", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError(this, ex);
                startButton.setEnabled(true);
            }
        }
    }

    // -------------------- RESULTS / HISTORY --------------------
    private class HistoryPanel extends JPanel {
        private final JTextField studentId = new JTextField(8);
        private final JTextArea output = new JTextArea();

        HistoryPanel() {
            setLayout(new BorderLayout(8, 8));
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.setBorder(new EmptyBorder(10, 10, 0, 10));
            top.add(new JLabel("Student ID (blank = all):"));
            top.add(studentId);
            JButton search = new JButton("Show History");
            top.add(search);
            output.setEditable(false);
            output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            add(top, BorderLayout.NORTH);
            add(new JScrollPane(output), BorderLayout.CENTER);
            search.addActionListener(e -> refreshHistory());
        }

        void refreshHistory() {
            String idText = studentId.getText().trim();
            String sql = "SELECT s.student_id, s.student_name, qa.attempt_id, " +
                    "sub.subject_name, qr.total_questions, qr.correct_answers, " +
                    "qr.wrong_answers, qr.score, qr.performance_level " +
                    "FROM Student s JOIN Quiz_Attempt qa ON qa.student_id=s.student_id " +
                    "JOIN Subject sub ON sub.subject_id=qa.subject_id " +
                    "LEFT JOIN Quiz_Result qr ON qr.attempt_id=qa.attempt_id ";
            if (!idText.isEmpty()) sql += "WHERE s.student_id=? ";
            sql += "ORDER BY qa.attempt_id DESC";

            StringBuilder b = new StringBuilder();
            b.append(String.format("%-5s %-18s %-8s %-12s %-8s %-8s %-8s %-9s %s%n",
                    "ID", "STUDENT", "ATTEMPT", "SUBJECT", "TOTAL", "RIGHT", "WRONG", "SCORE", "LEVEL"));
            b.append("-----------------------------------------------------------------------------------------------\n");
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                if (!idText.isEmpty()) ps.setInt(1, Integer.parseInt(idText));
                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        b.append(String.format("%-5d %-18s %-8d %-12s %-8s %-8s %-8s %-9s %s%n",
                                rs.getInt("student_id"),
                                shorten(rs.getString("student_name"), 17),
                                rs.getInt("attempt_id"),
                                shorten(rs.getString("subject_name"), 11),
                                value(rs, "total_questions"),
                                value(rs, "correct_answers"),
                                value(rs, "wrong_answers"),
                                value(rs, "score"),
                                value(rs, "performance_level")));
                    }
                    if (!found) b.append("No quiz attempts found.\n");
                }
                output.setText(b.toString());
            } catch (NumberFormatException ex) {
                output.setText("Student ID must be a number.");
            } catch (Exception ex) {
                output.setText("Could not load history. Check Oracle connection and table setup.\n"
                        + ex.getMessage());
            }
        }

        private String value(ResultSet rs, String column) throws SQLException {
            Object v = rs.getObject(column);
            return v == null ? "-" : v.toString();
        }

        private String shorten(String s, int max) {
            if (s == null) return "";
            return s.length() <= max ? s : s.substring(0, max);
        }
    }

    private static class SubjectItem {
        final int id;
        final String name;
        SubjectItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private static class QuestionRecord {
        final int id;
        final String text, a, b, c, d, correct, difficulty;
        QuestionRecord(int id, String text, String a, String b, String c, String d,
                       String correct, String difficulty) {
            this.id = id; this.text = text; this.a = a; this.b = b; this.c = c;
            this.d = d; this.correct = correct; this.difficulty = difficulty;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new AdaptiveQuizApp().setVisible(true);
        });
    }
}
