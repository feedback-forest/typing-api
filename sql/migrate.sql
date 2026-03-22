-- 기존 DB에 누락된 컬럼 추가 마이그레이션 스크립트
-- (테이블을 DROP/재생성하지 않고 컬럼만 추가)

-- consent 테이블에 누락된 컬럼 추가
ALTER TABLE consent
    ADD COLUMN IF NOT EXISTS version INTEGER,
    ADD COLUMN IF NOT EXISTS content TEXT,
    ADD COLUMN IF NOT EXISTS active BIT NOT NULL DEFAULT 1;

-- 기존 consent 데이터의 version, content 초기화
UPDATE consent SET version = 1, content = description WHERE version IS NULL;

-- member 테이블에 누락된 컬럼 추가
ALTER TABLE member
    ADD COLUMN IF NOT EXISTS role VARCHAR(50);

-- 기존 member 데이터 role 초기화 (기존 회원은 USER로)
UPDATE member SET role = 'USER' WHERE role IS NULL;
