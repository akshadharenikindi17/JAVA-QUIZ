package com.quiz;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class QuizController {

    private record Question(
        String text, String a, String b, String c, String d,
        String answer, String difficulty
    ) {}

    private final List<Question> questions = List.of(
        new Question("What is 5 + 3?", "6", "7", "8", "9", "C", "Easy"),
        new Question("What is 12 - 5?", "5", "6", "7", "8", "C", "Easy"),
        new Question("What is 7 × 6?", "36", "42", "48", "54", "B", "Medium"),
        new Question("What is 81 ÷ 9?", "7", "8", "9", "10", "C", "Medium"),
        new Question("What is 15 × 12?", "160", "170", "180", "190", "C", "Hard"),
        new Question("What is 144 ÷ 12?", "10", "11", "12", "13", "C", "Hard")
    );

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        model.addAttribute("started", session.getAttribute("student") != null);
        model.addAttribute("student", session.getAttribute("student"));
        model.addAttribute("finished", Boolean.TRUE.equals(session.getAttribute("finished")));
        model.addAttribute("feedback", session.getAttribute("feedback"));
        model.addAttribute("score", session.getAttribute("score"));
        model.addAttribute("total", questions.size());

        Integer index = (Integer) session.getAttribute("index");
        if (index != null && index < questions.size()
                && !Boolean.TRUE.equals(session.getAttribute("finished"))) {
            Question q = questions.get(index);
            model.addAttribute("question", q);
            model.addAttribute("number", index + 1);
            model.addAttribute("difficulty", session.getAttribute("difficulty"));
        }
        return "index";
    }

    @PostMapping("/start")
    public String start(@RequestParam String student, HttpSession session) {
        if (student == null || student.isBlank()) {
            return "redirect:/";
        }
        session.setAttribute("student", student.trim());
        session.setAttribute("index", 0);
        session.setAttribute("score", 0);
        session.setAttribute("difficulty", 0);
        session.setAttribute("finished", false);
        session.removeAttribute("feedback");
        return "redirect:/";
    }

    @PostMapping("/answer")
    public String answer(@RequestParam String choice, HttpSession session) {
        Integer index = (Integer) session.getAttribute("index");
        if (index == null || index >= questions.size()
                || Boolean.TRUE.equals(session.getAttribute("finished"))) {
            return "redirect:/";
        }

        Question q = questions.get(index);
        boolean correct = q.answer().equals(choice);

        int score = (Integer) session.getAttribute("score");
        int difficulty = (Integer) session.getAttribute("difficulty");

        if (correct) {
            score++;
            difficulty = Math.min(2, difficulty + 1);
            session.setAttribute("feedback", "Correct! Well done.");
        } else {
            difficulty = Math.max(0, difficulty - 1);
            session.setAttribute("feedback", "Not quite. The correct answer is "
                    + q.answer() + ".");
        }

        session.setAttribute("score", score);
        session.setAttribute("difficulty", difficulty);

        index++;
        session.setAttribute("index", index);

        if (index >= questions.size()) {
            session.setAttribute("finished", true);
        }
        return "redirect:/";
    }

    @PostMapping("/restart")
    public String restart(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
