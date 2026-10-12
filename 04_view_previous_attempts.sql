-- Previous quiz attempts and results for each student
SET LINESIZE 200
SET PAGESIZE 100

SELECT
    s.student_id,
    s.student_name,
    qa.attempt_id,
    sub.subject_name,
    qa.start_time,
    qr.total_questions,
    qr.correct_answers,
    qr.wrong_answers,
    qr.score,
    qr.performance_level
FROM Student s
JOIN Quiz_Attempt qa ON qa.student_id = s.student_id
JOIN Subject sub ON sub.subject_id = qa.subject_id
LEFT JOIN Quiz_Result qr ON qr.attempt_id = qa.attempt_id
ORDER BY s.student_id, qa.attempt_id DESC;
