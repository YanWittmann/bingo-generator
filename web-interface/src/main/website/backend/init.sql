CREATE TABLE bingoboards
(
    id                    INT(11)      NOT NULL AUTO_INCREMENT,
    difficulty            DOUBLE       NOT NULL,
    game                  VARCHAR(50)  NOT NULL,
    description           VARCHAR(500) NOT NULL,
    title                 VARCHAR(50)  NOT NULL,
    version               VARCHAR(50)  NOT NULL,
    authors               VARCHAR(200) NOT NULL,
    allow_multiple_claims BOOLEAN      NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE bingotiles
(
    id       INT(11)      NOT NULL AUTO_INCREMENT,
    board_id INT(11)      NOT NULL,
    x        INT(11)      NOT NULL,
    y        INT(11)      NOT NULL,
    text     VARCHAR(200) NOT NULL,
    tooltip  VARCHAR(200) NOT NULL,
    claimed  VARCHAR(8)   NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (board_id) REFERENCES bingoboards (id)
)
