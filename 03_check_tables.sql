-- Verify tables and sample data
SET LINESIZE 180
SET PAGESIZE 100

PROMPT === Tables in this schema ===
SELECT table_name
FROM user_tables
WHERE table_name IN (
    'STUDENT', 'SUBJECT', 'QUESTION',
    'QUIZ_ATTEMPT', 'STUDENT_RESPONSE', 'QUIZ_RESULT'
)
ORDER BY table_name;

PROMPT === Students ===
SELECT * FROM Student;

PROMPT === Subjects ===
SELECT * FROM Subject;

PROMPT === Questions ===
SELECT question_id, subject_id, question_text, correct_option, difficulty
FROM Question
ORDER BY question_id;

PROMPT === Quiz attempts ===
SELECT * FROM Quiz_Attempt;

PROMPT === Student responses ===
SELECT * FROM Student_Response;

PROMPT === Quiz results ===
SELECT * FROM Quiz_Result;
