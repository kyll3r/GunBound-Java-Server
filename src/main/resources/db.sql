-- --------------------------------------------------------
-- Host:                         217.196.60.115
-- Server version:               10.11.13-MariaDB-0ubuntu0.24.04.1 - Ubuntu 24.04
-- Server OS:                    debian-linux-gnu
-- HeidiSQL Version:             12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Dumping structure for table gbth.banlog
CREATE TABLE IF NOT EXISTS `banlog` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `StartTime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `UserId` varchar(16) NOT NULL DEFAULT '',
  `Duration` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `Reason` varchar(255) NOT NULL DEFAULT '',
  `JudgeId` varchar(16) NOT NULL DEFAULT '',
  `JudgeNickName` varchar(16) NOT NULL DEFAULT '',
  PRIMARY KEY (`Idx`) USING BTREE,
  KEY `Id` (`UserId`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.buddylist
CREATE TABLE IF NOT EXISTS `buddylist` (
  `UserId` varchar(16) NOT NULL DEFAULT '',
  `Category` char(30) DEFAULT '',
  `BuddyId` varchar(16) NOT NULL DEFAULT '',
  UNIQUE KEY `uniq_userid_buddyid` (`UserId`,`BuddyId`),
  KEY `Id` (`UserId`,`BuddyId`) USING BTREE,
  CONSTRAINT `fk_buddylist_userid` FOREIGN KEY (`UserId`) REFERENCES `user` (`UserId`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.cash
CREATE TABLE IF NOT EXISTS `cash` (
  `UserId` varchar(16) NOT NULL,
  `Cash` int(11) DEFAULT 0,
  PRIMARY KEY (`UserId`),
  CONSTRAINT `fk_cash_userid` FOREIGN KEY (`UserId`) REFERENCES `user` (`UserId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.chest
CREATE TABLE IF NOT EXISTS `chest` (
  `Idx` int(10) NOT NULL AUTO_INCREMENT,
  `Item` int(11) NOT NULL,
  `Wearing` varchar(1) DEFAULT '0',
  `Acquisition` varchar(1) DEFAULT '0',
  `Expire` datetime DEFAULT NULL,
  `Volume` tinyint(1) DEFAULT NULL,
  `PlaceOrder` varchar(50) DEFAULT '0',
  `Recovered` varchar(50) DEFAULT '0',
  `OwnerId` varchar(16) NOT NULL,
  `ExpireType` varchar(255) DEFAULT NULL,
  `Active` tinyint(4) DEFAULT 1,
  PRIMARY KEY (`Idx`) USING BTREE,
  KEY `OwnerId` (`OwnerId`),
  CONSTRAINT `fk_chest_owner` FOREIGN KEY (`OwnerId`) REFERENCES `user` (`UserId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4294 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci COMMENT='Itens de inventÃƒÂ¡rio do jogador';

-- Data exporting was unselected.

-- Dumping structure for table gbth.collection
CREATE TABLE IF NOT EXISTS `collection` (
  `User` varchar(16) DEFAULT NULL,
  `_0` int(11) DEFAULT NULL,
  `_1` int(11) DEFAULT NULL,
  `_2` int(11) DEFAULT NULL,
  `_3` int(11) DEFAULT NULL,
  `_4` int(11) DEFAULT NULL,
  `_5` int(11) DEFAULT NULL,
  `_6` int(11) DEFAULT NULL,
  `_7` int(11) DEFAULT NULL,
  `_8` int(11) DEFAULT NULL,
  `_9` int(11) DEFAULT NULL,
  `_10` int(11) DEFAULT NULL,
  `_11` int(11) DEFAULT NULL,
  `_12` int(11) DEFAULT NULL,
  `_13` int(11) DEFAULT NULL,
  `_14` int(11) DEFAULT NULL,
  `_15` int(11) DEFAULT NULL,
  `_16` int(11) DEFAULT NULL,
  `_17` int(11) DEFAULT NULL,
  `_18` int(11) DEFAULT NULL,
  `_19` int(11) DEFAULT NULL,
  `_20` int(11) DEFAULT NULL,
  `_21` int(11) DEFAULT NULL,
  `_22` int(11) DEFAULT NULL,
  `_23` int(11) DEFAULT NULL,
  `_24` int(11) DEFAULT NULL,
  `_25` int(11) DEFAULT NULL,
  `_26` int(11) DEFAULT NULL,
  `_27` int(11) DEFAULT NULL,
  `_28` int(11) DEFAULT NULL,
  `_29` int(11) DEFAULT NULL,
  `_30` int(11) DEFAULT NULL,
  `_31` int(11) DEFAULT NULL,
  `_32` int(11) DEFAULT NULL,
  `_33` int(11) DEFAULT NULL,
  `_255` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.country_reference
CREATE TABLE IF NOT EXISTS `country_reference` (
  `Country_Count` int(11) NOT NULL AUTO_INCREMENT,
  `Country_Number` int(11) NOT NULL DEFAULT 0,
  `Country_Name` varchar(200) NOT NULL DEFAULT '',
  PRIMARY KEY (`Country_Count`) USING BTREE,
  KEY `Country_Name` (`Country_Name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=244 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.currentuser
CREATE TABLE IF NOT EXISTS `currentuser` (
  `ServerIp` int(11) DEFAULT 0,
  `ServerPort` int(10) unsigned DEFAULT 0,
  `Context` varchar(20) DEFAULT '0',
  `LoggingTime` timestamp NULL DEFAULT NULL,
  `Id` varchar(16) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.game
CREATE TABLE IF NOT EXISTS `game` (
  `UserId` varchar(16) NOT NULL,
  `NickName` varchar(16) NOT NULL DEFAULT '',
  `Guild` varchar(8) NOT NULL DEFAULT '',
  `GuildRank` int(11) NOT NULL DEFAULT 0,
  `MemberGuildCount` smallint(6) NOT NULL DEFAULT 0,
  `Gold` int(10) unsigned NOT NULL DEFAULT 0,
  `Cash` int(10) unsigned NOT NULL DEFAULT 0,
  `EventScore0` int(11) NOT NULL DEFAULT 0,
  `EventScore1` int(11) NOT NULL DEFAULT 0,
  `EventScore2` int(11) NOT NULL DEFAULT 0,
  `EventScore3` int(11) NOT NULL DEFAULT 0,
  `Prop1` varchar(201) NOT NULL DEFAULT '',
  `Prop2` varchar(201) NOT NULL DEFAULT '',
  `AdminGift` smallint(6) NOT NULL DEFAULT 0,
  `TotalScore` int(11) NOT NULL DEFAULT 1000,
  `SeasonScore` int(11) NOT NULL DEFAULT 1000,
  `TotalGrade` smallint(6) NOT NULL DEFAULT 19,
  `SeasonGrade` smallint(6) NOT NULL DEFAULT 19,
  `TotalRank` int(11) NOT NULL DEFAULT 0,
  `SeasonRank` int(11) NOT NULL DEFAULT 0,
  `AccumShot` int(10) unsigned NOT NULL DEFAULT 0,
  `AccumDamage` int(10) unsigned NOT NULL DEFAULT 0,
  `LastUpdateTime` timestamp NULL DEFAULT NULL,
  `NoRankUpdate` tinyint(1) NOT NULL DEFAULT 0,
  `ClientData` varbinary(200) DEFAULT NULL,
  `Country` int(11) NOT NULL DEFAULT 0,
  `GiftProhibitTime` timestamp NOT NULL DEFAULT '2000-01-01 08:00:00',
  `CorGame` tinyint(4) NOT NULL DEFAULT 0,
  `CorGameTime` datetime NOT NULL DEFAULT '2000-01-01 08:00:00',
  `CorGameBalao` tinyint(4) NOT NULL DEFAULT 0,
  `CorGameBalaoTime` datetime NOT NULL DEFAULT '2000-01-01 08:00:00',
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `NickName_UNIQUE` (`NickName`),
  KEY `UserId` (`UserId`),
  KEY `Guild` (`Guild`),
  CONSTRAINT `game_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `user` (`UserId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.giftitem
CREATE TABLE IF NOT EXISTS `giftitem` (
  `No` int(11) NOT NULL AUTO_INCREMENT,
  `Sender` varchar(16) DEFAULT NULL,
  `MenuId` int(11) DEFAULT 0,
  `ReceiptGiftNo` int(16) DEFAULT NULL,
  `Acquisition` varchar(255) DEFAULT NULL,
  `Receiver` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`No`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Data exporting was unselected.

-- Dumping structure for table gbth.giftmsg
CREATE TABLE IF NOT EXISTS `giftmsg` (
  `No` int(11) NOT NULL AUTO_INCREMENT,
  `MenuId` int(16) DEFAULT NULL,
  `Sender` varchar(16) DEFAULT NULL,
  `SentTime` timestamp NULL DEFAULT NULL,
  `Msg` varchar(255) DEFAULT NULL,
  `MsgType` varchar(255) DEFAULT NULL,
  `ExpireType` varchar(11) DEFAULT '0',
  `GiftItemNo` int(16) DEFAULT NULL,
  `Receiver` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`No`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Data exporting was unselected.

-- Dumping structure for table gbth.grandprix_group
CREATE TABLE IF NOT EXISTS `grandprix_group` (
  `Category` varchar(50) DEFAULT NULL,
  `ScoreOnCalc` varchar(50) DEFAULT NULL,
  `Rank` varchar(50) DEFAULT NULL,
  `RankOld` varchar(50) DEFAULT NULL,
  `Win` varchar(50) DEFAULT NULL,
  `Lose` varchar(50) DEFAULT NULL,
  `Score` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.grandprix_personal
CREATE TABLE IF NOT EXISTS `grandprix_personal` (
  `Score` int(11) DEFAULT NULL,
  `Rank` int(11) DEFAULT NULL,
  `RankOld` int(11) DEFAULT NULL,
  `Win` int(11) DEFAULT NULL,
  `Lose` int(11) DEFAULT NULL,
  `ScoreOnCalc` varchar(50) DEFAULT NULL,
  `User` varchar(50) DEFAULT NULL,
  `Category` varchar(50) DEFAULT NULL,
  `Id` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.guildweb
CREATE TABLE IF NOT EXISTS `guildweb` (
  `Guild` varchar(8) NOT NULL DEFAULT '',
  `User_GMaster` varchar(16) NOT NULL DEFAULT '',
  `URL_img` text DEFAULT NULL,
  `Description` text DEFAULT NULL,
  `Requirements` text DEFAULT NULL,
  PRIMARY KEY (`Guild`),
  KEY `idx_User_GMaster` (`User_GMaster`),
  CONSTRAINT `fk_guildweb_user` FOREIGN KEY (`User_GMaster`) REFERENCES `user` (`UserId`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.honklog
CREATE TABLE IF NOT EXISTS `honklog` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `UserId` varchar(16) NOT NULL DEFAULT '',
  `UserNickName` varchar(16) NOT NULL DEFAULT '',
  `Phrase` varchar(255) NOT NULL DEFAULT '',
  `Time` datetime NOT NULL DEFAULT '2000-01-01 00:00:00',
  PRIMARY KEY (`Idx`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Data exporting was unselected.

-- Dumping structure for table gbth.item
CREATE TABLE IF NOT EXISTS `item` (
  `No` int(11) NOT NULL DEFAULT 0,
  `Refund_B` int(11) DEFAULT NULL,
  `Refund_C` int(11) DEFAULT 0,
  `Refund_E` int(11) DEFAULT 0,
  `Refund_G` int(11) DEFAULT 0,
  `Refund_T` int(11) DEFAULT 0,
  `Refund_J` int(11) DEFAULT NULL,
  PRIMARY KEY (`No`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.mathdata
CREATE TABLE IF NOT EXISTS `mathdata` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `Stage` int(11) DEFAULT 0,
  `Status` int(11) DEFAULT 0,
  `MatchStage` int(11) DEFAULT NULL,
  `MathId` int(11) DEFAULT 0,
  `Win` int(11) DEFAULT 0,
  `Code` varchar(255) DEFAULT NULL,
  `UId_1` varchar(16) DEFAULT NULL,
  `UId_2` varchar(16) DEFAULT NULL,
  `UId_3` varchar(16) DEFAULT NULL,
  `UId_4` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`Idx`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.menu
CREATE TABLE IF NOT EXISTS `menu` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `No` int(11) NOT NULL,
  `ItemCount` int(11) DEFAULT 0,
  `Item1` int(11) DEFAULT NULL,
  `Period1` int(10) unsigned DEFAULT NULL,
  `Volume1` int(10) unsigned DEFAULT NULL,
  `Item2` int(11) DEFAULT NULL,
  `Period2` int(10) unsigned DEFAULT NULL,
  `Volume2` int(10) unsigned DEFAULT NULL,
  `Item3` int(11) DEFAULT NULL,
  `Period3` int(10) unsigned DEFAULT NULL,
  `Volume3` int(10) unsigned DEFAULT NULL,
  `Item4` int(11) DEFAULT NULL,
  `Period4` int(10) unsigned DEFAULT NULL,
  `Volume4` int(10) unsigned DEFAULT NULL,
  `Item5` int(11) DEFAULT NULL,
  `Period5` int(10) unsigned DEFAULT NULL,
  `Volume5` int(10) unsigned DEFAULT NULL,
  `ImgNo` int(21) DEFAULT 0,
  `ImgShop` varchar(255) DEFAULT NULL,
  `Menu_Name` varchar(40) NOT NULL DEFAULT '?',
  `Menu_Desc` varchar(255) NOT NULL DEFAULT '?',
  `Menu_Image` varchar(255) NOT NULL DEFAULT '?',
  `Genero` varchar(11) DEFAULT NULL,
  `Part` varchar(10) DEFAULT NULL,
  `Delay` int(2) DEFAULT 0,
  `Popularity` int(2) DEFAULT 0,
  `Attack` int(2) DEFAULT 0,
  `Defense` int(2) DEFAULT 0,
  `Energy` int(2) DEFAULT 0,
  `Shield_Recovery` int(2) DEFAULT 0,
  `Item_Skip_Delay` int(2) DEFAULT 0,
  `Pit_Angle` int(2) DEFAULT 0,
  `Is_New` int(3) DEFAULT 0,
  `Is_Visible` tinyint(1) DEFAULT 0,
  `Seal_Enchant` tinyint(1) DEFAULT 0,
  `Can_Gift` tinyint(1) DEFAULT 0,
  `Can_Stack` tinyint(1) DEFAULT 0,
  `Color` int(3) DEFAULT 0,
  `ExType` int(1) DEFAULT NULL,
  `PlaceOrder` int(11) DEFAULT 0,
  `ShopOption1` tinyint(2) DEFAULT 0,
  `ShopOption2` tinyint(2) DEFAULT 0,
  `ShopOption3` tinyint(3) DEFAULT 0,
  `PriceByCashForH` int(10) DEFAULT NULL,
  `PriceByCashForD` int(10) DEFAULT NULL,
  `PriceByCashForW` int(10) unsigned DEFAULT NULL,
  `PriceByCashForM` int(10) unsigned DEFAULT NULL,
  `PriceByCashForY` int(10) unsigned DEFAULT NULL,
  `PriceByCashForI` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForH` int(10) DEFAULT NULL,
  `PriceByGoldForD` int(10) DEFAULT NULL,
  `PriceByGoldForW` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForM` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForY` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForI` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForW` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForM` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForI` int(10) unsigned DEFAULT NULL,
  `Enable_2Hour` int(1) DEFAULT 0,
  `Enable_Day` int(1) DEFAULT 0,
  `Enable_Week` int(1) DEFAULT 0,
  `Enable_Moth` int(1) DEFAULT 0,
  `Enable_Unlimited` int(1) DEFAULT 0,
  `Enable_Gold` int(1) DEFAULT 0,
  `Enable_Cash` int(1) DEFAULT 0,
  `Enable_GCoin` int(1) DEFAULT 0,
  `Type` int(2) DEFAULT 0,
  `Wearable` int(2) DEFAULT 0,
  `Location` int(3) DEFAULT NULL,
  `Dat` int(2) DEFAULT 0,
  PRIMARY KEY (`Idx`),
  UNIQUE KEY `No` (`No`)
) ENGINE=InnoDB AUTO_INCREMENT=4572 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Data exporting was unselected.

-- Dumping structure for table gbth.menu_snapshot
CREATE TABLE IF NOT EXISTS `menu_snapshot` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `No` int(11) NOT NULL,
  `ItemCount` int(11) DEFAULT 0,
  `Item1` int(11) DEFAULT NULL,
  `Period1` int(10) unsigned DEFAULT NULL,
  `Volume1` int(10) unsigned DEFAULT NULL,
  `Item2` int(11) DEFAULT NULL,
  `Period2` int(10) unsigned DEFAULT NULL,
  `Volume2` int(10) unsigned DEFAULT NULL,
  `Item3` int(11) DEFAULT NULL,
  `Period3` int(10) unsigned DEFAULT NULL,
  `Volume3` int(10) unsigned DEFAULT NULL,
  `Item4` int(11) DEFAULT NULL,
  `Period4` int(10) unsigned DEFAULT NULL,
  `Volume4` int(10) unsigned DEFAULT NULL,
  `Item5` int(11) DEFAULT NULL,
  `Period5` int(10) unsigned DEFAULT NULL,
  `Volume5` int(10) unsigned DEFAULT NULL,
  `ImgNo` int(21) DEFAULT 0,
  `ImgShop` varchar(255) DEFAULT NULL,
  `Menu_Name` varchar(40) NOT NULL DEFAULT '?',
  `Menu_Desc` varchar(255) NOT NULL DEFAULT '?',
  `Menu_Image` varchar(255) NOT NULL DEFAULT '?',
  `Genero` varchar(11) DEFAULT NULL,
  `Part` varchar(10) DEFAULT NULL,
  `Delay` int(2) DEFAULT 0,
  `Popularity` int(2) DEFAULT 0,
  `Attack` int(2) DEFAULT 0,
  `Defense` int(2) DEFAULT 0,
  `Energy` int(2) DEFAULT 0,
  `Shield_Recovery` int(2) DEFAULT 0,
  `Item_Skip_Delay` int(2) DEFAULT 0,
  `Pit_Angle` int(2) DEFAULT 0,
  `Is_New` int(3) DEFAULT 0,
  `Is_Visible` tinyint(1) DEFAULT 0,
  `Seal_Enchant` tinyint(1) DEFAULT 0,
  `Can_Gift` tinyint(1) DEFAULT 0,
  `Can_Stack` tinyint(1) DEFAULT 0,
  `Color` int(3) DEFAULT 0,
  `ExType` int(1) DEFAULT NULL,
  `PlaceOrder` int(11) DEFAULT 0,
  `ShopOption1` tinyint(2) DEFAULT 0,
  `ShopOption2` tinyint(2) DEFAULT 0,
  `ShopOption3` tinyint(3) DEFAULT 0,
  `PriceByCashForH` int(10) DEFAULT NULL,
  `PriceByCashForD` int(10) DEFAULT NULL,
  `PriceByCashForW` int(10) unsigned DEFAULT NULL,
  `PriceByCashForM` int(10) unsigned DEFAULT NULL,
  `PriceByCashForY` int(10) unsigned DEFAULT NULL,
  `PriceByCashForI` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForH` int(10) DEFAULT NULL,
  `PriceByGoldForD` int(10) DEFAULT NULL,
  `PriceByGoldForW` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForM` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForY` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForI` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForW` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForM` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForI` int(10) unsigned DEFAULT NULL,
  `Enable_2Hour` int(1) DEFAULT 0,
  `Enable_Day` int(1) DEFAULT 0,
  `Enable_Week` int(1) DEFAULT 0,
  `Enable_Moth` int(1) DEFAULT 0,
  `Enable_Unlimited` int(1) DEFAULT 0,
  `Enable_Gold` int(1) DEFAULT 0,
  `Enable_Cash` int(1) DEFAULT 0,
  `Enable_GCoin` int(1) DEFAULT 0,
  `Type` int(2) DEFAULT 0,
  `Wearable` int(2) DEFAULT 0,
  `Location` int(3) DEFAULT NULL,
  `Dat` int(2) DEFAULT 0,
  PRIMARY KEY (`Idx`),
  UNIQUE KEY `No` (`No`)
) ENGINE=InnoDB AUTO_INCREMENT=835 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Data exporting was unselected.

-- Dumping structure for table gbth.menu_without_ex
CREATE TABLE IF NOT EXISTS `menu_without_ex` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `No` int(11) NOT NULL,
  `ItemCount` int(11) DEFAULT 0,
  `Item1` int(11) DEFAULT NULL,
  `Period1` int(10) unsigned DEFAULT NULL,
  `Volume1` int(10) unsigned DEFAULT NULL,
  `Item2` int(11) DEFAULT NULL,
  `Period2` int(10) unsigned DEFAULT NULL,
  `Volume2` int(10) unsigned DEFAULT NULL,
  `Item3` int(11) DEFAULT NULL,
  `Period3` int(10) unsigned DEFAULT NULL,
  `Volume3` int(10) unsigned DEFAULT NULL,
  `Item4` int(11) DEFAULT NULL,
  `Period4` int(10) unsigned DEFAULT NULL,
  `Volume4` int(10) unsigned DEFAULT NULL,
  `Item5` int(11) DEFAULT NULL,
  `Period5` int(10) unsigned DEFAULT NULL,
  `Volume5` int(10) unsigned DEFAULT NULL,
  `ImgNo` int(21) DEFAULT 0,
  `ImgShop` varchar(255) DEFAULT NULL,
  `Menu_Name` varchar(40) NOT NULL DEFAULT '?',
  `Menu_Desc` varchar(255) NOT NULL DEFAULT '?',
  `Menu_Image` varchar(255) NOT NULL DEFAULT '?',
  `Genero` varchar(11) DEFAULT NULL,
  `Part` varchar(10) DEFAULT NULL,
  `Delay` int(2) DEFAULT 0,
  `Popularity` int(2) DEFAULT 0,
  `Attack` int(2) DEFAULT 0,
  `Defense` int(2) DEFAULT 0,
  `Energy` int(2) DEFAULT 0,
  `Shield_Recovery` int(2) DEFAULT 0,
  `Item_Skip_Delay` int(2) DEFAULT 0,
  `Pit_Angle` int(2) DEFAULT 0,
  `Is_New` int(3) DEFAULT 0,
  `Is_Visible` tinyint(1) DEFAULT 0,
  `Seal_Enchant` tinyint(1) DEFAULT 0,
  `Can_Gift` tinyint(1) DEFAULT 0,
  `Can_Stack` tinyint(1) DEFAULT 0,
  `Color` int(3) DEFAULT 0,
  `ExType` int(1) DEFAULT NULL,
  `PlaceOrder` int(11) DEFAULT 0,
  `ShopOption1` tinyint(2) DEFAULT 0,
  `ShopOption2` tinyint(2) DEFAULT 0,
  `ShopOption3` tinyint(3) DEFAULT 0,
  `PriceByCashForH` int(10) DEFAULT NULL,
  `PriceByCashForD` int(10) DEFAULT NULL,
  `PriceByCashForW` int(10) unsigned DEFAULT NULL,
  `PriceByCashForM` int(10) unsigned DEFAULT NULL,
  `PriceByCashForY` int(10) unsigned DEFAULT NULL,
  `PriceByCashForI` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForH` int(10) DEFAULT NULL,
  `PriceByGoldForD` int(10) DEFAULT NULL,
  `PriceByGoldForW` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForM` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForY` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForI` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForW` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForM` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForI` int(10) unsigned DEFAULT NULL,
  `Enable_2Hour` int(1) DEFAULT 0,
  `Enable_Day` int(1) DEFAULT 0,
  `Enable_Week` int(1) DEFAULT 0,
  `Enable_Moth` int(1) DEFAULT 0,
  `Enable_Unlimited` int(1) DEFAULT 0,
  `Enable_Gold` int(1) DEFAULT 0,
  `Enable_Cash` int(1) DEFAULT 0,
  `Enable_GCoin` int(1) DEFAULT 0,
  `Type` int(2) DEFAULT 0,
  `Wearable` int(2) DEFAULT 0,
  `Location` int(3) DEFAULT NULL,
  `Dat` int(2) DEFAULT 0,
  PRIMARY KEY (`Idx`),
  UNIQUE KEY `No` (`No`)
) ENGINE=InnoDB AUTO_INCREMENT=4516 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Data exporting was unselected.

-- Dumping structure for table gbth.mobilerecord
CREATE TABLE IF NOT EXISTS `mobilerecord` (
  `Win` int(11) NOT NULL DEFAULT 0,
  `Lose` int(11) NOT NULL DEFAULT 0,
  `UserId` varchar(16) NOT NULL DEFAULT '',
  `MobileId` tinyint(4) NOT NULL DEFAULT 0,
  UNIQUE KEY `key_User_Mobile` (`UserId`,`MobileId`) USING BTREE,
  CONSTRAINT `fk_mobilerecord_user` FOREIGN KEY (`UserId`) REFERENCES `user` (`UserId`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.msg
CREATE TABLE IF NOT EXISTS `msg` (
  `ID` varchar(16) NOT NULL DEFAULT '',
  `SerialNo` int(11) NOT NULL AUTO_INCREMENT,
  `MsgBoxText` varchar(50) DEFAULT '0',
  `MsgBoxType` varchar(255) DEFAULT NULL,
  `LinkUrl` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SerialNo`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.news
CREATE TABLE IF NOT EXISTS `news` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title_pt` varchar(255) NOT NULL COMMENT 'TÃ­tulo em PortuguÃªs (Fallback)',
  `title_en` varchar(255) DEFAULT NULL COMMENT 'TÃ­tulo em InglÃªs',
  `title_es` varchar(255) DEFAULT NULL COMMENT 'TÃ­tulo em Espanhol',
  `content_pt` text NOT NULL COMMENT 'ConteÃºdo em PortuguÃªs (Fallback)',
  `content_en` text DEFAULT NULL COMMENT 'ConteÃºdo em InglÃªs',
  `content_es` text DEFAULT NULL COMMENT 'ConteÃºdo em Espanhol',
  `category` enum('news','event','maintenance') NOT NULL DEFAULT 'news',
  `author` varchar(50) DEFAULT 'Admin',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `active` int(11) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.packet
CREATE TABLE IF NOT EXISTS `packet` (
  `SerialNo` int(11) NOT NULL DEFAULT 0,
  `Receiver` varchar(16) NOT NULL DEFAULT '',
  `Sender` varchar(16) NOT NULL DEFAULT '',
  `Code` smallint(5) unsigned NOT NULL DEFAULT 0,
  `Body` tinyblob NOT NULL,
  `Time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  UNIQUE KEY `unq_receiver_sender_code` (`Receiver`,`Sender`,`Code`),
  KEY `Receiver` (`Receiver`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.playlog
CREATE TABLE IF NOT EXISTS `playlog` (
  `ServerIP` int(10) unsigned NOT NULL DEFAULT 0,
  `GameRoomID` smallint(5) unsigned NOT NULL DEFAULT 0,
  `GameRoomTitle` varchar(64) NOT NULL DEFAULT '',
  `StartTime` datetime DEFAULT '0000-00-00 00:00:00',
  `EndTime` datetime DEFAULT '0000-00-00 00:00:00',
  `GameOption` int(10) unsigned NOT NULL DEFAULT 0,
  `WinTeamOrPlayer` tinyint(1) NOT NULL DEFAULT 0,
  `S0_ID` varchar(16) NOT NULL DEFAULT '',
  `S0_TeamID` tinyint(4) NOT NULL DEFAULT 0,
  `S0_DeadTime` datetime DEFAULT '0000-00-00 00:00:00',
  `S0_DeadCause` int(10) unsigned NOT NULL DEFAULT 0,
  `S0_ScoreDelta` smallint(6) NOT NULL DEFAULT 0,
  `S0_MoneyDelta` int(11) NOT NULL DEFAULT 0,
  `S1_ID` varchar(16) NOT NULL DEFAULT '',
  `S1_TeamID` tinyint(4) NOT NULL DEFAULT 0,
  `S1_DeadTime` datetime DEFAULT '0000-00-00 00:00:00',
  `S1_DeadCause` int(10) unsigned NOT NULL DEFAULT 0,
  `S1_ScoreDelta` smallint(6) NOT NULL DEFAULT 0,
  `S1_MoneyDelta` int(11) NOT NULL DEFAULT 0,
  `S2_ID` varchar(16) NOT NULL DEFAULT '',
  `S2_TeamID` tinyint(4) NOT NULL DEFAULT 0,
  `S2_DeadTime` datetime DEFAULT '0000-00-00 00:00:00',
  `S2_DeadCause` int(10) unsigned NOT NULL DEFAULT 0,
  `S2_ScoreDelta` smallint(6) NOT NULL DEFAULT 0,
  `S2_MoneyDelta` int(11) NOT NULL DEFAULT 0,
  `S3_ID` varchar(16) NOT NULL DEFAULT '',
  `S3_TeamID` tinyint(4) NOT NULL DEFAULT 0,
  `S3_DeadTime` datetime DEFAULT '0000-00-00 00:00:00',
  `S3_DeadCause` int(10) unsigned NOT NULL DEFAULT 0,
  `S3_ScoreDelta` smallint(6) NOT NULL DEFAULT 0,
  `S3_MoneyDelta` int(11) NOT NULL DEFAULT 0,
  `S4_ID` varchar(16) NOT NULL DEFAULT '',
  `S4_TeamID` tinyint(4) NOT NULL DEFAULT 0,
  `S4_DeadTime` datetime DEFAULT '0000-00-00 00:00:00',
  `S4_DeadCause` int(10) unsigned NOT NULL DEFAULT 0,
  `S4_ScoreDelta` smallint(6) NOT NULL DEFAULT 0,
  `S4_MoneyDelta` int(11) NOT NULL DEFAULT 0,
  `S5_ID` varchar(16) NOT NULL DEFAULT '',
  `S5_TeamID` tinyint(4) NOT NULL DEFAULT 0,
  `S5_DeadTime` datetime DEFAULT '0000-00-00 00:00:00',
  `S5_DeadCause` int(10) unsigned NOT NULL DEFAULT 0,
  `S5_ScoreDelta` smallint(6) NOT NULL DEFAULT 0,
  `S5_MoneyDelta` int(11) NOT NULL DEFAULT 0,
  `S6_ID` varchar(16) NOT NULL DEFAULT '',
  `S6_TeamID` tinyint(4) NOT NULL DEFAULT 0,
  `S6_DeadTime` datetime DEFAULT '0000-00-00 00:00:00',
  `S6_DeadCause` int(10) unsigned NOT NULL DEFAULT 0,
  `S6_ScoreDelta` smallint(6) NOT NULL DEFAULT 0,
  `S6_MoneyDelta` int(11) NOT NULL DEFAULT 0,
  `S7_ID` varchar(16) NOT NULL DEFAULT '',
  `S7_TeamID` tinyint(4) NOT NULL DEFAULT 0,
  `S7_DeadTime` datetime DEFAULT '0000-00-00 00:00:00',
  `S7_DeadCause` int(10) unsigned NOT NULL DEFAULT 0,
  `S7_ScoreDelta` smallint(6) NOT NULL DEFAULT 0,
  `S7_MoneyDelta` int(11) NOT NULL DEFAULT 0,
  KEY `S0_ID` (`S0_ID`),
  KEY `S1_ID` (`S1_ID`),
  KEY `S2_ID` (`S2_ID`),
  KEY `S3_ID` (`S3_ID`),
  KEY `S4_ID` (`S4_ID`),
  KEY `S5_ID` (`S5_ID`),
  KEY `S6_ID` (`S6_ID`),
  KEY `S7_ID` (`S7_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.receiptbuy
CREATE TABLE IF NOT EXISTS `receiptbuy` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `Consumer` varchar(16) DEFAULT NULL,
  `MenuId` int(11) DEFAULT NULL,
  `Idx_Chest` int(11) DEFAULT NULL,
  `CashChecked` int(11) DEFAULT NULL,
  `GoldChecked` int(11) DEFAULT NULL,
  `Time` timestamp NULL DEFAULT NULL,
  `BuyType` varchar(1) DEFAULT '',
  `ExpireType` varchar(1) DEFAULT NULL,
  `ReceiptGiftNo` int(11) DEFAULT NULL,
  PRIMARY KEY (`Idx`) USING BTREE,
  KEY `Id` (`Consumer`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2080 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.receiptconsume
CREATE TABLE IF NOT EXISTS `receiptconsume` (
  `Id` varchar(16) DEFAULT NULL,
  `Item` int(11) DEFAULT 0,
  `Volume` int(11) DEFAULT 0,
  `Recovered` varchar(255) DEFAULT NULL,
  `Refund` varchar(255) DEFAULT NULL,
  `Time` timestamp NULL DEFAULT NULL,
  `ExpireType` varchar(11) DEFAULT '0',
  `Expire` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.receiptgift
CREATE TABLE IF NOT EXISTS `receiptgift` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `Idx_chest` int(11) NOT NULL DEFAULT 0,
  `MenuId` int(10) unsigned DEFAULT 0,
  `Volume` tinyint(4) DEFAULT NULL,
  `Sender` varchar(16) DEFAULT NULL,
  `SenderNick` varchar(16) DEFAULT NULL,
  `Receiver` varchar(16) DEFAULT NULL,
  `ReceiverNick` varchar(16) DEFAULT NULL,
  `GiftTime` datetime DEFAULT NULL,
  `Expiretype` varchar(1) DEFAULT NULL,
  `Text` varchar(50) DEFAULT NULL,
  `ConfirmTime` datetime DEFAULT NULL,
  `Confirmed` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`Idx`) USING BTREE,
  UNIQUE KEY `Idx_Chest_UNIQUE` (`Idx_chest`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.receiptlog
CREATE TABLE IF NOT EXISTS `receiptlog` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `UserId` varchar(16) NOT NULL DEFAULT '',
  `UserNickName` varchar(16) NOT NULL DEFAULT '',
  `Item` varchar(20) NOT NULL DEFAULT '',
  `ItemDescription` varchar(255) NOT NULL DEFAULT '',
  `Time` datetime NOT NULL DEFAULT '2000-01-01 00:00:00',
  PRIMARY KEY (`Idx`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Data exporting was unselected.

-- Dumping structure for table gbth.receiptsell
CREATE TABLE IF NOT EXISTS `receiptsell` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `Consumer` varchar(16) DEFAULT NULL,
  `MenuId` int(11) DEFAULT NULL,
  `Idx_Chest` int(11) DEFAULT NULL,
  `CashChecked` int(11) DEFAULT NULL,
  `GoldChecked` int(11) DEFAULT NULL,
  `Time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`Idx`) USING BTREE,
  KEY `Id` (`Consumer`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.stagepos
CREATE TABLE IF NOT EXISTS `stagepos` (
  `MapId` int(11) NOT NULL,
  `Side` int(11) DEFAULT 0,
  `MapName` varchar(255) DEFAULT '',
  `PId` int(11) DEFAULT NULL,
  `Pos_X1` int(11) DEFAULT NULL,
  `Pos_X2` int(11) DEFAULT NULL,
  `Pos_Y` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for table gbth.stagerecord
CREATE TABLE IF NOT EXISTS `stagerecord` (
  `Win` int(11) NOT NULL DEFAULT 0,
  `Lose` int(11) NOT NULL DEFAULT 0,
  `UserId` varchar(16) NOT NULL DEFAULT '',
  `MapId` tinyint(4) NOT NULL DEFAULT 0,
  UNIQUE KEY `key_User_Mobile` (`UserId`,`MapId`) USING BTREE,
  CONSTRAINT `fk_stagerecord_user` FOREIGN KEY (`UserId`) REFERENCES `user` (`UserId`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

-- Dumping structure for procedure gbth.UpdateGameRankingAndGuilds
DELIMITER //
CREATE PROCEDURE `UpdateGameRankingAndGuilds`()
BEGIN
    /* Declarar tudo junto no comeÃƒÂ§o */
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_user_id VARCHAR(16);
    DECLARE v_total_score INT;
    DECLARE v_guild VARCHAR(8);
    DECLARE v_member_count INT;
    DECLARE v_guild_total_score INT;
    DECLARE rank INT DEFAULT 0;

    /* Cursores sÃƒÂ³ aqui */
    DECLARE rank_cursor CURSOR FOR
        SELECT UserId, TotalScore FROM game ORDER BY TotalScore DESC;
    DECLARE guild_cursor CURSOR FOR
        SELECT Guild FROM game WHERE Guild IS NOT NULL And Guild <> '' GROUP BY Guild ORDER BY Guild;

    /* Handler ÃƒÂºnico */
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    /* Ranking global */
    SET rank = 0;
    SET done = FALSE;
    OPEN rank_cursor;
    rank_loop: LOOP
        FETCH rank_cursor INTO v_user_id, v_total_score;
        IF done THEN
            LEAVE rank_loop;
        END IF;
        SET rank = rank + 1;
        UPDATE game SET TotalRank = rank WHERE UserId = v_user_id;
    END LOOP;
    CLOSE rank_cursor;

    /* Ranking por guilda */
    SET done = FALSE;
    OPEN guild_cursor;
    guild_loop: LOOP
        FETCH guild_cursor INTO v_guild;
        IF done THEN
            LEAVE guild_loop;
        END IF;

        /* Conta membros e soma pontos da guilda */
        SELECT COUNT(*), SUM(TotalScore) INTO v_member_count, v_guild_total_score FROM game WHERE Guild = v_guild;
        UPDATE game SET MemberGuildCount = v_member_count WHERE Guild = v_guild;

        /* Atualiza ranking dentro da guilda via variÃƒÂ¡vel de sessÃƒÂ£o */
        SET @guild_rank := 0;
        UPDATE game g
            JOIN (
                SELECT UserId, (@guild_rank := @guild_rank + 1) AS guild_rank
                FROM game
                WHERE Guild = v_guild
                ORDER BY TotalScore DESC
            ) ranked
            ON g.UserId = ranked.UserId
            SET g.GuildRank = ranked.guild_rank
            WHERE g.Guild = v_guild;
    END LOOP;
    CLOSE guild_cursor;
    SET done = FALSE;

    /* Grades */
    UPDATE game SET TotalGrade = 19 WHERE TotalScore >= 0 AND TotalScore < 1099 AND TotalGrade != 19 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 18 WHERE TotalScore >= 1100 AND TotalScore < 1199 AND TotalGrade != 18 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 17 WHERE TotalScore >= 1200 AND TotalScore < 1499 AND TotalGrade != 17 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 16 WHERE TotalScore >= 1500 AND TotalScore < 1799 AND TotalGrade != 16 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 15 WHERE TotalScore >= 1800 AND TotalScore < 2299 AND TotalGrade != 15 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 14 WHERE TotalScore >= 2300 AND TotalScore < 2799 AND TotalGrade != 14 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 13 WHERE TotalScore >= 2800 AND TotalScore < 3499 AND TotalGrade != 13 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 12 WHERE TotalScore >= 3500 AND TotalScore <= 4199 AND TotalGrade != 12 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 11 WHERE TotalScore >= 4200 AND TotalScore <= 5099 AND TotalGrade != 11 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 10 WHERE TotalScore >= 5100 AND TotalScore <= 5999 AND TotalGrade != 10 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 9 WHERE TotalScore >= 6000 AND TotalScore <= 6899 AND TotalGrade != 9 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 8 WHERE TotalScore >= 6900 AND TotalScore <= 8172 AND TotalGrade != 8 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 7 WHERE TotalScore >= 8173 AND TotalScore <= 9939 AND TotalGrade != 7 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 6 WHERE TotalScore >= 9940 AND TotalScore <= 13076 AND TotalGrade != 6 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 5 WHERE TotalScore >= 13077 AND TotalScore <= 16024 AND TotalGrade != 5 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 4 WHERE TotalScore >= 16025 AND TotalScore <= 21984 AND TotalGrade != 4 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 3 WHERE TotalScore >= 21985 AND TotalScore <= 27280 AND TotalGrade != 3 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 2 WHERE TotalScore >= 27281 AND TotalScore <= 35334 AND TotalGrade != 2 AND NoRankUpdate = 0;
    UPDATE game SET TotalGrade = 1 WHERE TotalScore >= 35335 AND TotalScore <= 49339 AND TotalGrade != 1 AND NoRankUpdate = 0;
END//
DELIMITER ;

-- Dumping structure for event gbth.UpdateRanking
DELIMITER //
CREATE EVENT `UpdateRanking` ON SCHEDULE EVERY 1 DAY STARTS '2026-02-18 21:59:00' ON COMPLETION NOT PRESERVE ENABLE DO BEGIN
CALL `UpdateGameRankingAndGuilds`();
END//
DELIMITER ;

-- Dumping structure for table gbth.user
CREATE TABLE IF NOT EXISTS `user` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `UserId` varchar(16) NOT NULL DEFAULT '',
  `Gender` tinyint(1) NOT NULL DEFAULT 0,
  `Password` varchar(16) NOT NULL DEFAULT '',
  `Status` varchar(10) NOT NULL DEFAULT '',
  `MuteTime` timestamp NULL DEFAULT '2000-01-01 08:00:00',
  `RestrictTime` datetime DEFAULT '2000-01-01 00:00:00',
  `Authority` int(11) NOT NULL DEFAULT 0,
  `Authority2` int(11) NOT NULL DEFAULT 0,
  `AuthorityBackup` int(11) DEFAULT 0,
  `E_Mail` varchar(50) NOT NULL DEFAULT '',
  `Country` int(11) NOT NULL DEFAULT 0,
  `User_Level` int(11) NOT NULL DEFAULT 0,
  `Dia` int(11) DEFAULT 0,
  `Mes` int(11) DEFAULT 0,
  `Ano` int(11) DEFAULT 0,
  `Created` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `user_UNIQUE` (`UserId`),
  KEY `Id` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=436 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Data exporting was unselected.

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
