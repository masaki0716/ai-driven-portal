-- 取得した予報（受け入れ条件 4・5）。地点ごとに最新の1件
create table weather_forecast (
    latitude   double precision not null,
    longitude  double precision not null,
    fetched_at timestamptz      not null,
    body       text             not null,
    primary key (latitude, longitude)
);

-- 天気 API の失敗の記録（受け入れ条件 6）。地点ごとに最新の1件
create table weather_api_failure (
    latitude  double precision not null,
    longitude double precision not null,
    failed_at timestamptz      not null,
    primary key (latitude, longitude)
);
