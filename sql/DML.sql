BEGIN TRANSACTION [Transaction1]

BEGIN TRY

--song(song_id, name, duration, release_date)

INSERT INTO song(name, duration, release_date)
VALUES
	('brutal', 143, '2021-05-21'),
	('traitor', 229, '2021-05-21'),
	('drivers license', 242, '2021-05-21'),
	('1 step forward, 3 steps back', 163, '2021-05-21'),
	('deja vu', 215, '2021-05-21'),
	('good 4 u', 178, '2021-05-21'),
	('enough for you', 202, '2021-05-21'),
	('happier', 175, '2021-05-21'),
	('jealousy, jealousy', 173, '2021-05-21'),
	('favorite crime', 152, '2021-05-21'),
	('hope ur ok', 209, '2021-05-21'),
	('Beatopia Cultsong', 151, '2022-07-15'),
	('10:36', 195, '2022-07-15'),
	('Sunny day', 160, '2022-07-15'),
	('See you Soon', 206, '2022-07-15'),
	('Ripples', 187, '2022-07-15'),
	('the perfect pair', 177, '2022-07-15'),
	('broken cd', 170, '2022-07-15'),
	('Talk', 158, '2022-07-15'),
	('Lovesong', 245, '2022-07-15'),
	('Pictures of Us', 279, '2022-07-15'),
	('fairy song', 164, '2022-07-15'),
	('Don''t get the deal', 220, '2022-07-15'),
	('tinkerbell is overrated', 228, '2022-07-15'),
	('You''re here that''s the thing', 198, '2022-07-15'),
	('Do you miss me?', 128, '2022-11-15'),
	('Don''t Stop ''Til You Get Enough', 365, '1979-08-10'),
	('Rock with You', 220, '1979-08-10'),
	('Workin'' Day and Night', 313, '1979-08-10'),
	('Get on the Floor', 277, '1979-08-10'),
	('Off the Wall', 246, '1979-08-10'),
	('Girlfriend', 184, '1979-08-10'),
	('She''s Out of My Life', 218, '1979-08-10'),
	('I Can''t Help It', 269, '1979-08-10'),
	('It''s the Falling in Love', 227, '1979-08-10'),
	('Burn This Disco Out', 221, '1979-08-10'),
	('Better Better', 210, '2017-12-06'),
	('I like you', 240, '2017-12-06'),
	('What Can I Do', 232, '2017-12-06'),
	('I''ll remember', 234, '2017-12-06'),
	('Whatever!', 196, '2017-12-06'),
	('Be Lazy', 194, '2017-12-06'),
	('Hi Hello', 232, '2017-12-06'),
	('I Loved You', 234, '2017-12-06'),
	('When you love someone', 226, '2017-12-06'),
	('All Alone', 224, '2017-12-06'),
	('Pouring', 245, '2017-12-06'),
	('I Need Somebody', 218, '2017-12-06'),
	('I''ll try', 223, '2017-12-06'),
	('GET IT', 151, '2022-03-25'),
	('SOMEBODY', 164, '2022-03-25'),
	('WESTSIDE', 184, '2022-03-25'),
	('TOUCH', 205, '2022-03-25'),
	('MILLI', 135, '2022-03-25'),
	('PÈRE', 48, '2022-03-25'),
	('HELL/HEAVEN', 160, '2022-03-25'),
	('ANGOSTURA', 171, '2022-03-25'),
	('UNDERSTAND', 150, '2022-03-25'),
	('LIMBO', 212, '2022-03-25'),
	('ANGEL', 247, '2022-03-25'),
	('GABRIEL', 128, '2022-03-25'),
	('Thank You', 275, '1994-08-30'),
	('All Around The World', 296, '1994-08-30'),
	('U Know', 286, '1994-08-30'),
	('Vibin''', 267, '1994-08-30'),
	('I Sit Away', 274, '1994-08-30'),
	('Jezzebel', 366, '1994-08-30'),
	('Khalil - Interlude', 101, '1994-08-30'),
	('Trying Times', 323, '1994-08-30'),
	('I''ll Make Love To You', 236, '1994-08-30'),
	('On Bended Knee', 329, '1994-08-30'),
	('50 Candles', 307, '1994-08-30'),
	('Water Runs Dry', 201, '1994-08-30'),
	('Yesterday', 188, '1994-08-30'),
	('1. Mars, the Bringer of War', 441, '1981-01-01'),
	('2. Venus, the Bringer of Peace',  517, '1981-01-01'),
	('3. Mercury, the Winged Messenger', 256, '1981-01-01'),
	('4. Jupiter, the Bringer of Jollity', 456, '1981-01-01'),
	('5. Saturn, the Bringer of Old Age', 562, '1981-01-01'),
	('6. Uranus, the Magician', 363, '1981-01-01'),
	('7. Neptune, the Mystic', 527, '1981-01-01');

--genre(name, description)

INSERT INTO genre(name, description)
VALUES
	('Rock', 'Music with simple tunes typically played with electric guitars and drums and sung'),
	('Jazz', 'Music that contains blues and swing notes'),
	('Electronic', 'Music in which synthesizers and other electronic instruments are the primary sources of sound'),
	('R&B', 'Music combining gospel, blues, and jazz influences'),
	('Pop', 'Popular music'),
	('Country', 'Music with its roots in western music and American folk music.'),
	('Classical', 'Music rooted in the traditions of Western music over the broad span of time from roughly the 11th century to the present day.'),
	('Hip-Hop', 'Music consisting of stylized rhythmic music that commonly accompanies rapping.'),
	('Alternative R&B', 'Music consisting of stylized rhythmic music that commonly accompanies rapping.'),
	('Alternative',  'Music that derives from mainstream pop or rock.'),
	('K-pop', 'Popular music originating from South Korea.');

--music_user(user_id, name, join_date)
INSERT INTO music_user(username, name, join_date)
VALUES
	('spikywater', 'Eric Zhang', '2015-12-29'),
	('gorilla', 'Felix Huang', '2017-10-20'),
	('rodrigo', 'Olivia Rodrigo', '2016-07-26'),
	('bbdb', 'beabadoobee', '2017-08-23'),
	('mj', 'Michael Jackson', '1971-10-07'),
	('day6', 'Day6', '2014-10-01'),
	('keshi', 'keshi', '2017-09-20'),
	('b2m', 'Boyz II Men', '1993-10-05'),
	('pinkpanther', 'PinkPantheress', '2021-10-15'),
	('holst', 'Gustav Holst', '1981-01-01');

INSERT INTO music_user(username, name)
VALUES
	('gaynell', 'Gaynell Jainschigg'),
	('nobelium', 'Nobel Zhou'),
	('sakin', 'Sakin Kirti');

--artist(artist_id)

INSERT INTO artist(artist_id)
VALUES
	(3),
	(4),
	(5),
	(6),
	(7),
	(8),
	(9),
	(10);

--album(album_id, name, release_date,)

INSERT INTO album(name, release_date)
VALUES
	('Sour', '2021-05-21'),
	('Beatopia', '2022-07-15'),
	('Off the Wall', '1979-08-10'),
	('MOONRISE', '2017-12-06'),
	('GABRIEL', '2022-03-25'),
	('II', '1994-08-30'),
	('The Planets, Op. 32', '1981-01-01');

--playlist(playlist_id, user_id, name, is_public)

INSERT INTO playlist(playlist_id, user_id, name, is_public)
VALUES
	(1, 1, 'gorilla brain', 'y'),
	(2, 1, 'jigglypuff', 'n'),
	(1, 2, 'chill', 'y'),
	(1, 12, 'dramatic vibes', 'n');

--song_artist(song_id, artist_id)

INSERT INTO song_artist(song_id, artist_id)
VALUES
	(1, 3),
	(2, 3),
	(3, 3),
	(4, 3),
	(5, 3),
	(6, 3),
	(7, 3),
	(8, 3),
	(9, 3),
	(10, 3),
	(11, 3),
	(12, 4),
	(13, 4),
	(14, 4),
	(15, 4),
	(16, 4),
	(17, 4),
	(18, 4),
	(19, 4),
	(20, 4),
	(21, 4),
	(22, 4),
	(23, 4),
	(24, 4),
	(25, 4),
	(24, 9),
	(26, 9),
	(27, 5),
	(28, 5),
	(29, 5),
	(30, 5),
	(31, 5),
	(32, 5),
	(33, 5),
	(34, 5),
	(35, 5),
	(36, 5),
	(37, 6),
	(38, 6),
	(39, 6),
	(40, 6),
	(41, 6),
	(42, 6),
	(43, 6),
	(44, 6),
	(45, 6),
	(46, 6),
	(47, 6),
	(48, 6),
	(49, 6),
	(50, 7),
	(51, 7),
	(52, 7),
	(53, 7),
	(54, 7),
	(55, 7),
	(56, 7),
	(57, 7),
	(58, 7),
	(59, 7),
	(60, 7),
	(61, 7),
	(62, 8),
	(63, 8),
	(64, 8),
	(65, 8),
	(66, 8),
	(67, 8),
	(68, 8),
	(69, 8),
	(70, 8),
	(71, 8),
	(72, 8),
	(73, 8),
	(74, 8),
	(75, 10),
	(76, 10),
	(77, 10),
	(78, 10),
	(79, 10),
	(80, 10),
	(81, 10);
	
--song_genre(song_id, genre_name)

INSERT INTO song_genre(song_id, genre_name)
VALUES
	(1, 'Pop'),
	(2, 'Pop'),
	(3, 'Pop'),
	(4, 'Pop'),
	(5, 'Pop'),
	(6, 'Pop'),
	(7, 'Pop'),
	(8, 'Pop'),
	(9, 'Pop'),
	(10, 'Pop'),
	(11, 'Pop'),
	(12, 'Alternative'),
	(13, 'Alternative'),
	(14, 'Alternative'),
	(15, 'Alternative'),
	(16, 'Alternative'),
	(17, 'Alternative'),
	(18, 'Alternative'),
	(19, 'Alternative'),
	(20, 'Alternative'),
	(21, 'Alternative'),
	(22, 'Alternative'),
	(23, 'Alternative'),
	(24, 'Alternative'),
	(25, 'Alternative'),
	(26, 'Alternative'),
	(27, 'Pop'),
	(27, 'R&B'),
	(28, 'Pop'),
	(28, 'R&B'),
	(29, 'Pop'),
	(29, 'R&B'),
	(30, 'Pop'),
	(30, 'R&B'),
	(31, 'Pop'),
	(31, 'R&B'),
	(32, 'Pop'),
	(32, 'R&B'),
	(33, 'Pop'),
	(33, 'R&B'),
	(34, 'Pop'),
	(34, 'R&B'),
	(35, 'Pop'),
	(35, 'R&B'),
	(36, 'Pop'),
	(36, 'R&B'),
	(37, 'K-pop'),
	(37, 'Rock'),
	(38, 'K-pop'),
	(38, 'Rock'),
	(39, 'K-pop'),
	(39, 'Rock'),
	(40, 'K-pop'),
	(40, 'Rock'),
	(41, 'K-pop'),
	(41, 'Rock'),
	(42, 'K-pop'),
	(42, 'Rock'),
	(43, 'K-pop'),
	(43, 'Rock'),
	(44, 'K-pop'),
	(44, 'Rock'),
	(45, 'K-pop'),
	(45, 'Rock'),
	(46, 'K-pop'),
	(46, 'Rock'),
	(47, 'K-pop'),
	(47, 'Rock'),
	(48, 'K-pop'),
	(48, 'Rock'),
	(49, 'K-pop'),
	(49, 'Rock'),
	(50, 'R&B'),
	(51, 'R&B'),
	(52, 'R&B'),
	(53, 'R&B'),
	(54, 'R&B'),
	(55, 'R&B'),
	(56, 'R&B'),
	(57, 'R&B'),
	(58, 'R&B'),
	(59, 'R&B'),
	(60, 'R&B'),
	(61, 'R&B'),
	(62, 'R&B'),
	(63, 'R&B'),
	(64, 'R&B'),
	(65, 'R&B'),
	(66, 'R&B'),
	(67, 'R&B'),
	(68, 'R&B'),
	(69, 'R&B'),
	(70, 'R&B'),
	(71, 'R&B'),
	(72, 'R&B'),
	(73, 'R&B'),
	(74, 'R&B'),
	(75, 'Classical'),
	(76, 'Classical'),
	(77, 'Classical'),
	(78, 'Classical'),
	(79, 'Classical'),
	(80, 'Classical'),
	(81, 'Classical');

--song_album(song_id, album_id)

INSERT INTO song_album(song_id, album_id)
VALUES
	(1, 1),
	(2, 1),
	(3, 1),
	(4, 1),
	(5, 1),
	(6, 1),
	(7, 1),
	(8, 1),
	(9, 1),
	(10, 1),
	(11, 1),
	(12, 2),
	(13, 2),
	(14, 2),
	(15, 2),
	(16, 2),
	(17, 2),
	(18, 2),
	(19, 2),
	(20, 2),
	(21, 2),
	(22, 2),
	(23, 2),
	(24, 2),
	(25, 2),
	(27, 3),
	(28, 3),
	(29, 3),
	(30, 3),
	(31, 3),
	(32, 3),
	(33, 3),
	(34, 3),
	(35, 3),
	(36, 3),
	(37, 4),
	(38, 4),
	(39, 4),
	(40, 4),
	(41, 4),
	(42, 4),
	(43, 4),
	(44, 4),
	(45, 4),
	(46, 4),
	(47, 4),
	(48, 4),
	(49, 4),
	(50, 5),
	(51, 5),
	(52, 5),
	(53, 5),
	(54, 5),
	(55, 5),
	(56, 5),
	(57, 5),
	(58, 5),
	(59, 5),
	(60, 5),
	(61, 5),
	(62, 6),
	(63, 6),
	(64, 6),
	(65, 6),
	(66, 6),
	(67, 6),
	(68, 6),
	(69, 6),
	(70, 6),
	(71, 6),
	(72, 6),
	(73, 6),
	(74, 6),
	(75, 7),
	(76, 7),
	(77, 7),
	(78, 7),
	(79, 7),
	(80, 7),
	(81, 7);

--album_artist(album_id, artist_id)

INSERT INTO album_artist(album_id, artist_id)
VALUES
	(1, 3),
	(2, 4),
	(3, 5), 
	(4, 6),
	(5, 7),
	(6, 8),
	(7, 10);

--album_genre(album_id, genre_name)

INSERT INTO album_genre(album_id, genre_name)
VALUES
	(1, 'Pop'),
	(2, 'Alternative'),
	(3, 'Pop'),
	(3, 'R&B'),		
	(4, 'K-pop'),
	(4, 'Rock'),	
	(5, 'R&B'),	
	(6, 'R&B'),	
	(7, 'Classical');

--song_playlist(song_id, user_id, playlist_id)

INSERT INTO song_playlist(song_id, user_id, playlist_id)
VALUES
	(75, 12, 1),
	(38, 1, 1),
	(44, 1, 1), 
	(45, 1, 1),
	(5, 1, 1),
	(2, 1, 1),
	(70, 1, 1),
	(14, 1, 2),
	(27, 1, 2),
	(28, 1, 2),
	(52, 1, 2),
	(62, 1, 2);

--rating(user_id, song_id, rating, review)

INSERT INTO rating(user_id, song_id, rating, review)
VALUES
	(12, 75, 10, 'So powerful and wondrous. Love the explosive brass!'),
	(12, 81, 9, 'The choir at the end is *chef'' kiss*'),
	(12, 80, 6, 'Kinda a forgettable movement ngl'),
	(1, 38, 10, 'Truly captures the raw emotions of what it is like to like someone.'), 
	(1, 59, 9, 'Really feeling the beat and the vibes of this one.'),
	(1, 70, 9, 'Definitely a shower song.'),
	(1, 54, 6, 'Personally not a fan.'),
	(1, 28, 10, 'Will never fail to make me stand up and dance.'),
	(1, 17, 8, 'Really fun song to play on the guitar'),
	(1, 6, 6, 'Generic pop song. Nothing too special.');	

INSERT INTO rating(user_id, song_id, rating)
VALUES
	(12, 39, 8),
	(12, 41, 7),
	(12, 78, 8),
	(1, 5, 9),
	(1, 19, 7),
	(1, 39, 6),
	(1, 57, 8),
	(1, 37, 7),
	(1, 71,7);
	
--listens(user_id, song_id, num_listens)

INSERT INTO listens(user_id, song_id, num_listens)
VALUES
	(12, 75, 4),
	(12, 77, 1),
	(12, 78, 6),
	(12, 79, 1),
	(12, 80, 1),
	(12, 81, 3),
	(12, 38, 1),
	(12, 39, 1),
	(12, 40, 1),
	(12, 41, 1),
	(12, 42, 1),
	(1, 38, 50),
	(1, 2, 10),
	(1, 14, 14),
	(1, 62, 2);


--recommendation(from_id, to_id, song_id)

INSERT INTO recommendation(from_id, to_id, song_id)
VALUES
	(12, 1, 75),
	(12, 2, 75),
	(12, 1, 81),
	(1, 2, 38),
	(1, 11, 5),
	(1, 12, 25);	

--Total Participation Checks

IF ((SELECT COUNT(*) FROM song WHERE song_id NOT IN (SELECT song_id FROM song_artist)) > 0)
BEGIN
	THROW 51001, 'Some songs do not have artists. Rolling back changes.', 1;
END

IF ((SELECT COUNT(*) FROM song WHERE song_id NOT IN (SELECT song_id FROM song_genre)) > 0)
BEGIN
	THROW 51002, 'Some songs do not have genres. Rolling back changes.', 1;
END

IF ((SELECT COUNT(*) FROM album WHERE album_id NOT IN (SELECT album_id FROM album_artist)) > 0)
BEGIN
	THROW 51003, 'Some albums do not have artists. Rolling back changes.', 1;
END

IF ((SELECT COUNT(*) FROM album WHERE album_id NOT IN (SELECT album_id FROM album_genre)) > 0)
BEGIN
	THROW 51004, 'Some albums do not have genres. Rolling back changes.', 1;
END

IF ((SELECT COUNT(*) FROM album WHERE album_id NOT IN (SELECT album_id FROM song_album)) > 0)
BEGIN
	THROW 51005, 'Some albums do not have any songs. Rolling back changes.', 1;
END

COMMIT TRANSACTION [Transaction1]

END TRY
	
BEGIN CATCH
	ROLLBACK TRANSACTION [Transaction1];
	THROW;
END CATCH
