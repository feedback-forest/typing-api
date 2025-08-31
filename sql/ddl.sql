-- 타이핑 연습 애플리케이션 MySQL DDL

-- 기존 테이블 삭제 (순서 중요 - FK 관계 고려)
DROP TABLE IF EXISTS typing;
DROP TABLE IF EXISTS member_consent;
DROP TABLE IF EXISTS phrase;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS consent;

-- 1. consent 테이블 생성
CREATE TABLE consent
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    created_date  DATETIME(6),
    modified_date DATETIME(6),
    description   VARCHAR(255),
    type          ENUM ('AGE_LIMIT_POLICY', 'PRIVACY_POLICY', 'TERMS_OF_SERVICE'),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- 2. member 테이블 생성
CREATE TABLE member
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    created_date  DATETIME(6),
    modified_date DATETIME(6),
    kakao_id      VARCHAR(255),
    nickname      VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- 3. phrase 테이블 생성
CREATE TABLE phrase
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    created_date  DATETIME(6),
    modified_date DATETIME(6),
    author        VARCHAR(255),
    sentence      VARCHAR(255),
    title         VARCHAR(255),
    lang          ENUM ('EN', 'KO'),
    type          ENUM ('POEM', 'QUOTE'),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- 4. member_consent 테이블 생성 (중간 테이블)
CREATE TABLE member_consent
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    agreed        BIT    NOT NULL,
    consent_id    BIGINT,
    created_date  DATETIME(6),
    member_id     BIGINT,
    modified_date DATETIME(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- 5. typing 테이블 생성
CREATE TABLE typing
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    acc           DOUBLE,
    cpm           INTEGER,
    max_cpm       INTEGER,
    score         INTEGER,
    wpm           INTEGER,
    created_date  DATETIME(6),
    member_id     BIGINT,
    modified_date DATETIME(6),
    phrase_id     BIGINT,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- 제약조건 추가

-- member 테이블 유니크 제약조건
ALTER TABLE member
    ADD CONSTRAINT UKtqi1nx9ul3nx7guxpqycuvgue UNIQUE (kakao_id);

ALTER TABLE member
    ADD CONSTRAINT UKhh9kg6jti4n1eoiertn2k6qsc UNIQUE (nickname);

-- 외래키 제약조건

-- member_consent 테이블 외래키
ALTER TABLE member_consent
    ADD CONSTRAINT FK7h9xx5o1kgq2qswngcglyxsmm
        FOREIGN KEY (consent_id)
            REFERENCES consent (id);

ALTER TABLE member_consent
    ADD CONSTRAINT FKrd3p8f9mb76xm2g3av9neh24d
        FOREIGN KEY (member_id)
            REFERENCES member (id);

-- typing 테이블 외래키
ALTER TABLE typing
    ADD CONSTRAINT FKqm0uhurd8husw5jxea25d9mib
        FOREIGN KEY (member_id)
            REFERENCES member (id);

ALTER TABLE typing
    ADD CONSTRAINT FKjl4m0l29g290umdwq0idecivy
        FOREIGN KEY (phrase_id)
            REFERENCES phrase (id);

-- 회원 동의 유형 기본 데이터 삽입
insert into consent(type, description, created_date, modified_date)
values ('TERMS_OF_SERVICE', '서비스 이용 약관', now(), now()),
       ('PRIVACY_POLICY', '개인 정보 처리 방침', now(), now()),
       ('AGE_LIMIT_POLICY', '14세 미만 이용 제한', now(), now());
