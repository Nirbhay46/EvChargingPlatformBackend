-- Envers revision info table
CREATE TABLE revinfo (
    rev      INT    NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT NOT NULL,
    PRIMARY KEY (rev)
);

-- Audit table for users
CREATE TABLE users_aud (
    id                     BIGINT       NOT NULL,
    rev                    INT          NOT NULL,
    revtype                TINYINT,
    email                  VARCHAR(120),
    password_hash          VARCHAR(100),
    full_name              VARCHAR(120),
    enabled                BIT,
    failed_login_attempts  INT,
    locked_until           DATETIME(6),
    created_at             DATETIME(6),
    updated_at             DATETIME(6),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_users_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

-- Audit table for user_roles (element collection)
CREATE TABLE user_roles_aud (
    rev     INT         NOT NULL,
    revtype TINYINT,
    user_id BIGINT      NOT NULL,
    role    VARCHAR(32) NOT NULL,
    PRIMARY KEY (rev, user_id, role),
    CONSTRAINT fk_user_roles_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
