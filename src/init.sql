-- USERS
CREATE TABLE public.users (
                              id        BIGSERIAL PRIMARY KEY,
                              avatar    VARCHAR(255),
                              email     VARCHAR(255) NOT NULL UNIQUE,
                              password  VARCHAR(255) NOT NULL,
                              status    VARCHAR(255)
                                  CHECK(status IN ('ONLINE','OFFLINE','AWAY','BUSY')),
                              username  VARCHAR(255) NOT NULL
);


-- CHATS
CREATE TABLE public.chats (
                              id         BIGSERIAL PRIMARY KEY,
                              created_at TIMESTAMP WITHOUT TIME ZONE,
                              name       VARCHAR(255),
                              type       VARCHAR(255)
                                  CHECK(type IN ('PRIVATE','GROUP')),
                              updated_at TIMESTAMP WITHOUT TIME ZONE
);


-- CHAT_PARTICIPANTS: join users ↔ chats
CREATE TABLE public.chat_participants (
                                          chat_id BIGINT NOT NULL,
                                          user_id BIGINT NOT NULL,
                                          PRIMARY KEY (chat_id, user_id),

                                          FOREIGN KEY (chat_id)
                                              REFERENCES public.chats(id)
                                              ON DELETE CASCADE,

                                          FOREIGN KEY (user_id)
                                              REFERENCES public.users(id)
                                              ON DELETE CASCADE
);


-- MESSAGES
CREATE TABLE public.messages (
                                 id           BIGSERIAL PRIMARY KEY,
                                 content      TEXT NOT NULL,
                                 created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                 file_url     VARCHAR(255),
                                 is_read      BOOLEAN,
                                 media_url    VARCHAR(255),
                                 message_type VARCHAR(255)
                                     CHECK(message_type IN ('TEXT','IMAGE','VIDEO','AUDIO','FILE')),
                                 updated_at   TIMESTAMP WITHOUT TIME ZONE,
                                 chat_id      BIGINT NOT NULL,
                                 sender_id    BIGINT NOT NULL,

                                 FOREIGN KEY (chat_id)
                                     REFERENCES public.chats(id)
                                     ON DELETE CASCADE,

                                 FOREIGN KEY (sender_id)
                                     REFERENCES public.users(id)
                                     ON DELETE CASCADE
);
