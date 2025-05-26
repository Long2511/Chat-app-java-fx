# Chat-app-java-fx

## Database Schema
### 1. users
   Stores each user’s profile and login info.

| Column   | Type              | Notes                                                  |
|----------|-------------------|--------------------------------------------------------|
| id       | bigint            | Primary key                                            |
| avatar   | character varying | URL (or path) to user’s avatar image                   |
| email    | character varying | User’s email address (unique-constrained at app-level) |
| password | character varying | Hashed password                                        |
| status   | character varying | e.g. “online”, “offline”, “away”                       |
| username | character varying | Display name / login handle                            |


### 2. chats
   Holds metadata for each chat 

| Column      | Type                        | Notes                                           |
| ----------- | --------------------------- | ----------------------------------------------- |
| id          | bigint                      | Primary key                                     |
| name        | character varying           | Chat name (used for group chats)                |
| type        | character varying           | e.g. “direct”, “group”                          |
| created\_at | timestamp without time zone | When the chat was first created                 |
| updated\_at | timestamp without time zone | Last time chat metadata (e.g. name) was changed |


### 3. chat_participants
   Join table linking users ↔ chats.

| Column   | Type   | Notes                                                     |
| -------- | ------ | --------------------------------------------------------- |
| chat\_id | bigint | FK → chats.id                                             |
| user\_id | bigint | FK → users.id; each row means “this user is in this chat” |

Together (chat_id, user_id) form the composite key.

### 4. messages
   All individual messages sent within chats.

| Column        | Type                        | Notes                                                         |
| ------------- | --------------------------- | ------------------------------------------------------------- |
| id            | bigint                      | Primary key                                                   |
| chat\_id      | bigint                      | FK → chats.id                                                 |
| sender\_id    | bigint                      | FK → users.id (who sent it)                                   |
| content       | text                        | Message text                                                  |
| message\_type | character varying           | e.g. “text”, “image”, “file”                                  |
| media\_url    | character varying           | URL for image/video, if applicable                            |
| file\_url     | character varying           | URL for file download, if applicable                          |
| is\_read      | boolean                     | Whether recipient(s) have read the message                    |
| created\_at   | timestamp without time zone | When the message was originally sent                          |
| updated\_at   | timestamp without time zone | When the message was last edited (if your app allows editing) |
