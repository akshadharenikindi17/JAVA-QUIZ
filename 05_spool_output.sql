-- Save SQL*Plus output to a text file.
-- Make sure D:\25251A05T7 already exists.
SPOOL D:\25251A05T7\adaptive_quiz_output.txt

SET LINESIZE 180
SET PAGESIZE 100

SELECT table_name
FROM user_tables
WHERE table_name IN (
    'STUDENT', 'SUBJECT', 'QUESTION',
    'QUIZ_ATTEMPT', 'STUDENT_RESPONSE', 'QUIZ_RESULT'
)
ORDER BY table_name;

SELECT * FROM Student;
SELECT * FROM Subject;
SELECT question_id, subject_id, question_text, correct_option, difficulty
FROM Question
ORDER BY question_id;

SPOOL OFF
