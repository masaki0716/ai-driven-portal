-- 招待コードの誤りとログインの失敗の記録（受け入れ条件 6）。接続元の IP アドレスごとに1件
create table login_attempt (
    ip            text        primary key,
    failure_count integer     not null,
    -- 5回目の失敗の時刻。ここから15分は受け付けない
    locked_at     timestamptz
);
