insert into consent(type, description, created_date, modified_date)
values ('TERMS_OF_SERVICE', '서비스 이용 약관', now(), now()),
       ('PRIVACY_POLICY', '개인 정보 처리 방침', now(), now()),
       ('AGE_LIMIT_POLICY', '14세 미만 이용 제한', now(), now());
