-- BEGIN USE CASE 1
/*
 Searches entire database for a search keyword.

 Parameters:
    @ID: User ID of user performing the search
    @query: Search keyword
    @num: Maximum number of results per type
    @order_by: Ordering scheme
        1: Order by relevance (default)
        2: Order by rating, when applicable
        3: Order by popularity, when applicable
    @show_songs: 'y' or 'n', Whether to show songs in result, default is 'y'
    @show_playlists: 'y' or 'n', Whether to show playlists in result, default is 'y'
    @show_users: 'y' or 'n', Whether to show users in result, default is 'y'
    @show_artists: 'y' or 'n', Whether to show artists in result, default is 'y'
    @show_albums: 'y' or 'n', Whether to show albums in result, default is 'y'
 Returns: Table consisting of types, ID's, and names of the entries.
 Uses:
    Function avg_rating_song
    Function avg_rating_artist
    Function avg_rating_album
    Function num_plays_song
    Function num_plays_artist
    Function num_plays_album
 */
CREATE OR ALTER PROCEDURE search
    @ID int, -- User ID of user performing the search
    @query varchar(1000), -- Search keyword
    @num int, -- Maximum number of results (per type)
    @order_by int = 1, -- Ordering scheme (1: Relevance, 2: Rating (when applicable), 3: Popularity (when applicable); Default is Relevance)
    @show_songs char(1) = 'y', -- Whether to show songs in search result (Default is to show songs)
    @show_playlists char(1) = 'y', -- Whether to show playlists in search result (Default is to show playlists)
    @show_users char(1) = 'y', -- Whether to show users in search result (Default is to show users)
    @show_artists char(1) = 'y', -- Whether to show artists in search result (Default is to show artists)
    @show_albums char(1) = 'y' -- Whether to show albums in search result (Default is to show artists)
AS
BEGIN
    /*
     Return table.

     Types:
        type: Type of search result (e.g. 'song', 'playlist', etc.)
        id: ID of search result (for playlists, this would be the userID)
        sec_id: Secondary ID of search result, if applicable (for playlists, this would be the playlistID)
        name: Name of search result
     */
    DECLARE @Result TABLE -- Table that is returned showing search result
                    (
                        type varchar(10),
                        id int,
                        sec_id int,
                        name varchar(100)
                    );

    -- Check to see if songs should be returned in result
    IF (@show_songs = 'y')
        BEGIN
            -- Gets the top n songs based on the order scheme that fit the keyword and inserts into the result table
            INSERT INTO @Result
            SELECT TOP(@num) 'song' AS type, song_id AS id, 0 AS sec_id, name
            FROM song
            WHERE LOWER(name) LIKE LOWER('%' + @query + '%') -- Check if keyword is in the song name (with case-insensitive too)
            ORDER BY
                -- Check which order schema to follow
                CASE
                    WHEN @order_by = 2 THEN -- Order by rating (highest average rating at the top)
                        CASE
                            WHEN dbo.avg_rating_song(song_id) IS NULL THEN 10
                            ELSE 10 - dbo.avg_rating_song(song_id)
                            END
                    WHEN @order_by = 3 THEN -dbo.num_plays_song(song_id) -- Order by popularity (most num listens at the top)
                    ELSE -- Order by relevance
                        CASE
                            WHEN LOWER(name) LIKE LOWER(@query) THEN 1 -- Highest priority: keyword is exactly the name
                            WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 2 -- 2nd priority: keyword occurs at very beginning of name
                            WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 3 -- 3rd priority: keyword occurs at very end of name
                            ELSE 4 -- 4th priority: keyword occurs in the middle of the name
                            END
                    END;
        END

    -- Check to see if playlists should be returned in result
    IF (@show_playlists = 'y')
        BEGIN
            -- Gets the top n entries of public playlists and the (public and private) playlists of the user and inserts them into result table
            WITH playlists AS
                     (SELECT user_id AS id, playlist_id AS sec_id, name
                      FROM playlist
                      WHERE (user_id = @ID OR is_public = 'y'))
            INSERT INTO @Result
            SELECT TOP(@num) 'playlist' AS type, id, sec_id, name
            FROM playlists
            WHERE LOWER(name) LIKE LOWER('%' + @query + '%') -- Check if keyword is in the playlist name
            ORDER BY
                CASE -- Order by relevance only
                    WHEN id = @ID THEN -- User's own playlists are prioritized over other users' playlists
                        CASE
                            WHEN LOWER(name) LIKE LOWER(@query) THEN 1.1 -- Highest priority: keyword is exactly the name
                            WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 1.2 -- 2nd priority: keyword occurs at very beginning of name
                            WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 1.3 -- 3rd priority: keyword occurs at very end of name
                            ELSE 1.4 -- 4th priority: keyword occurs in the middle of the name
                            END
                    ELSE -- Other user's playlists have lower priority
                        CASE
                            WHEN LOWER(name) LIKE LOWER(@query) THEN 2.1
                            WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 2.2
                            WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 2.3
                            ELSE 2.4
                            END
                    END;
        END

    -- Check to see if users should be returned in result
    IF (@show_users = 'y')
        BEGIN
            -- Gets the top n entries of the users whose name and/or username contain the keyword
            INSERT INTO @Result
            SELECT TOP(@num) 'user' AS type, user_id AS id, 0 AS sec_id, name
            FROM music_user
            WHERE (LOWER(name) LIKE LOWER('%' + @query + '%') OR LOWER(username) LIKE LOWER('%' + @query + '%')) -- Check if keyword is in username or name of user
            ORDER BY
                CASE -- Order by relevance only, username is prioritized over name
                    WHEN @ID = user_id THEN 0.5 -- Highest priority: self
                    WHEN LOWER(username) LIKE LOWER(@query) THEN 1 -- 2nd priority: keyword is exactly the same as username
                    WHEN LOWER(name) LIKE LOWER(@query) THEN 2 -- 3rd priority: keyword is exactly the same as name
                    WHEN LOWER(username) LIKE LOWER(@query + '%') THEN 3 -- 4th priority: keyword occurs at beginning of username
                    WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 4 -- 5th priority: keyword occurs at beginning of name
                    WHEN LOWER(username) LIKE LOWER('%' + @query) THEN 5 -- 6th priority: keyword occurs at very end of username
                    WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 6 -- 7th priority: keyword occurs at very end of name
                    WHEN LOWER(username) LIKE LOWER('%' + @query + '%') THEN 7 -- 8th priority: keyword occurs in middle of username
                    ELSE 8 -- 9th priority: keyword occurs in middle of name
                    END;
        END

    -- Check to see if artists should be returned in result
    IF (@show_artists = 'y')
        BEGIN
            -- Gets the top n entries of the artists whose name and/or username contain the keyword
            INSERT INTO @Result
            SELECT TOP(@num) 'artist' AS type, artist.artist_id AS id, 0 AS sec_id, music_user.name AS name
            FROM music_user
                     JOIN artist ON music_user.user_id = artist.artist_id
            WHERE (LOWER(music_user.name) LIKE LOWER('%' + @query + '%') OR LOWER(music_user.username) LIKE LOWER('%' + @query + '%')) -- Check if keyword is in username or name of artist
            ORDER BY
                CASE
                    WHEN @order_by = 2 THEN -- Order by average rating, descending
                        CASE
                            WHEN dbo.avg_rating_artist(artist_id) IS NULL THEN 10 -- If artist has no rating, they are placed at the bottom
                            ELSE 10 - dbo.avg_rating_artist(artist_id) -- If artists do have ratings, order by descending order
                            END
                    WHEN @order_by = 3 THEN -dbo.num_plays_artist(artist_id) -- Order by popularity (number of plays), descending
                    ELSE -- Order by relevance
                        CASE
                            WHEN @ID = artist_id THEN 0.5 -- Highest priority: self
                            WHEN LOWER(username) LIKE LOWER(@query) THEN 1 -- 2nd priority: keyword is exactly the same as username
                            WHEN LOWER(name) LIKE LOWER(@query) THEN 2 -- 3rd priority: keyword is exactly the same as name
                            WHEN LOWER(username) LIKE LOWER(@query + '%') THEN 3 -- 4th priority: keyword occurs at beginning of username
                            WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 4 -- 5th priority: keyword occurs at beginning of name
                            WHEN LOWER(username) LIKE LOWER('%' + @query) THEN 5 -- 6th priority: keyword occurs at very end of username
                            WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 6 -- 7th priority: keyword occurs at very end of name
                            WHEN LOWER(username) LIKE LOWER('%' + @query + '%') THEN 7 -- 8th priority: keyword occurs in middle of username
                            ELSE 8
                            END
                    END;
        END

    -- Check to see if albums should be returned in result
    IF (@show_albums = 'y')
        BEGIN
            -- Gets the top n entries of the albums whose name contain the keyword
            INSERT INTO @Result
            SELECT TOP(@num) 'album' AS type, album_id AS id, 0 AS sec_id, name
            FROM album
            WHERE LOWER(name) LIKE LOWER('%' + @query + '%') -- Check if keyword is in name of album
            ORDER BY
                CASE
                    WHEN @order_by = 2 THEN -- Order by average rating, descending
                        CASE
                            WHEN dbo.avg_rating_album(album_id) IS NULL THEN 10
                            ELSE 10 - dbo.avg_rating_album(album_id)
                            END
                    WHEN @order_by = 3 THEN -dbo.num_plays_album(album_id) -- Order by popularity (number of plays), descending
                    ELSE -- Order by relevance
                        CASE
                            WHEN LOWER(name) LIKE LOWER(@query) THEN 1
                            WHEN LOWER(name) LIKE LOWER(@query + '%') THEN 2
                            WHEN LOWER(name) LIKE LOWER('%' + @query) THEN 3
                            ELSE 4
                            END
                    END;
        END

    SELECT * FROM @Result; -- Return all search entries
END;
GO

/*
 Finds the average rating of a particular song.

 Parameters:
    @ID: The song ID
 Returns: The average rating of the song, as a float. If the song has no ratings or if the song cannot be found, it
    returns NULL.
 */
CREATE OR ALTER FUNCTION avg_rating_song(@ID int)
    RETURNS float
AS
BEGIN
    DECLARE @avg_rating float
    SELECT @avg_rating = avg(CAST(rating AS float))
    FROM rating
    WHERE rating.song_id = @ID
    RETURN @avg_rating;
END
GO

/*
 Finds the average rating of a particular artist. This is calculated by taking the average of all of the artists'
 average song rating.

 Parameters:
    @ID: The artist ID
 Returns: The average rating of the artist, as a float. If the artist has no ratings or if the artist cannot be found,
    it returns NULL.
 Uses:
    Function avg_rating_song
 */
CREATE OR ALTER FUNCTION avg_rating_artist(@ID int)
    RETURNS float
AS
BEGIN
    DECLARE @avg_rating float
    SELECT @avg_rating = avg(dbo.avg_rating_song(song_id))
    FROM song_artist
    WHERE artist_id = @ID AND dbo.avg_rating_song(song_id) IS NOT NULL -- Make sure song rating is not NULL
    RETURN @avg_rating;
END
GO

/*
 Finds the average rating of a particular album. This is calculated by taking the average of all of the song ratings for
 the album.

 Parameters:
    @ID: The album ID
 Returns: The average rating of the album, as a float. If the album has no ratings or if the album cannot be found, it
    returns NULL.
 Uses:
    Function avg_rating_song
 */
CREATE OR ALTER FUNCTION avg_rating_album(@ID int)
    RETURNS float
AS
BEGIN
    DECLARE @avg_rating float
    SELECT @avg_rating = avg(dbo.avg_rating_song(song_id))
    FROM song_album
    WHERE album_id = @ID AND dbo.avg_rating_song(song_id) IS NOT NULL -- Make sure song rating is not NULL
    RETURN @avg_rating;
END
GO
-- END USE CASE 1

-- BEGIN USE CASE 2
/*
 Deletes a user's playlist.

 Parameters:
    @user_id: User ID of the user
    @playlist_id: Playlist ID of the playlist
 */
CREATE OR ALTER PROCEDURE delete_playlist
    @user_id int,
    @playlist_id int
AS
BEGIN
    DELETE FROM playlist
    WHERE user_id = @user_id AND playlist_id = @playlist_id; -- Checks to make sure user and playlist ID's are the same
END;
GO

/*
 Creates a new playlist.

 Parameters:
    @user_id: User ID of the user
    @name: Name of the new playlist
    @is_public: 'y' or 'n', privacy of the playlist
 Uses:
    Function generate_playlist_id
 */
CREATE OR ALTER PROCEDURE make_playlist
    @user_id int,
    @name varchar(50),
    @is_public char(1)
AS
BEGIN
    INSERT INTO playlist(user_id, playlist_id, name, is_public)
    VALUES (@user_id, dbo.generate_playlist_id(@user_id), @name, @is_public);
END;
GO

/*
 Generates a new playlist ID for a newly inserted playlist.

 Parameters:
    @user_id: User ID of the user who is creating a playlist
 Returns: Newly-generated playlist ID
 */
CREATE OR ALTER FUNCTION generate_playlist_id(@user_id int)
    RETURNS int
AS
BEGIN
    DECLARE @Result int
    SELECT @Result = max(playlist_id) + 1 -- Finds the max playlist ID and adds one
    FROM playlist
    WHERE user_id = @user_id
    SELECT @Result =
           CASE
               WHEN @Result IS NULL THEN 1 -- If no playlists have been made yet, the default playlist ID is 1
               ELSE @Result
               END
    RETURN @Result;
END;
GO
-- END USE CASE 2

-- BEGIN USE CASE 3
/*
 Adds a song to a playlist.

 Parameters:
    @song_id: Song ID of song to add
    @user_id: User ID of the user who is adding the song
    @playlist_id: Playlist ID of the playlist the song will be added to
 */
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

/*
 Removes a song from a playlist.

 Parameters:
    @song_id: Song ID of song to remove
    @user_id: User ID of the user who is removing the song
    @playlist_id: Playlist ID of the playlist the song will be removed from
 */
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
-- END USE CASE 3

-- BEGIN USE CASE 4
/*
 Gets all song recommendations made by other users.

 Parameters:
    @ID: The User ID of the user who is viewing song recommendations
 Returns: Table consisting of all recommended song ID's, song names, and ID's of the user who recommended the song.
 */
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

/*
 Gets automatically-generated song recommendations.

 Parameters:
    @ID: The User ID of the user who is viewing song recommendations
    @num_recommendations: The max number of song recommendations per recommendation type
 Returns: Table consisting of the recommendation type (artist or genre), song ID's and names.
 */
CREATE OR ALTER PROCEDURE view_auto_recommendations
    @ID int,
    @num_recommendations int
AS
BEGIN
    /*
     Return table.

     Types:
        type: Type of song recommendation (e.g. 'artist' or 'genre')
        id: ID of song
        name: Name of song
     */
    DECLARE @Result TABLE
                    (
                        type varchar(10),
                        song_id int,
                        name varchar(100)
                    );

    -- Gets all artist song recommendations
    WITH temp_artist AS
             (
                 SELECT DISTINCT 'artist' AS type, song.song_id, song.name
                 FROM dbo.fav_artists(@ID, 10) AS temp
                          JOIN song_artist ON song_artist.artist_id = temp.artist_id
                          JOIN song ON song_artist.song_id = song.song_id
                          LEFT JOIN listens ON listens.song_id = song.song_id
                 WHERE listens.user_id IS NULL AND song.song_id NOT IN
                                                   (SELECT song_id FROM @Result) -- Checks to make sure the user hasn't already listened to the song
             )
    INSERT INTO @Result
    SELECT TOP(@num_recommendations) * FROM temp_artist -- Return n artist recommendations at most
    ORDER BY NEWID(); -- Randomize song recommendations

    -- Gets all genre song recommendations
    WITH temp_genre AS
             (
                 SELECT DISTINCT 'genre' AS type, song.song_id, song.name
                 FROM dbo.fav_genres(@ID, 3) AS temp
                          JOIN song_genre ON song_genre.genre_name = temp.genre_name
                          JOIN song ON song_genre.song_id = song.song_id
                          LEFT JOIN listens ON listens.song_id = song.song_id
                 WHERE listens.user_id IS NULL AND song.song_id NOT IN
                                                   (SELECT song_id FROM @Result) -- Checks to make sure the user hasn't already listened to the song, in addition to making sure that the recommendation hasn't already been recommended in temp_artist
             )
    INSERT INTO @Result
    SELECT TOP(@num_recommendations) * FROM temp_genre -- Return n genre recommendations at most
    ORDER BY NEWID(); -- Randomize song recommendations

    SELECT * FROM @Result; -- Return auto-generated recommendations
END;
GO

/*
 Makes a song recommendation to another user

 Parameters:
    @from_id: The User ID of the user making the song recommendation
    @to_id: The User ID of the user receiving the song recommendation
    @song_id: The Song ID of the song recommended
 */
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
-- END USE CASE 4

-- BEGIN USE CASE 5
/*
 Makes a rating and a review on a song.

 Parameters:
    @user_id: The User ID of the user making the review
    @song_id: The Song ID of the song being rated/reviewed
    @rating: The rating of the song
    @review: The review of the song
 */
CREATE OR ALTER PROCEDURE make_review
    @user_id int,
    @song_id int,
    @rating int,
    @review varchar(250) = ''
AS
BEGIN
    -- Checks if the review is empty
    IF @review = ''
        BEGIN
            INSERT INTO rating(user_id, song_id, rating)
            VALUES (@user_id, @song_id, @rating); -- Insert rating only
        END
    ELSE
        BEGIN
            INSERT INTO rating(user_id, song_id, rating, review)
            VALUES (@user_id, @song_id, @rating, @review); -- Insert rating and review
        END
END;
GO

/*
 Updates a rating and review of a song.

 Parameters:
    @user_id: The User ID of the user updating the review
    @song_id: The Song ID of the song being rated/reviewed
    @rating: The new rating of the song
    @review: The new review of the song
 */
CREATE OR ALTER PROCEDURE update_review
    @user_id int,
    @song_id int,
    @rating int,
    @review varchar(250) = ''
AS
BEGIN
    -- Checks if review is empty
    IF @review = ''
        BEGIN
            UPDATE rating
            SET rating = @rating, review = NULL, timestamp = CURRENT_TIMESTAMP -- Sets review to NULL and updates timestamp
            WHERE user_id = @user_id AND song_id = @song_id;
        END
    ELSE
        BEGIN
            UPDATE rating
            SET rating = @rating, review = @review, timestamp = CURRENT_TIMESTAMP -- Updates timestamp too
            WHERE user_id = @user_id AND song_id = @song_id; -- Updates both rating and review
        END
END;
GO
-- END USE CASE 5

-- BEGIN USE CASE 6
/*
 Returns the favorite genres of a user.

 Parameters:
    @ID: The User ID
    @num_genres: The maximum number of genres to be returned
 Returns: Table showing the genre names and total listens of the top genres.
 */
CREATE OR ALTER FUNCTION fav_genres(
    @ID int,
    @num_genres int)
RETURNS TABLE
AS
    RETURN
        (
            -- Returns top n favorite genres
            SELECT TOP (@num_genres) song_genre.genre_name, sum(listens.num_listens) AS total_listens -- Totals all listens of the genre
            FROM listens
                     JOIN song_genre ON listens.song_id = song_genre.song_id
            WHERE listens.user_id = @ID -- Restricts the number of listens to the user itself
            GROUP BY song_genre.genre_name
            ORDER BY total_listens DESC -- Genres with most listens are returned first
        );
GO

/*
 Returns the favorite artists of a user.

 Parameters:
    @ID: The User ID
    @num_artists: The maximum number of artists to be returned
 Returns: Table showing the artist names and total listens of the top artists.
 */
CREATE OR ALTER FUNCTION fav_artists(
    @ID int,
    @num_artists int)
RETURNS TABLE
AS
    RETURN
        (
            -- Returns top n favorite artists
            SELECT TOP (@num_artists) song_artist.artist_id, music_user.name, sum(listens.num_listens) AS total_listens -- Totals all listens of the artist
            FROM listens
                     JOIN song_artist ON listens.song_id = song_artist.song_id
                     JOIN music_user ON music_user.user_id = song_artist.artist_id
            WHERE listens.user_id = @ID -- Restricts the number of listens to the user itself
            GROUP BY song_artist.artist_id, music_user.name
            ORDER BY total_listens DESC -- Artists with most listens are returned first
        );
GO

/*
 Returns the favorite songs of a user.

 Parameters:
    @ID: The User ID
    @num_songs: The maximum number of songs to be returned
 Returns: Table showing the song names and total listens of the top songs.
 */
CREATE OR ALTER FUNCTION fav_songs(
    @ID int,
    @num_songs int)
RETURNS TABLE
AS
    RETURN
        (
            -- Returns top n favorite songs
            SELECT TOP (@num_songs) song.song_id, song.name, sum(listens.num_listens) AS total_listens -- Totals all listens of the song
            FROM listens
                     JOIN song ON song.song_id = listens.song_id
            WHERE listens.user_id = @ID -- Restricts the number of listens to the user itself
            GROUP BY song.song_id, song.name
            ORDER BY total_listens DESC -- Songs with most listens are returned first
        );
GO
-- END USE CASE 6

-- BEGIN USE CASE 7
CREATE OR ALTER VIEW highest_rated_songs
AS
    SELECT TOP(10) song_id, name, dbo.avg_rating_song(song_id) AS avg_rating
    FROM song
    ORDER BY dbo.avg_rating_song(song_id) DESC;
GO

CREATE OR ALTER VIEW most_popular_songs
AS
    SELECT TOP(10) song_id, name, dbo.num_plays_song(song_id) AS total_listens
    FROM song
    ORDER BY dbo.num_plays_song(song_id) DESC;
GO
-- END USE CASE 7

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

CREATE OR ALTER PROCEDURE add_to_album
    @song_id int,
    @album_id int
AS
BEGIN
    INSERT INTO song_album(song_id, album_id)
    VALUES (@song_id, @album_id);
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

CREATE OR ALTER PROCEDURE get_user_by_username
    @username varchar(30)
AS
BEGIN
    SELECT *
    FROM music_user
    WHERE @username = username;
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
