-- Sample data for Adaptive Quiz System
-- Run 01_create_tables.sql first.
-- This script assumes the tables are empty.

INSERT INTO Student (student_name, age, class_name, village_school)
VALUES ('Ananya', 10, '5th Class', 'Nalgonda');

INSERT INTO Student (student_name, age, class_name, village_school)
VALUES ('Rahul', 11, '6th Class', 'Warangal');

INSERT INTO Subject (subject_name) VALUES ('Maths');
INSERT INTO Subject (subject_name) VALUES ('Science');

-- Use subject names in a subquery so this works even if identity IDs differ.
INSERT INTO Question (
    subject_id, question_text, option_a, option_b, option_c, option_d,
    correct_option, difficulty
) VALUES (
    (SELECT subject_id FROM Subject WHERE subject_name = 'Maths'),
    'What is 2 + 3?', '4', '5', '6', '7', 'B', 'Easy'
);

INSERT INTO Question (
    subject_id, question_text, option_a, option_b, option_c, option_d,
    correct_option, difficulty
) VALUES (
    (SELECT subject_id FROM Subject WHERE subject_name = 'Maths'),
    'What is 12 multiplied by 8?', '86', '94', '96', '98', 'C', 'Medium'
);

INSERT INTO Question (
    subject_id, question_text, option_a, option_b, option_c, option_d,
    correct_option, difficulty
) VALUES (
    (SELECT subject_id FROM Subject WHERE subject_name = 'Maths'),
    'What is the square root of 144?', '10', '11', '12', '14', 'C', 'Hard'
);

INSERT INTO Question (
    subject_id, question_text, option_a, option_b, option_c, option_d,
    correct_option, difficulty
) VALUES (
    (SELECT subject_id FROM Subject WHERE subject_name = 'Science'),
    'Which planet is known as the Red Planet?',
    'Earth', 'Mars', 'Venus', 'Jupiter', 'B', 'Easy'
);

INSERT INTO Question (
    subject_id, question_text, option_a, option_b, option_c, option_d,
    correct_option, difficulty
) VALUES (
    (SELECT subject_id FROM Subject WHERE subject_name = 'Science'),
    'Which gas do plants absorb from the air?',
    'Oxygen', 'Nitrogen', 'Carbon dioxide', 'Hydrogen', 'C', 'Medium'
);

COMMIT;

PROMPT Sample students, subjects, and questions inserted.
