-- 会員（受け入れ条件 1〜5・10・11）。ID は URL に入れるので推測できない UUID にする
create table member (
    id               uuid        primary key,
    -- 前後の空白を除いた、入力どおりの値
    email            text        not null,
    -- email を小文字にした値。重複の判定とログインに使う（4）
    email_normalized text        not null unique,
    password_hash    text        not null,
    username         text        not null,
    created_at       timestamptz not null default now()
);
