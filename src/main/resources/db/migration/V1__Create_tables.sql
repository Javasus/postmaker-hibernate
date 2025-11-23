CREATE SCHEMA IF NOT EXISTS postmaker;

CREATE TABLE postmaker.writers
(
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    status     VARCHAR(20) NOT NULL
);

CREATE TABLE postmaker.labels
(
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    status VARCHAR(20)  NOT NULL
);

CREATE TABLE postmaker.posts
(
    id        BIGSERIAL PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    content   TEXT,
    status    VARCHAR(20)  NOT NULL,
    writer_id BIGINT       NOT NULL,
    CONSTRAINT fk_post_writer FOREIGN KEY (writer_id) REFERENCES writers (id)
);

CREATE TABLE postmaker.post_labels
(
    post_id  BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, label_id),
    CONSTRAINT fk_post_labels_post FOREIGN KEY (post_id) REFERENCES postmaker.posts (id),
    CONSTRAINT fk_post_labels_label FOREIGN KEY (label_id) REFERENCES postmaker.labels (id)
);