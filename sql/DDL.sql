USE MyMusicList;

IF OBJECT_ID(N'recommendation', N'U') IS NOT NULL
	DROP TABLE recommendation;
IF OBJECT_ID(N'listens', N'U') IS NOT NULL
	DROP TABLE listens;
IF OBJECT_ID(N'rating', N'U') IS NOT NULL
	DROP TABLE rating;
IF OBJECT_ID(N'song_playlist', N'U') IS NOT NULL
	DROP TABLE song_playlist;
IF OBJECT_ID(N'album_genre', N'U') IS NOT NULL
	DROP TABLE album_genre;
IF OBJECT_ID(N'album_artist', N'U') IS NOT NULL
	DROP TABLE album_artist;
IF OBJECT_ID(N'song_genre', N'U') IS NOT NULL
	DROP TABLE song_genre;
IF OBJECT_ID(N'song_album', N'U') IS NOT NULL
	DROP TABLE song_album;
IF OBJECT_ID(N'song_artist', N'U') IS NOT NULL
	DROP TABLE song_artist;
IF OBJECT_ID(N'playlist', N'U') IS NOT NULL
	DROP TABLE playlist;
IF OBJECT_ID(N'album', N'U') IS NOT NULL
	DROP TABLE album;
IF OBJECT_ID(N'artist', N'U') IS NOT NULL
	DROP TABLE artist;
IF OBJECT_ID(N'music_user', N'U') IS NOT NULL
	DROP TABLE music_user;
IF OBJECT_ID(N'genre', N'U') IS NOT NULL
	DROP TABLE genre;
IF OBJECT_ID(N'song', N'U') IS NOT NULL
	DROP TABLE song;

CREATE TABLE song
(
	song_id int NOT NULL IDENTITY(1,1),
	name varchar(100)  NOT NULL,
	duration int NOT NULL DEFAULT 0 CHECK (duration >= 0 AND duration < 360000),
	release_date DATE NOT NULL DEFAULT GETDATE() CHECK (release_date <= GETDATE()), 

	CONSTRAINT song_PK PRIMARY KEY (song_id)
);

CREATE TABLE genre
(
	name varchar(25) NOT NULL,
	description varchar(200),

	CONSTRAINT genre_PK PRIMARY KEY (name)
);

CREATE TABLE music_user
(
	user_id int NOT NULL IDENTITY(1,1),
	username varchar(30) NOT NULL UNIQUE,
	name varchar(50) NOT NULL,
	join_date DATE NOT NULL DEFAULT GETDATE() CHECK (join_date <= GETDATE()),

	CONSTRAINT user_PK PRIMARY KEY (user_id)
);

CREATE TABLE artist
(
	artist_id int NOT NULL,

	CONSTRAINT artist_id_PK PRIMARY KEY (artist_id),
	CONSTRAINT artist_music_user_FK FOREIGN KEY (artist_id)
		REFERENCES music_user(user_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE album
(
	album_id int NOT NULL IDENTITY(1,1),
	name varchar(50),
	release_date DATE NOT NULL DEFAULT GETDATE() CHECK (release_date <= GETDATE()), 

	CONSTRAINT album_PK PRIMARY KEY (album_id)
);

CREATE TABLE playlist
(
	playlist_id int NOT NULL,
	user_id int NOT NULL,
	name varchar(50),
	is_public char(1) NOT NULL DEFAULT 'n' CHECK (is_public in ('y', 'n')),

	CONSTRAINT playlist_PK PRIMARY KEY (user_id, playlist_id),
	CONSTRAINT playlist_user_FK FOREIGN KEY (user_id)
		REFERENCES music_user(user_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE song_artist
(
	song_id int NOT NULL,
	artist_id int NOT NULL,

	CONSTRAINT song_artist_PK PRIMARY KEY (song_id, artist_id),
	CONSTRAINT song_artist_song_FK FOREIGN KEY (song_id)
		REFERENCES song(song_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT song_artist_artist_FK FOREIGN KEY (artist_id)
		REFERENCES artist(artist_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE song_genre
(
	song_id int NOT NULL,
	genre_name varchar(25),

	CONSTRAINT song_genre_PK PRIMARY KEY (song_id, genre_name),
	CONSTRAINT song_genre_song_FK FOREIGN KEY (song_id)
		REFERENCES song(song_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT song_genre_genre_FK FOREIGN KEY (genre_name)
		REFERENCES genre(name)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE song_album
(
	song_id int NOT NULL,
	album_id int NOT NULL,

	CONSTRAINT song_album_PK PRIMARY KEY (song_id, album_id),
	CONSTRAINT song_album_song_FK FOREIGN KEY (song_id)
		REFERENCES song(song_id) 
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT song_album_album_FK FOREIGN KEY (album_id)
		REFERENCES album(album_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE album_artist
(
	album_id int NOT NULL,
	artist_id int NOT NULL,

	CONSTRAINT album_artist_PK PRIMARY KEY (album_id, artist_id),
	CONSTRAINT album_artist_album_FK FOREIGN KEY (album_id)
		REFERENCES album(album_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT album_artist_artist_FK FOREIGN KEY (artist_id)
		REFERENCES artist(artist_id) 
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE album_genre
(
	album_id int NOT NULL,
	genre_name varchar(25) NOT NULL,
	CONSTRAINT album_genre_PK PRIMARY KEY (album_id, genre_name),
	CONSTRAINT album_genre_album_FK FOREIGN KEY (album_id)
		REFERENCES album(album_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT album_genre_genre_FK FOREIGN KEY (genre_name)
	REFERENCES genre(name)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE song_playlist
(
	song_id int NOT NULL,
	user_id int NOT NULL, 
	playlist_id int NOT NULL,

	CONSTRAINT song_playlist_PK PRIMARY KEY (song_id, user_id, playlist_id),
	CONSTRAINT song_FK FOREIGN KEY (song_id)
		REFERENCES song(song_id) 
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT song_playlist_playlist FOREIGN KEY (user_id, playlist_id)
		REFERENCES playlist(user_id, playlist_id) 
		ON UPDATE CASCADE
		ON DELETE CASCADE,
);

CREATE TABLE rating
(
	user_id int NOT NULL,
	song_id int NOT NULL,
	rating int NOT NULL CHECK (rating >=1 AND rating <= 10),
	review varchar(250), 
	timestamp datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

	CONSTRAINT rating_PK PRIMARY KEY (user_id, song_id),
	CONSTRAINT rating_user_FK FOREIGN KEY (user_id)
		REFERENCES music_user(user_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT rating_song_FK FOREIGN KEY (song_id)
		REFERENCES song(song_id) 
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE listens
(
	user_id int NOT NULL,
	song_id int NOT NULL,
	num_listens int NOT NULL DEFAULT 1,

	CONSTRAINT listens_PK PRIMARY KEY (user_id, song_id),
	CONSTRAINT listens_user_FK FOREIGN KEY (user_id)
		REFERENCES music_user(user_id) 
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT listens_song_FK FOREIGN KEY (song_id)
		REFERENCES song(song_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE recommendation
(
	from_id int NOT NULL,
	to_id int NOT NULL,
	song_id int NOT NULL,

	CONSTRAINT recommendation_PK PRIMARY KEY (from_id, to_id, song_id),
	CONSTRAINT from_id_FK FOREIGN KEY (from_id)
		REFERENCES music_user(user_id)
		ON DELETE NO ACTION,
	CONSTRAINT to_id_FK FOREIGN KEY (to_id)
		REFERENCES music_user(user_id) 
		ON DELETE NO ACTION,
	CONSTRAINT recommendation_song_FK FOREIGN KEY (song_id)
		REFERENCES song(song_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);
GO

CREATE OR ALTER TRIGGER recommendation_delete 
	ON music_user
	INSTEAD OF DELETE
AS
BEGIN
	SET NOCOUNT ON;

	DELETE FROM recommendation
	WHERE from_id in (SELECT user_id FROM DELETED);

	DELETE FROM recommendation
	WHERE to_id in (SELECT user_id FROM DELETED);

	DELETE FROM music_user
	WHERE user_id in (SELECT user_id FROM DELETED);
END;
GO
