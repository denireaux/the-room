CREATE TABLE IF NOT EXISTS chat_message (
  id           UUID PRIMARY KEY,
  username     VARCHAR(64)  NOT NULL,
  content      VARCHAR(2000) NOT NULL,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_message_created_at
  ON chat_message (created_at DESC);
