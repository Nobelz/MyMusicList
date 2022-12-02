CREATE OR ALTER FUNCTION convert_seconds_to_string(
	@num_seconds int
) RETURNS varchar(8)
AS
BEGIN
	DECLARE @Result varchar(8)
	IF (@num_seconds IS NULL)
	BEGIN
		SELECT @Result = '0:00'
	END
	ELSE IF (@num_seconds < 600)
	BEGIN
		SELECT @Result = SUBSTRING(CONVERT(varchar(8), DATEADD(second, @num_seconds, 0), 108), 5, 8)
	END
	ELSE IF (@num_seconds < 3600)
	BEGIN
		SELECT @Result = SUBSTRING(CONVERT(varchar(8), DATEADD(second, @num_seconds, 0), 108), 4, 8)
	END
	ELSE IF (@num_seconds < 36000)
	BEGIN
		SELECT @Result = SUBSTRING(CONVERT(varchar(8), DATEADD(second, @num_seconds, 0), 108), 2, 8)
	END
	ELSE
	BEGIN
		SELECT @Result = CONVERT(varchar(8), DATEADD(second, @num_seconds, 0), 108)
	END

	RETURN @Result;
END;
GO

CREATE OR ALTER PROCEDURE view_playlists 
	@ID int,
	@restrict_public char(1) = 'n'
AS
BEGIN
	SELECT playlist.playlist_id, playlist.name, count(song.song_id) AS num_songs, dbo.convert_seconds_to_string(sum(song.duration)) AS total_listening_time
	FROM playlist 
		LEFT JOIN song_playlist ON song_playlist.playlist_id = playlist.playlist_id AND song_playlist.user_id = playlist.user_id
		LEFT JOIN song ON song.song_id = song_playlist.song_id
	WHERE playlist.user_id = @ID AND playlist.is_public IN (@restrict_public, 'y')
	GROUP BY playlist.playlist_id, playlist.name;
END;
GO

CREATE OR ALTER PROCEDURE view_songs
    @ID int
AS
BEGIN
    SELECT song_id
    FROM song_artist
    WHERE artist_id = @ID;
END;
GO

CREATE OR ALTER FUNCTION avg_rating_song(@ID int)
	RETURNS float
AS
BEGIN
	DECLARE @avg_rating float
	SELECT @avg_rating = avg(rating)
		FROM rating
		WHERE rating.song_id = @ID
	RETURN @avg_rating;
END
GO

CREATE OR ALTER FUNCTION avg_rating_album(@ID int)
	RETURNS float
AS
BEGIN
	DECLARE @avg_rating float
	SELECT @avg_rating = avg(dbo.avg_rating_song(song_id))
		FROM song_album
		WHERE album_id = @ID AND dbo.avg_rating_song(song_id) IS NOT NULL
	RETURN @avg_rating;
END
GO

CREATE OR ALTER FUNCTION avg_rating_artist(@ID int)
	RETURNS float
AS
BEGIN
	DECLARE @avg_rating float
	SELECT @avg_rating = avg(dbo.avg_rating_song(song_id))
		FROM song_artist
		WHERE artist_id = @ID AND dbo.avg_rating_song(song_id) IS NOT NULL
	RETURN @avg_rating;
END
GO

CREATE OR ALTER FUNCTION num_plays_song(@ID int)
	RETURNS int
AS
BEGIN
	DECLARE @total_plays int
	SELECT @total_plays = sum(num_listens)
		FROM listens
		WHERE song_id = @ID
	SELECT @total_plays = 
		CASE
			WHEN @total_plays IS NULL THEN 0
			ELSE @total_plays
		END
	RETURN @total_plays;
END
GO

CREATE OR ALTER FUNCTION num_plays_playlist(
	@user_id int,
	@playlist_id int)
	RETURNS int
AS
BEGIN
	DECLARE @total_plays int
	SELECT @total_plays = sum(dbo.num_plays_song(song_id))
		FROM song_playlist
		WHERE playlist_id = @playlist_id AND user_id = @user_id
	SELECT @total_plays = 
		CASE
			WHEN @total_plays IS NULL THEN 0
			ELSE @total_plays
		END
	RETURN @total_plays;
END
GO

CREATE OR ALTER FUNCTION num_plays_album(@ID int)
	RETURNS int
AS
BEGIN
	DECLARE @total_plays int
	SELECT @total_plays = sum(dbo.num_plays_song(song_id))
		FROM song_album
		WHERE album_id = @ID
	SELECT @total_plays = 
		CASE
			WHEN @total_plays IS NULL THEN 0
			ELSE @total_plays
		END
	RETURN @total_plays;
END
GO

CREATE OR ALTER FUNCTION num_plays_artist(@ID int)
	RETURNS int
AS
BEGIN
	DECLARE @total_plays int
	SELECT @total_plays = sum(dbo.num_plays_song(song_id))
		FROM song_artist
		WHERE artist_id = @ID
	SELECT @total_plays = 
		CASE
			WHEN @total_plays IS NULL THEN 0
			ELSE @total_plays
		END
	RETURN @total_plays;
END
GO

CREATE OR ALTER PROCEDURE search
	@ID int,
	@query varchar(1000),
	@num int,
	@order_by int = 1,
	@show_songs char(1) = 'y',
	@show_playlists char(1) = 'y',
	@show_users char(1) = 'y',
	@show_artists char(1) = 'y',
	@show_albums char(1) = 'y'
AS
BEGIN
	DECLARE @Result TABLE
	(
		type varchar(10),
		id int,
		sec_id int,
		name varchar(100)
	);
	
	IF (@show_songs = 'y')
	BEGIN
		INSERT INTO @Result
		SELECT TOP(@num) 'song' AS type, song_id AS id, 0 AS sec_id, name
		FROM song
		WHERE LOWER(name) LIKE LOWER('%' + @query + '%')
		ORDER BY
			CASE
				WHEN @order_by = 2 THEN 
					CASE
						WHEN dbo.avg_rating_song(song_id) IS NULL THEN 10
						ELSE 10 - dbo.avg_rating_song(song_id)
					END
				WHEN @order_by = 3 THEN -dbo.num_plays_song(song_id)
				ELSE
					CASE
						WHEN LOWER(name) LIKE LOWER(@query) THEN 1
    					WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 2
						WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 3
						ELSE 4
					END
			END;
	END
	
	IF (@show_playlists = 'y')
	BEGIN
		WITH playlists AS
			(SELECT user_id AS id, playlist_id AS sec_id, name 
			FROM playlist
			WHERE (user_id = @ID OR is_public = 'y'))
		INSERT INTO @Result
		SELECT TOP(@num) 'playlist' AS type, id, sec_id, name
		FROM playlists
		WHERE LOWER(name) LIKE LOWER('%' + @query + '%')
		ORDER BY
			CASE
				WHEN id = @ID THEN
					CASE
						WHEN LOWER(name) LIKE LOWER(@query) THEN 1.1
    					WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 1.2
						WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 1.3
						ELSE 1.4
					END
				ELSE
					CASE
						WHEN LOWER(name) LIKE LOWER(@query) THEN 2.1
    					WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 2.2
						WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 2.3
						ELSE 2.4
					END
			END;
	END

	IF (@show_users = 'y')
	BEGIN
		INSERT INTO @Result
		SELECT TOP(@num) 'user' AS type, user_id AS id, 0 AS sec_id, name
		FROM music_user
		WHERE (LOWER(name) LIKE LOWER('%' + @query + '%') OR LOWER(username) LIKE LOWER('%' + @query + '%'))
		ORDER BY
			CASE
				WHEN LOWER(username) LIKE LOWER(@query) THEN 1
				WHEN LOWER(name) LIKE LOWER(@query) THEN 2
    			WHEN LOWER(username) LIKE LOWER(@query + '%') THEN 3
				WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 4
				WHEN LOWER(username) LIKE LOWER('%' + @query) THEN 5
				WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 6
				WHEN LOWER(username) LIKE LOWER('%' + @query + '%') THEN 7
				ELSE 8
			END;
	END

	IF (@show_artists = 'y')
	BEGIN
		INSERT INTO @Result
		SELECT TOP(@num) 'artist' AS type, artist.artist_id AS id, 0 AS sec_id, music_user.name AS name
		FROM music_user
			JOIN artist ON music_user.user_id = artist.artist_id
		WHERE (LOWER(music_user.name) LIKE LOWER('%' + @query + '%') OR LOWER(music_user.username) LIKE LOWER('%' + @query + '%'))
		ORDER BY
			CASE
				WHEN @order_by = 2 THEN 
					CASE
						WHEN dbo.avg_rating_artist(artist_id) IS NULL THEN 10
						ELSE 10 - dbo.avg_rating_artist(artist_id)
					END
				WHEN @order_by = 3 THEN -dbo.num_plays_artist(artist_id)
				ELSE
					CASE
						WHEN @ID = artist_id THEN 0.5
						WHEN LOWER(username) LIKE LOWER(@query) THEN 1
						WHEN LOWER(name) LIKE LOWER(@query) THEN 2
    					WHEN LOWER(username) LIKE LOWER(@query + '%') THEN 3
						WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 4
						WHEN LOWER(username) LIKE LOWER('%' + @query) THEN 5
						WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 6
						WHEN LOWER(username) LIKE LOWER('%' + @query + '%') THEN 7
						ELSE 8
					END
			END;
	END

	IF (@show_albums = 'y')
	BEGIN
		INSERT INTO @Result
		SELECT TOP(@num) 'album' AS type, album_id AS id, 0 AS sec_id, name
		FROM album
		WHERE LOWER(name) LIKE LOWER('%' + @query + '%')
		ORDER BY
			CASE
				WHEN @order_by = 2 THEN 
					CASE
						WHEN dbo.avg_rating_album(album_id) IS NULL THEN 10
						ELSE 10 - dbo.avg_rating_album(album_id)
					END
				WHEN @order_by = 3 THEN -dbo.num_plays_album(album_id)
				ELSE
					CASE
						WHEN LOWER(name) LIKE LOWER(@query) THEN 1
    					WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 2
						WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 3
						ELSE 4
					END
			END;
	END

	SELECT * FROM @Result;
END;
GO

CREATE OR ALTER PROCEDURE add_to_playlist
	@song_id int,
	@user_id int,
	@playlist_id int
AS
BEGIN
	INSERT INTO song_playlist(song_id, user_id, playlist_id)
	VALUES (@song_id, @user_id, @playlist_id);
END;
GO

CREATE OR ALTER PROCEDURE add_to_album
    @song_id int,
    @album_id int
AS
BEGIN
    INSERT INTO song_album(song_id, album_id)
    VALUES (@song_id, @album_id);
END;
GO


CREATE OR ALTER PROCEDURE remove_from_playlist
	@song_id int,
	@user_id int,
	@playlist_id int
AS
BEGIN
	DELETE FROM song_playlist
		WHERE song_id = @song_id AND user_id = @user_id AND playlist_id = @playlist_id;
END;
GO

CREATE OR ALTER PROCEDURE remove_from_album
    @song_id int,
    @album_id int
AS
BEGIN
    DELETE FROM song_album
    WHERE song_id = @song_id AND album_id = @album_id;
END;
GO

CREATE OR ALTER PROCEDURE remove_song
    @song_id int
AS
BEGIN
    DELETE FROM song
    WHERE song_id = @song_id;
END;
GO

CREATE OR ALTER FUNCTION fav_genres
	(@ID int,
	@num_genres int)
RETURNS TABLE
AS
RETURN
(
	SELECT TOP (@num_genres) song_genre.genre_name, sum(listens.num_listens) AS total_listens
	FROM listens
		JOIN song_genre ON listens.song_id = song_genre.song_id
	WHERE listens.user_id = @ID
	GROUP BY song_genre.genre_name
	ORDER BY total_listens DESC
);
GO

CREATE OR ALTER FUNCTION fav_artists
	(@ID int,
	@num_artists int)
RETURNS TABLE
AS
RETURN
(
	SELECT TOP (@num_artists) song_artist.artist_id, music_user.name, sum(listens.num_listens) AS total_listens
	FROM listens
		JOIN song_artist ON listens.song_id = song_artist.song_id
		JOIN music_user ON music_user.user_id = song_artist.artist_id
	WHERE listens.user_id = @ID
	GROUP BY song_artist.artist_id, music_user.name
	ORDER BY total_listens DESC
);
GO

CREATE OR ALTER PROCEDURE find_rated_songs
	@ID int
AS
BEGIN
	SELECT song.song_id, song.name, rating.rating, rating.review, rating.timestamp
	FROM rating
		JOIN song ON song.song_id = rating.song_id
	WHERE rating.user_id = @ID;
END;
GO

CREATE OR ALTER PROCEDURE highest_rated_songs
	@num_songs int = 10
AS
BEGIN
	SELECT TOP(@num_songs) song_id, name, dbo.avg_rating_song(song_id) AS avg_rating
	FROM song
	ORDER BY dbo.avg_rating_song(song_id) DESC;
END;
GO

CREATE OR ALTER PROCEDURE most_popular_songs
	@num_songs int = 10
AS
BEGIN
	SELECT TOP(@num_songs) song_id, name, dbo.num_plays_song(song_id) AS total_listens
	FROM song
	ORDER BY dbo.num_plays_song(song_id) DESC;
END;
GO

CREATE OR ALTER PROCEDURE find_artists_by_genre
	@genre_name varchar(25)
AS
BEGIN
	SELECT song.song_id, name
	FROM song
		JOIN song_genre ON song_genre.song_id = song.song_id
	WHERE song_genre.genre_name = @genre_name;
END;
GO

CREATE OR ALTER PROCEDURE most_played_songs_by_user
	@ID int,
	@num_songs int
AS
BEGIN
	SELECT TOP(@num_songs) song.song_id, name, listens.num_listens
	FROM song
		JOIN listens ON song.song_id = listens.song_id
	WHERE listens.user_id = @ID
	ORDER BY listens.num_listens DESC;
END;
GO

CREATE OR ALTER FUNCTION login_with_username(@username varchar(200))
RETURNS int
AS
BEGIN
	DECLARE @user_id int
	SELECT @user_id = user_id
	FROM music_user
	WHERE music_user.username = @username;
	RETURN @user_id;
END;
GO

CREATE OR ALTER PROCEDURE get_songs_by_playlist
	@user_id int,
	@playlist_id int
AS
BEGIN
	SELECT song_id
	FROM song_playlist
	WHERE @user_id = user_id AND @playlist_id = playlist_id;
END;
GO

CREATE OR ALTER PROCEDURE get_user_by_id
	@user_id int
AS
BEGIN
	SELECT *
	FROM music_user
	WHERE @user_id = music_user.user_id;
END;
GO

CREATE OR ALTER PROCEDURE get_artist_by_id
    @artist_id int
AS
BEGIN
    SELECT artist.artist_id, music_user.name, music_user.username, music_user.join_date
    FROM artist
        JOIN music_user ON artist.artist_id = music_user.user_id
    WHERE @artist_id = artist.artist_id;
END;
GO

CREATE OR ALTER PROCEDURE get_playlist_by_id
	@user_id int,
	@playlist_id int
AS
BEGIN
	SELECT playlist.*, count(song.song_id) AS num_songs, sum(song.duration) AS duration_value, dbo.convert_seconds_to_string(sum(song.duration)) AS duration
	FROM playlist
		LEFT JOIN song_playlist ON playlist.playlist_id = song_playlist.playlist_id AND song_playlist.user_id = playlist.user_id
		LEFT JOIN song ON song_playlist.song_id = song.song_id
	WHERE playlist.playlist_id = @playlist_id AND playlist.user_id = @user_id
	GROUP BY playlist.playlist_id, playlist.user_id, playlist.is_public, playlist.name;
END;
GO


CREATE OR ALTER PROCEDURE get_song_by_id
	@ID int
AS
BEGIN
	SELECT *, dbo.convert_seconds_to_string(duration) AS duration_string
	FROM song
	WHERE song.song_id = @ID;
END;
GO

CREATE OR ALTER PROCEDURE get_artists_by_song
	@ID int
AS
BEGIN
	SELECT artist_id
	FROM song_artist
	WHERE song_id = @ID;
END;
GO

CREATE OR ALTER PROCEDURE get_artists_by_album
	@ID int
AS
BEGIN
	SELECT artist_id
	FROM album_artist
	WHERE album_id = @ID;
END;
GO

CREATE OR ALTER PROCEDURE delete_playlist
    @user_id int,
    @playlist_id int
AS
BEGIN
    DELETE FROM playlist
    WHERE user_id = @user_id AND playlist_id = @playlist_id;
END;
GO

CREATE OR ALTER PROCEDURE delete_album
    @album_id int
AS
BEGIN
    DELETE FROM album
    WHERE album_id = @album_id;
END;
GO

CREATE OR ALTER PROCEDURE toggle_playlist_privacy
    @user_id int,
    @playlist_id int
AS
BEGIN
    UPDATE playlist
    SET is_public =
        CASE
            WHEN is_public = 'y' THEN 'n'
            ELSE 'y'
        END
    WHERE @playlist_id = playlist_id AND @user_id = user_id;
END;
GO

CREATE OR ALTER FUNCTION num_playlists_song(@ID int)
RETURNS int
AS
BEGIN
    DECLARE @num_playlists int

    SELECT @num_playlists = count(*)
    FROM song_playlist
    WHERE @ID = song_id

    SELECT @num_playlists =
        CASE
            WHEN @num_playlists IS NULL THEN 0
            ELSE @num_playlists
        END

    RETURN @num_playlists;
END;
GO

CREATE OR ALTER FUNCTION num_albums_song(@ID int)
    RETURNS int
AS
BEGIN
    DECLARE @num_albums int

    SELECT @num_albums = count(*)
    FROM song_album
    WHERE @ID = song_id

    SELECT @num_albums =
           CASE
               WHEN @num_albums IS NULL THEN 0
               ELSE @num_albums
               END

    RETURN @num_albums;
END;
GO

CREATE OR ALTER PROCEDURE view_user_recommendations
    @ID int
AS
BEGIN
    SELECT song.song_id, song.name, recommendation.from_id
    FROM recommendation
             JOIN song ON song.song_id = recommendation.song_id
    WHERE to_id = @ID;
END;
GO

CREATE OR ALTER PROCEDURE view_auto_recommendations
    @ID int,
    @num_recommendations int
AS
BEGIN
    DECLARE @Result TABLE
    (
        type varchar(10),
        song_id int,
        name varchar(100)
    );

    CREATE TABLE #temp_artist
    (
        artist_id int,
        name varchar(50),
        total_listens int
    );

    WITH temp_artist AS
        (
            SELECT DISTINCT 'artist' AS type, song.song_id, song.name
            FROM dbo.fav_artists(@ID, 10) AS temp
                JOIN song_artist ON song_artist.artist_id = temp.artist_id
                JOIN song ON song_artist.song_id = song.song_id
                LEFT JOIN listens ON listens.song_id = song.song_id
            WHERE listens.user_id IS NULL AND song.song_id NOT IN
                (SELECT song_id FROM @Result)
        )
    INSERT INTO @Result
    SELECT TOP(@num_recommendations) * FROM temp_artist
    ORDER BY NEWID();

    WITH temp_genre AS
        (
            SELECT DISTINCT 'genre' AS type, song.song_id, song.name
            FROM dbo.fav_genres(@ID, 3) AS temp
                JOIN song_genre ON song_genre.genre_name = temp.genre_name
                JOIN song ON song_genre.song_id = song.song_id
                LEFT JOIN listens ON listens.song_id = song.song_id
            WHERE listens.user_id IS NULL AND song.song_id NOT IN
                (SELECT song_id FROM @Result)
        )
    INSERT INTO @Result
    SELECT TOP(@num_recommendations) * FROM temp_genre
    ORDER BY NEWID();

    SELECT * FROM @Result;
END;
GO

CREATE OR ALTER PROCEDURE get_user_by_username
    @username varchar(30)
AS
BEGIN
    SELECT *
    FROM music_user
    WHERE @username = username;
END;
GO

CREATE OR ALTER PROCEDURE make_recommendation
    @from_id int,
    @to_id int,
    @song_id int
AS
BEGIN
    INSERT INTO recommendation(from_id, to_id, song_id)
    VALUES (@from_id, @to_id, @song_id);
END;
GO

CREATE OR ALTER PROCEDURE add_listens
    @user_id int,
    @song_id int,
    @num_listens int
AS
BEGIN
    IF dbo.get_num_listens(@user_id, @song_id) = 0
    BEGIN
        INSERT INTO listens(user_id, song_id, num_listens)
        VALUES (@user_id, @song_id, @num_listens);
    END
    ELSE
    BEGIN
        UPDATE listens
        SET num_listens = num_listens + @num_listens
        WHERE user_id = @user_id AND song_id = @song_id;
    END
END;
GO

CREATE OR ALTER FUNCTION get_num_listens(
    @user_id int,
    @song_id int)
RETURNS int
AS
BEGIN
    DECLARE @num_listens int
    SELECT @num_listens = num_listens
    FROM listens
    WHERE user_id = @user_id AND song_id = @song_id
    SELECT @num_listens =
        CASE
            WHEN @num_listens IS NULL THEN 0
            ELSE @num_listens
        END
    RETURN @num_listens;
END;
GO

CREATE OR ALTER PROCEDURE make_review
    @user_id int,
    @song_id int,
    @rating int,
    @review varchar(250)
AS
BEGIN
    IF @review = ''
    BEGIN
        INSERT INTO rating(user_id, song_id, rating)
        VALUES (@user_id, @song_id, @rating);
    END
    ELSE
    BEGIN
        INSERT INTO rating(user_id, song_id, rating, review)
        VALUES (@user_id, @song_id, @rating, @review);
    END
END;
GO

CREATE OR ALTER PROCEDURE update_review
    @user_id int,
    @song_id int,
    @rating int,
    @review varchar(250)
AS
BEGIN
    IF @review = ''
    BEGIN
        UPDATE rating
        SET rating = @rating
        WHERE user_id = @user_id AND song_id = @song_id;
    END
    ELSE
    BEGIN
        UPDATE rating
        SET rating = @rating, review = @review
        WHERE user_id = @user_id AND song_id = @song_id;
    END
END;
GO

CREATE OR ALTER PROCEDURE view_albums
    @ID int
AS
BEGIN
    SELECT album.album_id, album.name, count(song.song_id) AS num_songs, dbo.convert_seconds_to_string(sum(song.duration)) AS total_listening_time
    FROM album
             LEFT JOIN song_album ON song_album.album_id = album.album_id
             LEFT JOIN song ON song.song_id = song_album.song_id
			 LEFT JOIN album_artist ON album.album_id = album_artist.album_id
    WHERE album_artist.artist_id = @ID
    GROUP BY album.album_id, album.name;
END;
GO

CREATE OR ALTER PROCEDURE get_album_by_id
    @album_id int
AS
BEGIN
    SELECT album.*, count(song.song_id) AS num_songs, dbo.convert_seconds_to_string(sum(song.duration)) AS duration
    FROM album
             LEFT JOIN song_album ON album.album_id = song_album.album_id
             LEFT JOIN song ON song_album.song_id = song.song_id
    WHERE album.album_id = @album_id
    GROUP BY album.album_id, album.name, album.release_date;
END;
GO

CREATE OR ALTER PROCEDURE get_songs_by_album
    @album_id int
AS
BEGIN
    SELECT song_id
    FROM song_album
    WHERE album_id = @album_id;
END;
GO

CREATE OR ALTER PROCEDURE get_genres_by_album
    @album_id int
AS
BEGIN
    SELECT genre_name
    FROM album_genre
    WHERE album_id = @album_id;
END;
GO

CREATE OR ALTER PROCEDURE get_genres_by_song
    @song_id int
AS
BEGIN
    SELECT genre_name
    FROM song_genre
    WHERE song_id = @song_id;
END;
GO

CREATE OR ALTER PROCEDURE get_genre_by_name
    @genre_name varchar(25)
AS
BEGIN
    SELECT name, description
    FROM genre
    WHERE name = @genre_name;
END;
GO

CREATE OR ALTER PROCEDURE make_playlist
    @user_id int,
    @playlist_id int,
    @name varchar(50),
    @is_public char(1)
AS
BEGIN
    INSERT INTO playlist(user_id, playlist_id, name, is_public)
    VALUES (@user_id, @playlist_id, @name, @is_public);
END;
GO

CREATE OR ALTER PROCEDURE make_album
    @name varchar(50)
AS
BEGIN
    INSERT INTO album(name)
    VALUES (@name);
END;
GO

CREATE OR ALTER PROCEDURE make_song
    @name varchar(50),
    @duration int
AS
BEGIN
    INSERT INTO song(name, duration)
    VALUES (@name, @duration);
END;
GO

CREATE OR ALTER FUNCTION generate_playlist_id(@user_id int)
RETURNS int
AS
BEGIN
    DECLARE @Result int
    SELECT @Result = max(playlist_id) + 1
    FROM playlist
    WHERE user_id = @user_id
    SELECT @Result =
        CASE
            WHEN @Result IS NULL THEN 1
            ELSE @Result
        END
    RETURN @Result;
END;
GO

CREATE OR ALTER PROCEDURE add_album_artist
    @album_id int,
    @artist_id int
AS
BEGIN
    INSERT INTO album_artist(album_id, artist_id)
    VALUES (@album_id, @artist_id);
END;
GO

CREATE OR ALTER PROCEDURE add_album_genre
    @album_id int,
    @genre_name varchar(25)
AS
BEGIN
    INSERT INTO album_genre(album_id, genre_name)
    VALUES (@album_id, @genre_name);
END;
GO

CREATE OR ALTER PROCEDURE add_song_artist
    @song_id int,
    @artist_id int
AS
BEGIN
    INSERT INTO song_artist(song_id, artist_id)
    VALUES (@song_id, @artist_id);
END;
GO

CREATE OR ALTER PROCEDURE add_song_genre
    @song_id int,
    @genre_name varchar(25)
AS
BEGIN
    INSERT INTO song_genre(song_id, genre_name)
    VALUES (@song_id, @genre_name);
END;
GO

CREATE OR ALTER PROCEDURE delete_artist
    @artist_id int
AS
BEGIN
    DELETE FROM song
    WHERE song_id IN (SELECT song_id FROM song_artist WHERE song_artist.artist_id = @artist_id);

    DELETE FROM album
    WHERE album_id IN (SELECT album_id FROM album_artist WHERE album_artist.artist_id = @artist_id);

    DELETE FROM artist
    WHERE artist_id = @artist_id;
END;
GO

CREATE OR ALTER PROCEDURE delete_user
    @user_id int
AS
BEGIN
    EXEC delete_artist @artist_id = @user_id;

    DELETE FROM music_user
    WHERE user_id = @user_id;
END;
GO

CREATE OR ALTER PROCEDURE create_artist
    @user_id int
AS
BEGIN
    INSERT INTO artist(artist_id)
    VALUES (@user_id);
END;
GO
