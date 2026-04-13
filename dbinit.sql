CREATE DATABASE login;
show databases;
GRANT ALL PRIVILEGES ON login.* TO 'wanted'@'%';

show grants for 'wanted'@'%';

--  root 계정에서 읽히기 