-- 5 Member Info Insert
INSERT INTO member (id, kakao_id, nickname)
VALUES (1, '3942518969', 'user1');
INSERT INTO member (id, kakao_id, nickname)
VALUES (2, '7442532469', 'user2');
INSERT INTO member (id, kakao_id, nickname)
VALUES (3, '1234518969', 'user3');
INSERT INTO member (id, kakao_id, nickname)
VALUES (4, '3232518969', 'user4');
INSERT INTO member (id, kakao_id, nickname)
VALUES (5, '4536218969', 'user5');

-- 1 Phrase Info Insert
INSERT INTO phrase (sentence, title, author, lang, types)
VALUES ('좋은 아침이야', '안녕', 'kim', 'ko', 'quote');

-- user1 Typing Info (12)
INSERT INTO typing (id, member_id, phrase_id, cpm, acc, wpm, max_wpm, created_date, modified_date)
VALUES (1, 1, 1, 120, 95, 105, 120, '2025-01-15 00:00:00', '2025-01-15 00:00:00'),
       (2, 1, 1, 110, 90, 100, 120, '2025-02-20 00:00:00', '2025-02-20 00:00:00'),
       (3, 1, 1, 100, 85, 95, 120, '2025-03-05 00:00:00', '2025-03-05 00:00:00'),
       (4, 1, 1, 130, 92, 115, 120, '2025-01-25 00:00:00', '2025-01-25 00:00:00'),
       (5, 1, 1, 125, 88, 110, 120, '2025-02-10 00:00:00', '2025-02-10 00:00:00'),
       (6, 1, 1, 115, 93, 108, 120, '2025-03-12 00:00:00', '2025-03-12 00:00:00'),
       (7, 1, 1, 105, 90, 98, 120, '2025-01-18 00:00:00', '2025-01-18 00:00:00'),
       (8, 1, 1, 120, 89, 102, 120, '2025-02-02 00:00:00', '2025-02-02 00:00:00'),
       (9, 1, 1, 140, 94, 120, 120, '2025-03-01 00:00:00', '2025-03-01 00:00:00'),
       (10, 1, 1, 135, 91, 113, 120, '2025-02-28 00:00:00', '2025-02-28 00:00:00'),
       (11, 1, 1, 110, 87, 100, 120, '2025-01-30 00:00:00', '2025-01-30 00:00:00'),
       (12, 1, 1, 125, 92, 112, 120, '2025-03-10 00:00:00', '2025-03-10 00:00:00');
-- user2 Typing Info (12)
INSERT INTO typing (id, member_id, phrase_id, cpm, acc, wpm, max_wpm, created_date, modified_date)
VALUES (13, 2, 1, 130, 90, 110, 120, '2025-02-05 00:00:00', '2025-02-05 00:00:00'),
       (14, 2, 1, 120, 85, 100, 115, '2025-01-28 00:00:00', '2025-01-28 00:00:00'),
       (15, 2, 1, 125, 92, 108, 118, '2025-03-03 00:00:00', '2025-03-03 00:00:00'),
       (16, 2, 1, 135, 91, 115, 125, '2025-02-18 00:00:00', '2025-02-18 00:00:00'),
       (17, 2, 1, 140, 94, 118, 130, '2025-03-07 00:00:00', '2025-03-07 00:00:00'),
       (18, 2, 1, 110, 87, 102, 112, '2025-01-22 00:00:00', '2025-01-22 00:00:00'),
       (19, 2, 1, 105, 86, 97, 107, '2025-02-12 00:00:00', '2025-02-12 00:00:00'),
       (20, 2, 1, 115, 90, 105, 115, '2025-03-08 00:00:00', '2025-03-08 00:00:00'),
       (21, 2, 1, 120, 89, 109, 117, '2025-01-17 00:00:00', '2025-01-17 00:00:00'),
       (22, 2, 1, 125, 93, 113, 121, '2025-02-25 00:00:00', '2025-02-25 00:00:00'),
       (23, 2, 1, 130, 88, 110, 118, '2025-01-10 00:00:00', '2025-01-10 00:00:00'),
       (24, 2, 1, 120, 91, 106, 116, '2025-03-02 00:00:00', '2025-03-02 00:00:00');
-- user3 Typing Info (12)
INSERT INTO typing (id, member_id, phrase_id, cpm, acc, wpm, max_wpm, created_date, modified_date)
VALUES (25, 3, 1, 110, 90, 100, 105, '2025-02-14 00:00:00', '2025-02-14 00:00:00'),
       (26, 3, 1, 120, 92, 106, 112, '2025-03-09 00:00:00', '2025-03-09 00:00:00'),
       (27, 3, 1, 125, 88, 105, 110, '2025-01-11 00:00:00', '2025-01-11 00:00:00'),
       (28, 3, 1, 135, 94, 115, 120, '2025-03-15 00:00:00', '2025-03-15 00:00:00'),
       (29, 3, 1, 130, 90, 110, 115, '2025-02-08 00:00:00', '2025-02-08 00:00:00'),
       (30, 3, 1, 115, 86, 101, 106, '2025-01-05 00:00:00', '2025-01-05 00:00:00'),
       (31, 3, 1, 120, 91, 107, 112, '2025-02-18 00:00:00', '2025-02-18 00:00:00'),
       (32, 3, 1, 125, 93, 112, 117, '2025-03-04 00:00:00', '2025-03-04 00:00:00'),
       (33, 3, 1, 110, 84, 97, 102, '2025-01-20 00:00:00', '2025-01-20 00:00:00'),
       (34, 3, 1, 130, 87, 109, 114, '2025-03-10 00:00:00', '2025-03-10 00:00:00'),
       (35, 3, 1, 120, 95, 113, 118, '2025-02-28 00:00:00', '2025-02-28 00:00:00'),
       (36, 3, 1, 125, 92, 111, 116, '2025-03-01 00:00:00', '2025-03-01 00:00:00');
-- user4 Typing Info (12)
INSERT INTO typing (id, member_id, phrase_id, cpm, acc, wpm, max_wpm, created_date, modified_date)
VALUES (37, 4, 1, 140, 94, 118, 125, '2025-03-06 00:00:00', '2025-03-06 00:00:00'),
       (38, 4, 1, 130, 90, 110, 115, '2025-01-21 00:00:00', '2025-01-21 00:00:00'),
       (39, 4, 1, 120, 86, 105, 110, '2025-02-03 00:00:00', '2025-02-03 00:00:00'),
       (40, 4, 1, 115, 92, 108, 113, '2025-03-14 00:00:00', '2025-03-14 00:00:00'),
       (41, 4, 1, 110, 85, 98, 102, '2025-02-06 00:00:00', '2025-02-06 00:00:00'),
       (42, 4, 1, 125, 90, 108, 112, '2025-01-18 00:00:00', '2025-01-18 00:00:00'),
       (43, 4, 1, 140, 92, 120, 125, '2025-03-11 00:00:00', '2025-03-11 00:00:00'),
       (44, 4, 1, 130, 91, 111, 116, '2025-02-17 00:00:00', '2025-02-17 00:00:00'),
       (45, 4, 1, 120, 88, 104, 109, '2025-03-13 00:00:00', '2025-03-13 00:00:00'),
       (46, 4, 1, 135, 89, 113, 118, '2025-01-29 00:00:00', '2025-01-29 00:00:00'),
       (47, 4, 1, 110, 90, 100, 105, '2025-02-22 00:00:00', '2025-02-22 00:00:00'),
       (48, 4, 1, 120, 92, 107, 112, '2025-01-12 00:00:00', '2025-01-12 00:00:00');
-- user5 Typing Info (12)
INSERT INTO typing (id, member_id, phrase_id, cpm, acc, wpm, max_wpm, created_date, modified_date)
VALUES (49, 5, 1, 110, 90, 101, 105, '2025-02-14 00:00:00', '2025-02-14 00:00:00'),
       (50, 5, 1, 120, 95, 110, 115, '2025-01-25 00:00:00', '2025-01-25 00:00:00'),
       (51, 5, 1, 130, 88, 107, 112, '2025-03-05 00:00:00', '2025-03-05 00:00:00'),
       (52, 5, 1, 115, 93, 104, 110, '2025-01-30 00:00:00', '2025-01-30 00:00:00'),
       (53, 5, 1, 140, 90, 120, 125, '2025-03-03 00:00:00', '2025-03-03 00:00:00'),
       (54, 5, 1, 125, 91, 112, 118, '2025-02-20 00:00:00', '2025-02-20 00:00:00'),
       (55, 5, 1, 120, 89, 108, 113, '2025-01-10 00:00:00', '2025-01-10 00:00:00'),
       (56, 5, 1, 135, 94, 116, 120, '2025-02-28 00:00:00', '2025-02-28 00:00:00'),
       (57, 5, 1, 130, 92, 114, 118, '2025-03-07 00:00:00', '2025-03-07 00:00:00'),
       (58, 5, 1, 115, 86, 105, 110, '2025-01-18 00:00:00', '2025-01-18 00:00:00'),
       (59, 5, 1, 125, 91, 110, 115, '2025-02-12 00:00:00', '2025-02-12 00:00:00'),
       (60, 5, 1, 120, 90, 107, 112, '2025-03-01 00:00:00', '2025-03-01 00:00:00');
