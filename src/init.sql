create table public.chat_participants (
  chat_id bigint not null,
  user_id bigint not null,
  constraint chat_participants_pkey primary key (chat_id, user_id),
  constraint chat_participants_user_id_fkey foreign KEY (user_id) references users (id) on delete CASCADE,
  constraint fkbhdyxo0ndtbs1t49l28y21rkw foreign KEY (user_id) references users (id),
  constraint fkn4feij8janlba38q59kl2ebgg foreign KEY (chat_id) references chats (id)
) TABLESPACE pg_default;



create table public.chats (
  id bigserial not null,
  created_at timestamp without time zone null,
  name character varying(255) null,
  type character varying(255) null,
  updated_at timestamp without time zone null,
  constraint chats_pkey primary key (id),
  constraint chats_type_check check (
    (
      (type)::text = any (
        array[
          ('PRIVATE'::character varying)::text,
          ('GROUP'::character varying)::text
        ]
      )
    )
  )
) TABLESPACE pg_default;


create table public.messages (
  id bigserial not null,
  content text not null,
  created_at timestamp without time zone not null,
  file_url character varying(255) null,
  is_read boolean null,
  media_url character varying(255) null,
  message_type character varying(255) null,
  updated_at timestamp without time zone null,
  chat_id bigint not null,
  sender_id bigint not null,
  constraint messages_pkey primary key (id),
  constraint fk4ui4nnwntodh6wjvck53dbk9m foreign KEY (sender_id) references users (id),
  constraint fk64w44ngcpqp99ptcb9werdfmb foreign KEY (chat_id) references chats (id),
  constraint messages_sender_id_fkey foreign KEY (sender_id) references users (id) on delete CASCADE,
  constraint messages_message_type_check check (
    (
      (message_type)::text = any (
        array[
          ('TEXT'::character varying)::text,
          ('IMAGE'::character varying)::text,
          ('VIDEO'::character varying)::text,
          ('AUDIO'::character varying)::text,
          ('FILE'::character varying)::text
        ]
      )
    )
  )
) TABLESPACE pg_default;


create table public.users (
  id bigserial not null,
  avatar character varying(255) null,
  email character varying(255) not null,
  password character varying(255) not null,
  status character varying(255) null,
  username character varying(255) not null,
  constraint users_pkey primary key (id),
  constraint uk_6dotkott2kjsp8vw4d0m25fb7 unique (email),
  constraint users_status_check check (
    (
      (status)::text = any (
        array[
          ('ONLINE'::character varying)::text,
          ('OFFLINE'::character varying)::text,
          ('AWAY'::character varying)::text,
          ('BUSY'::character varying)::text
        ]
      )
    )
  )
) TABLESPACE pg_default;