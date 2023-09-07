USE busdb;

CREATE TABLE RegUser
(
    Id        NVARCHAR(30) NOT NULL,
    FirstName NVARCHAR(30) NOT NULL,
    LastName  NVARCHAR(30) NOT NULL,
    Password  NVARCHAR(30) NOT NULL,
    Role      INT          NOT NULL,

    CONSTRAINT PkRegUser_Id PRIMARY KEY (Id),
    CONSTRAINT CkRegUser_Role CHECK (Role IN (0, 1, 2)),


);

------------- ANBOTHER APPORACH ----------------

CREATE TABLE Location
(
    Id   NVARCHAR(20) NOT NULL,
    Name NVARCHAR(30) NOT NULL,


    CONSTRAINT PkLocation_Id PRIMARY KEY (Id),
    CONSTRAINT UqLocation_Name UNIQUE (Name)

)


CREATE TABLE Bus
(
    Id        NVARCHAR(30) NOT NULL,
    FromId    NVARCHAR(20) NOT NULL,
    ToId      NVARCHAR(20) NOT NULL,
    DateRun   DATETIME2    NOT NULL,
    SeatCount INT          NOT NULL,
    Price     FLOAT        NOT NULL,

    CONSTRAINT PkBus_Id PRIMARY KEY (Id),
    CONSTRAINT FkBus_FromId FOREIGN KEY (FromId) REFERENCES Location (Id),
    CONSTRAINT FkBus_ToId FOREIGN KEY (ToId) REFERENCES Location (Id),
    CONSTRAINT CkBus_DateRun CHECK (DateRun >= GETDATE()),
    CONSTRAINT CkBus_SeatCount CHECK (SeatCount > 0),
    CONSTRAINT CkBus_Price CHECK (Price > 0)

)

ALTER TABLE Bus
    ADD CONSTRAINT CkBus_DateRun CHECK (DateRun >= GETDATE());

CREATE TABLE Seat
(
    Id       INT IDENTITY (1,1) NOT NULL,
    BusId    NVARCHAR(30)       NOT NULL,
    SeatNo   INT                NOT NULL,
    IsBooked BIT DEFAULT 0,

    CONSTRAINT PkSeat_Id PRIMARY KEY (Id),
    CONSTRAINT FkBus_BusId FOREIGN KEY (BusId) REFERENCES Bus (Id),

);

CREATE TABLE Ticket
(
    Id                INT IDENTITY (1,1) NOT NULL,
    SeatId            INT                NOT NULL,
    UserId            NVARCHAR(30),
    Status            INT                NOT NULL DEFAULT 1,

    PassengerName     NVARCHAR(100)      NOT NULL,
    PassengerIdentity NVARCHAR(20)       NOT NULL,
    PassengerIsChild  BIT                         DEFAULT 0,

    CONSTRAINT PkTicket_Id PRIMARY KEY (Id),
    CONSTRAINT FkTicket_SeatId FOREIGN KEY (SeatId) REFERENCES Seat (Id),
    CONSTRAINT FkTicket_UserId FOREIGN KEY (UserId) REFERENCES RegUser (Id),
    CONSTRAINT CkTicket_Status CHECK (Status IN (0, 1, 2))
);

CREATE TABLE Wallet
(

    UserId  NVARCHAR(30) NOT NULL,
    Balance FLOAT        NOT NULL DEFAULT 0,

    CONSTRAINT PkWallet_UserId PRIMARY KEY (UserId),
    CONSTRAINT FkWallet_UserId FOREIGN KEY (UserId) REFERENCES RegUser (Id),
    CONSTRAINT FkWallet_Balance CHECK (Balance >= 0)

);

INSERT INTO Wallet(UserId, Balance)
VALUES ('tungnd', 2500);
INSERT INTO Wallet(UserId, Balance)
VALUES ('tiendat', 3500);

DROP PROC spCreateTicket;

EXEC spCreateTicket 'tungnd', 1, 'Nguyen Tung', '123123', 0
CREATE PROCEDURE spCreateTicket(@userId NVARCHAR(30), @seatId INT,
                                @passengerName NVARCHAR(100), @passengerIdentity NVARCHAR(20),
                                @passengerIsChild BIT) AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        IF (@passengerIsChild = 0)
            BEGIN
                IF EXISTS(SELECT 1
                          FROM Ticket
                          WHERE PassengerIdentity = @passengerIdentity
                            AND Status = 1)
                    RAISERROR ('TicketError-1-Duplicated Passengers', 16, 1);

                INSERT INTO Ticket(SeatId, UserId, PassengerName, PassengerIdentity)
                VALUES (@seatId, @userId, @passengerName, @passengerIdentity);

                UPDATE Seat SET IsBooked = 1 WHERE Id = @seatId;
            END
        ELSE
            BEGIN

                IF NOT EXISTS(SELECT 1
                              FROM Ticket
                              WHERE UserId = @userId
                                AND PassengerIdentity = @passengerIdentity
                                AND Status = 1
                                AND PassengerIsChild = 0)
                    RAISERROR ('TicketError-2-Caretaker Not Found', 16, 1);

                IF (
                        (SELECT COUNT(Id)
                         FROM Ticket
                         WHERE UserId = @userId
                           AND PassengerIdentity = @passengerIdentity
                           AND Status = 1
                           AND PassengerIsChild = 1) = 2
                    )
                    RAISERROR ('TicketError-3-Exceeded children count per caretaker', 16, 1);

                INSERT INTO Ticket (SeatId, UserId, PassengerName, PassengerIdentity, PassengerIsChild)
                VALUES (@seatId, @userId, @passengerName, @passengerIdentity, 1);

                UPDATE Seat SET IsBooked = 1 WHERE Id = @seatId;
            END


    END TRY
    BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();

        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;

        SET NOCOUNT OFF;

        RAISERROR (@ErrorMessage, @ErrorSeverity, @ErrorState);
    END CATCH;
    IF @@TRANCOUNT > 0 COMMIT TRANSACTION;

    SET NOCOUNT OFF;

END
GO;


CREATE PROCEDURE spGenerateSeatForBus(@busId NVARCHAR(30))
AS
BEGIN
    DECLARE @seatNum INT = (SELECT SeatCount FROM Bus WHERE Id = @busId);
    DECLARE @count INT = 1;
    WHILE @count <= @seatNum
        BEGIN
            INSERT INTO Seat(BusId, SeatNo) VALUES (@busId, @count)
            SET @count = @count + 1;
        END
END
GO;


DROP PROC spCreateBus;
GO;
CREATE PROCEDURE spCreateBus(@id NVARCHAR(30),
                             @fromId NVARCHAR(20),
                             @toId NVARCHAR(20),
                             @dateRun DATETIME2,
                             @seatCount INT,
                             @price FLOAT)
AS
BEGIN

    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        INSERT INTO Bus(Id, FromId, ToId, DateRun, SeatCount, Price)
        VALUES (@id, @fromId, @toId, @dateRun, @seatCount, @price);

        EXEC spGenerateSeatForBus @id;
    END TRY
    BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();

        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;

        SET NOCOUNT OFF;

        RAISERROR (@ErrorMessage, @ErrorSeverity, @ErrorState);
    END CATCH;
    IF @@TRANCOUNT > 0 COMMIT TRANSACTION;

    SET NOCOUNT OFF;

END
GO;

--- Query to find the group 10 of the MOST POPULAR trips
SELECT TOP 2 TMP.BusId, COUNT(TMP.Id) AS COUNT
FROM (SELECT Ticket.Id, Seat.BusId
      FROM Ticket
               INNER JOIN Seat ON Ticket.SeatId = Seat.Id
      WHERE Ticket.Status = 1) AS TMP
GROUP BY BusId
ORDER BY COUNT DESC;

--Query to find the date list that a set of trips specified by From & To is available


SELECT *
FROM Seat
         INNER JOIN Bus ON Seat.BusId = Bus.Id;


SELECT TOP 10 COUNT(*) AS CountBookers
FROM Ticket
GROUP BY Ticket.SeatId

EXEC spCreateBus 'B0001', 'HCMC', 'DALAT', '08-25-2020 17:30:00', 40, 200;
EXEC spCreateBus 'B0002', 'HCMC', 'DALAT', '08-25-2020 05:00:00', 40, 170;


INSERT INTO busdb.dbo.Location (Id, Name)
VALUES (N'ANGI', N'An Giang');
INSERT INTO busdb.dbo.Location (Id, Name)
VALUES (N'CAMAU', N'Cà Mau');
INSERT INTO busdb.dbo.Location (Id, Name)
VALUES (N'DALAT', N'Đà Lat');
INSERT INTO busdb.dbo.Location (Id, Name)
VALUES (N'DANA', N'Đà Nẵng');
INSERT INTO busdb.dbo.Location (Id, Name)
VALUES (N'DONA', N'Đồng Nai');
INSERT INTO busdb.dbo.Location (Id, Name)
VALUES (N'HANOI', N'Hà Nội');
INSERT INTO busdb.dbo.Location (Id, Name)
VALUES (N'HCMC', N'Hồ Chí Minh');
INSERT INTO busdb.dbo.Location (Id, Name)
VALUES (N'HUE', N'Huế');


EXEC spCreateBus 'B0007', 'HCMC', 'DALAT', '08-10-2020', 32, 150;
EXEC spCreateBus 'B0008', 'HCMC', 'DALAT', '08-10-2020', 40, 150;
EXEC spCreateBus 'B0009', 'HCMC', 'DALAT', '08-10-2020 09:30:22', 40, 150;


SELECT *
FROM Ticket
         INNER JOIN Seat S ON Ticket.SeatId = S.Id
         INNER JOIN Bus B ON S.BusId = B.Id
WHERE Ticket.Status = 1
  AND B.Id = 'B0002'
  AND Ticket.UserId = 'tiendat'
  AND PassengerIdentity = '2222'
  AND PassengerIsChild = 0