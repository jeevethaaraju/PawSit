[petsit (1).sql](https://github.com/user-attachments/files/21851437/petsit.1.sql)
-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 19, 2025 at 10:18 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `petsit`
--

-- --------------------------------------------------------

--
-- Table structure for table `booking`
--

CREATE TABLE `booking` (
  `booking_id` int(11) NOT NULL,
  `pet_id` int(100) NOT NULL,
  `service_id` int(100) NOT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `FromDate` date DEFAULT NULL,
  `ToDate` date DEFAULT NULL,
  `FromTime` time DEFAULT NULL,
  `ToTime` time DEFAULT NULL,
  `Pets` text DEFAULT NULL,
  `PetTypes` varchar(255) NOT NULL,
  `Price` varchar(50) DEFAULT NULL,
  `TotalPrice` varchar(50) DEFAULT NULL,
  `PayMethod` varchar(50) DEFAULT NULL,
  `booking_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `petOwner_ID` int(100) NOT NULL,
  `petSitter_ID` int(100) NOT NULL,
  `approval` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `booking`
--

INSERT INTO `booking` (`booking_id`, `pet_id`, `service_id`, `service_name`, `FromDate`, `ToDate`, `FromTime`, `ToTime`, `Pets`, `PetTypes`, `Price`, `TotalPrice`, `PayMethod`, `booking_date`, `petOwner_ID`, `petSitter_ID`, `approval`) VALUES
(184, 68, 93, 'Tamilcare', '2025-06-19', '2025-06-20', '13:30:00', '20:30:00', 'Snowy', 'Dog', '60', 'RM77.50', 'FPX', '2025-06-23 17:35:02', 56, 48, 'completed'),
(186, 65, 93, 'Tamilcare', '2025-06-24', '2025-06-24', '13:30:00', '16:30:00', 'Ricky', 'Dog', '60', 'RM7.50', 'FPX', '2025-06-24 00:11:11', 56, 48, 'approved'),
(187, 60, 100, 'Fur Home', '2025-07-01', '2025-07-02', '13:30:00', '21:18:00', 'sugi', 'Dog', '90', 'RM119.25', 'FPX', '2025-06-24 00:18:34', 68, 69, 'pending'),
(188, 60, 94, 'Aina Meow', '2025-07-03', '2025-07-04', '20:19:00', '21:19:00', 'sugi', 'Dog', '80', 'RM83.33', 'Wallet', '2025-06-24 00:19:34', 68, 64, 'rejected'),
(189, 65, 95, 'Ainnnn Home', '2025-06-24', '2025-06-24', '15:30:00', '18:30:00', 'Ricky', 'Dog', '60', 'RM7.50', 'Visa', '2025-06-24 00:22:20', 56, 65, 'completed'),
(190, 65, 95, 'Ainnnn Home', '2025-06-25', '2025-06-25', '16:22:00', '18:22:00', 'Ricky', 'Dog', '60', 'RM5.00', 'Visa', '2025-06-24 00:22:47', 56, 65, 'cancelled'),
(191, 68, 95, 'Ainnnn Home', '2025-06-26', '2025-06-26', '16:30:00', '21:30:00', 'Snowy', 'Dog', '60', 'RM12.50', 'Visa', '2025-06-24 00:23:16', 56, 65, 'approved'),
(192, 65, 93, 'Tamilcare', '2025-06-25', '2025-06-26', '14:30:00', '16:30:00', 'Ricky', 'Dog', '60', 'RM65.00', 'Wallet', '2025-06-24 02:30:58', 56, 48, 'approved'),
(198, 58, 101, 'Umesh', '2025-08-22', '2025-08-24', '03:00:00', '06:30:00', 'jujj', 'Dog', '25', 'RM53.65', 'Visa', '2025-08-17 16:07:05', 84, 70, 'pending');

-- --------------------------------------------------------

--
-- Table structure for table `complaints`
--

CREATE TABLE `complaints` (
  `complaint_id` int(100) NOT NULL,
  `booking_id` int(100) NOT NULL,
  `petOwner_ID` int(100) NOT NULL,
  `petSitter_ID` int(100) NOT NULL,
  `description` varchar(100) NOT NULL,
  `status` varchar(100) NOT NULL,
  `FromDate` date NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `proof_image` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `complaints`
--

INSERT INTO `complaints` (`complaint_id`, `booking_id`, `petOwner_ID`, `petSitter_ID`, `description`, `status`, `FromDate`, `created_at`, `proof_image`) VALUES
(141, 189, 56, 65, 'abused my pet', 'reviewed', '2025-06-24', '2025-06-24 02:38:10', 'uploads/6859f053ebcb2.jpg'),
(142, 190, 56, 65, 'abused again', 'reviewed', '2025-06-25', '2025-06-24 02:38:06', 'uploads/6859f246b3194.jpg'),
(143, 191, 56, 65, 'injured', 'reviewed', '2025-06-26', '2025-06-24 00:37:03', 'uploads/6859f2cedcd05.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `complaint_id` int(11) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notification_id`, `user_id`, `complaint_id`, `message`, `is_read`, `created_at`) VALUES
(156, 65, 143, 'Your complaint #143 has been reviewed by admin', 0, '2025-06-24 00:37:03'),
(157, 65, 142, 'Your complaint #142 has been reviewed by admin', 0, '2025-06-24 02:38:06'),
(158, 65, 141, 'Your complaint #141 has been reviewed by admin', 0, '2025-06-24 02:38:10');

-- --------------------------------------------------------

--
-- Table structure for table `pet`
--

CREATE TABLE `pet` (
  `id` int(11) NOT NULL,
  `petName` varchar(100) NOT NULL,
  `petType` varchar(100) NOT NULL,
  `petBreed` varchar(100) NOT NULL,
  `petSize` varchar(100) NOT NULL,
  `petGender` varchar(100) NOT NULL,
  `petBirthDate` varchar(100) NOT NULL,
  `petImage` varchar(255) NOT NULL,
  `petOwner_ID` int(100) NOT NULL,
  `status` varchar(100) NOT NULL DEFAULT 'active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pet`
--

INSERT INTO `pet` (`id`, `petName`, `petType`, `petBreed`, `petSize`, `petGender`, `petBirthDate`, `petImage`, `petOwner_ID`, `status`) VALUES
(58, 'jujj', 'Dog', 'Affenpinscher', 'Small (1-5kg)', 'Male', '2025-06-06', 'pet_6857e906a2e75.jpg', 84, 'active'),
(59, 'jokoo', 'Dog', 'affenpinscher', 'Small (1-5kg)', 'Male', '2025-06-06', 'pet_6857ecd39076a.jpg', 56, 'inactive'),
(60, 'sugi', 'Dog', 'affenpinscher', 'Small (1-5kg)', 'Male', '2025-06-20', '6859eeb643703.jpg', 68, 'active'),
(61, 'ok', 'Dog', 'Affenpinscher', 'Small (1-5kg)', 'Male', '2025-06-11', '', 56, 'inactive'),
(62, 'jo', 'Dog', 'affenpinscher', 'Small (1-5kg)', 'Male', '2025-06-06', '6858e5196ba97.jpg', 56, 'inactive'),
(63, 'j', 'Dog', 'Affenpinscher', 'Small (1-5kg)', 'Male', '2025-06-13', '', 56, 'inactive'),
(64, 'try', 'Dog', 'Affenpinscher', 'Small (1-5kg)', 'Male', '2025-06-12', '', 56, 'inactive'),
(65, 'Ricky', 'Dog', 'retriever', 'Small (1-5kg)', 'Male', '2025-06-04', 'pet_68590b1b924cf.jpg', 56, 'active'),
(66, 'kichi', 'Dog', 'affenpinscher', 'Extra Large (20kg+)', 'Male', '2002-09-18', '685951b66556d.jpg', 91, 'active'),
(67, 'max', 'Dog', 'affenpinscher', 'Extra Large (20kg+)', 'Male', '2025-06-23', '685951ce503dc.jpg', 91, 'active'),
(68, 'Snowy', 'Dog', 'Akita', 'Small (1-5kg)', 'Male', '2025-06-02', 'pet_685982af989cf.jpg', 56, 'active');

-- --------------------------------------------------------

--
-- Table structure for table `petadmin`
--

CREATE TABLE `petadmin` (
  `id` int(100) NOT NULL,
  `FirstName` varchar(100) NOT NULL,
  `Email` varchar(100) NOT NULL,
  `Password` varchar(100) NOT NULL,
  `ProfileImage` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `petadmin`
--

INSERT INTO `petadmin` (`id`, `FirstName`, `Email`, `Password`, `ProfileImage`) VALUES
(1, 'Admin', 'admin@gmail.com', '$2y$10$fR840jngYqp2nfn1GUvmsugagcQxTUMuxNwaNzTzRT4v3DI58PSTi', 'uploads/6809fe5d4cc30_avatar-profile-icon-flat-style-female-user-profile-vector-illustration-isolate');

-- --------------------------------------------------------

--
-- Table structure for table `petowner`
--

CREATE TABLE `petowner` (
  `id` int(11) NOT NULL,
  `FirstName` varchar(50) DEFAULT NULL,
  `LastName` varchar(50) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `Password` varchar(100) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `Phone_Number` varchar(11) NOT NULL,
  `Location` varchar(250) NOT NULL,
  `Gender` varchar(10) DEFAULT NULL,
  `Role` varchar(20) DEFAULT NULL,
  `WalletBalance` decimal(10,2) DEFAULT 0.00,
  `ProfileImage` varchar(255) DEFAULT '',
  `lat` decimal(10,8) DEFAULT NULL,
  `lng` decimal(11,8) DEFAULT NULL,
  `reset_code` varchar(6) DEFAULT NULL,
  `reset_code_expires` datetime DEFAULT NULL,
  `reset_token` varchar(64) DEFAULT NULL,
  `reset_token_expires` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `petowner`
--

INSERT INTO `petowner` (`id`, `FirstName`, `LastName`, `Email`, `Password`, `birth_date`, `Phone_Number`, `Location`, `Gender`, `Role`, `WalletBalance`, `ProfileImage`, `lat`, `lng`, `reset_code`, `reset_code_expires`, `reset_token`, `reset_token_expires`) VALUES
(11, 'testi', 'test', 'test@gmail.com', '$2y$10$0HGfEAGkIUtEslOuiqxRRO7.Jxg6xByjrdivtzCrU9Pa7xQ61JK3O', '2025-05-01', '0134', '', 'Female', 'petowner', 0.00, 'uploads/a342a5261e23a03fdfa88be4c793e27e.jpg', NULL, NULL, NULL, NULL, NULL, NULL),
(56, 'jeevethaaa', 'raju', 'jeevethaa@gmail.com', '$2y$10$aPCkFKE2UK/AVX64poMql.zrh5KyZKANuN/heufs0c6wsMHynHzZ2', '2025-04-26', '0134351022', 'Permas Jaya, 81750 Masai, Johor, Malaysia', 'Female', 'petowner', 148.56, 'uploads/IMG_20250429_081023_901.jpg', 1.50228900, 103.82230300, NULL, NULL, NULL, NULL),
(67, 'testla', 'testla', 'testla@gmail.com', '$2y$10$h1atM4VZCSU9ygJpiPHZ.eKSNyTQjEROQij2M6yU5RK7ThbOiOWY6', '2025-05-10', '0134351022', '21, Jln Permas 10, Bandar Baru Permas Jaya, 81750 Johor Bahru, Johor Darul Ta\'zim, Malaysia', 'Female', 'petowner', 257.08, 'uploads/68173dd70e8d4_a342a5261e23a03fdfa88be4c793e27e.jpg', 1.49711340, 103.81518960, NULL, NULL, NULL, NULL),
(68, 'sugi', 'sugi', 'sugi@gmail.com', '$2y$10$6zZVzLmSEAk2lhOLr7fopOqDlk09p7IkxghC8NYQFWOK8AxWeRf/u', '2025-05-01', '12345', 'Bandar Seri Alam, 81750 Masai, Johor, Malaysia', 'Female', 'petowner', 430.42, 'uploads/681757b28fcb0_a342a5261e23a03fdfa88be4c793e27e.jpg', 1.50572500, 103.87449900, NULL, NULL, NULL, NULL),
(69, 'qistina', 'q', 'q@gmail.com', '$2y$10$l2Tumz47hlYJQm.as3m.AuCT8uh.vLu36bhUHKBOx5NCnyufKbfFC', '2025-05-01', '012345672', '25, Jalan Beringin, Taman Rinting, 81750 Masai, Johor Darul Ta\'zim, Malaysia', 'Female', 'petowner', 92.50, 'uploads/68184a771c23f_a342a5261e23a03fdfa88be4c793e27e.jpg', 1.48127300, 103.86320150, NULL, NULL, NULL, NULL),
(70, 'bU', 'bu', 'bu@gmail.com', '$2y$10$FYg3.5JVtBo7.2zBrWU4xuaFgtdkb9EjG8dQFR9BhJ71aZlRlOHn.', '2025-05-23', '8020929', 'Pangsapuri Senibong Indah, Jalan Tanjung Senibong 1/2, Tanjung Senibong, 81750 Masai, Johor Darul Ta\'zim, Malaysia', 'Female', 'petowner', 0.00, 'uploads/6830815f427a4_Screenshot_2025-05-02-04-50-30-05_b1686e0ed143d060d187661aba5a06a3.jpg', 1.48616090, 103.85546350, NULL, NULL, NULL, NULL),
(74, 'vinoshni', 'balachandran', 'vinoshni0202@gmail.com', '$2y$10$XQ9jLNYqjXWSkPk0zUO84.MphZlC6Xti0B5kw0rOhLHpC32pvP7Ra', '2025-06-09', '01478900011', 'Senibong Cove, 81750 Masai, Johor, Malaysia', 'Female', 'petowner', 0.00, 'uploads/6845e26224a3b_Screenshot_2025-06-09-03-18-49-45_1c337646f29875672b5a61192b9010f9.jpg', 1.48795170, 103.83104550, NULL, NULL, NULL, NULL),
(75, 'tamilselvi', 't', 'ts746650@gmail.com', '$2y$10$J5kHmrGwW3ZDkzQHAgRX6el46yeSjB0/awJ4z0kWalYq13x5Svt2e', '2025-06-15', '0198388921', 'Permas Jaya, 81750 Masai, Johor, Malaysia', 'Female', 'petowner', 0.00, 'uploads/684d9ee35bf35_Screenshot_2025-06-11-23-38-45-87_9c2f7b596ba6a500e3c00bae5adf061f.jpg', 1.50228900, 103.82230300, '578674', '2025-06-15 00:25:17', NULL, NULL),
(80, 'r', 'i', 'jeev@gmail.com', '$2y$10$j.ekBTQDSDwJV8Aw/L4LuudwbeGnDPv398xU66pGgMnnKqmeNGecG', '2025-06-06', '0743221786', 'Yemen', 'Female', 'petowner', 0.00, '', 15.55272700, 48.51638800, NULL, NULL, NULL, NULL),
(81, 'hh', 'h', 'h@gmail.com', '$2y$10$P1igM6DuRRuBbqMxxVYnX.vdxw09aR6lABbSXqjsDPeMQzvKGaRAi', '2025-06-13', '0986655544', 'T1 Parking Garage, 6301 Silver Dart Dr, Mississauga, ON L5P, Canada', 'Female', 'petowner', 0.00, '', 43.68378080, -79.61285940, NULL, NULL, NULL, NULL),
(82, 'v', 'v', 'v@gmail.com', '$2y$10$ZjVhEmDLRhH0PO3TEU7Ru.hVs9yuNQ/vlmnIg/BLCSe3LycJEYYaC', '2025-06-04', '0145678951', 'Yemen', 'Male', 'petowner', 0.00, '', 15.55272700, 48.51638800, '852886', '2025-06-24 01:47:55', '5867808bbc8942d39b5e27a33fd2dc622ed8f8d72d73e3ac96d2456392299188', '2025-06-24 01:47:55'),
(84, 'jeevethaa', 'raju', 'jeevethaa18@gmail.com', '$2y$10$sh7hZBN5eq128VQEVDxNDeC1kUZR9.rhJ3MAFFM9m8wd21IguAImi', '2025-06-18', '0134351022', '29, Jalan Beringin 8, Taman Rinting, 81750 Masai, Johor Darul Ta\'zim, Malaysia', 'Female', 'petowner', 0.00, 'uploads/6857dfa42e893_IMG_20250429_081023_901.jpg', 1.48165670, 103.85964890, '340142', '2025-08-15 00:33:02', 'f1893c65bd067408eaed78cc4bb711937e77313aca0462b0df5d36ce3b68b01f', '2025-08-15 00:33:02'),
(85, 'f', 'g', 'f@gmail.com', '$2y$10$rcL.dcrM34gxv178GuRDleELOeDQ8ec9zPTEPYpfrhNSn.x8NgFv.', '2025-06-11', '0134351022', 'Batu Pahat, Johor, Malaysia', 'Female', 'petowner', 0.00, '', 1.85450650, 102.94643720, NULL, NULL, NULL, NULL),
(89, 'Uhmmis', 'Naidu', 'uhmmis@gmail.com', '$2y$10$qgAu44Cw0vMzCdBihA.yFepSPYBES0xJzGhTkemuSosjXVFC42oJa', '2025-06-11', '0167789531', 'Masai, Johor, Malaysia', 'Male', 'petowner', 0.00, 'uploads/685907144b2a0_images.jpeg', 1.49163260, 103.87986790, NULL, NULL, NULL, NULL),
(91, 'Thivya', 'Laxhimi', 'thivyalaxhimi06@gmail.com', '$2y$10$apm3QlVUr7DzIK9.RgYDhOHRY4pM9ohOywJ3m/NNcxSn.9cbAhSxy', '2002-11-06', '0143793270', 'Jalan Angsana 10, Taman Rinting, 81750 Masai, Johor Darul Ta\'zim, Malaysia', 'Female', 'petowner', 217.13, 'uploads/Screenshot_2025-06-09-03-18-41-58_1c337646f29875672b5a61192b9010f9.jpg', 1.48505200, 103.87375310, '022611', '2025-06-23 22:09:05', 'aee9bf6cdad8f6294befd5df0fbc531f3d7a0c1162c77886f608c64bc2a0dd66', '2025-06-23 22:09:05');

-- --------------------------------------------------------

--
-- Table structure for table `petsitter`
--

CREATE TABLE `petsitter` (
  `id` int(11) NOT NULL,
  `FirstName` varchar(50) DEFAULT NULL,
  `LastName` varchar(50) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `Password` varchar(255) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `Phone_Number` varchar(11) NOT NULL,
  `Location` varchar(250) NOT NULL,
  `Gender` varchar(10) DEFAULT NULL,
  `Role` varchar(20) DEFAULT NULL,
  `ProfileImage` varchar(255) DEFAULT '',
  `lat` decimal(10,8) DEFAULT NULL,
  `lng` decimal(11,8) DEFAULT NULL,
  `reset_code` varchar(6) DEFAULT NULL,
  `reset_code_expires` datetime DEFAULT NULL,
  `reset_token` varchar(64) DEFAULT NULL,
  `reset_token_expires` datetime DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `bank` varchar(100) DEFAULT NULL,
  `account_number` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `petsitter`
--

INSERT INTO `petsitter` (`id`, `FirstName`, `LastName`, `Email`, `Password`, `birth_date`, `Phone_Number`, `Location`, `Gender`, `Role`, `ProfileImage`, `lat`, `lng`, `reset_code`, `reset_code_expires`, `reset_token`, `reset_token_expires`, `status`, `bank`, `account_number`) VALUES
(48, 'Tamil', 'Selvi', 'tamil@gmail.com', '$2y$10$g4dWBHbUphp0/hONbSB13.iDHsV2ySGrABzM17RbjHcgISyXtB.dm', '2025-06-25', '0167178513', 'Jalan Tasek, Bandar Baru Seri Alam, 81750 Masai, Johor Darul Ta\'zim, Malaysia', 'Female', 'petsitter', 'uploads/Trisha_Krishnan_pet_1735114980162_1735115028907.jpg', 1.50036900, 103.88115400, NULL, NULL, NULL, NULL, 'active', NULL, NULL),
(54, 'kiki', 'b', 'kiki@gmail.com', '$2y$10$Co4QLob9FL4zFJj/XzwAGufcMt6CVZrsG.uC10ah5./KuCfT/Brq2', '2025-06-06', '0134546778', '2000 Airport Rd NE, Calgary, AB T2E 6W5, Canada', 'Male', 'petsitter', '', 51.12165260, -114.00805250, NULL, NULL, NULL, NULL, 'active', NULL, NULL),
(59, 't', 't', 'y@gmail.com', '$2y$10$gIoOztSyBGdRkCir6OMyhuzdyb6P2vAbDMoXQeIIy0B8CkYLj3ysW', '2025-06-12', '0134351022', 'Batu Pahat, Johor, Malaysia', 'Female', 'petsitter', '', 1.85450650, 102.94643720, NULL, NULL, NULL, NULL, 'inactive', NULL, NULL),
(64, 'Aina', 'Nasuha', 'aina@gmail.com', '$2y$10$jtqaEYxfIi1I71RkH/YYyOjyk9bC2w.6fkP65kWr12han2ON3aAai', '2002-06-08', '0194350475', 'Octville Condominium, Bandar Baru Seri Alam, 81750 Johor Bahru, Johor, Malaysia', 'Female', 'petsitter', 'uploads/c5661bfd-630f-4077-a79c-454961019bf7.jpg', 1.50591880, 103.86841290, NULL, NULL, NULL, NULL, 'active', 'Maybank', '151278856959'),
(65, 'Ain', 'Nabila', 'ain@gmail.com', '$2y$10$Q2/MUO9quUvjw2nix8LryOVqAgt82u5/EwdZLEC/5deBnyM2vTqEG', '2025-06-01', '0102721652', 'Bandar Seri Alam, 81750 Masai, Johor, Malaysia', 'Female', 'petsitter', 'uploads/assurance_chat-340x190.jpg', 1.50572500, 103.87449900, NULL, NULL, NULL, NULL, 'active', 'Bank Islam', '145236569880'),
(66, 'Madhavan', 'Kumar', 'madhavan@gmail.com', '$2y$10$IINDuvSoTyvf0wKJApW9Fu9Q3dA1vLBpEIFTal.SLNpSvNrG6N8Uy', '2025-06-01', '0143793270', 'Kluang, Johor, Malaysia', 'Male', 'petsitter', 'uploads/68597aead8d8c_3RMadhavan-with-his-wife-and-pet-dogs.jpg', 2.03387290, 103.31985870, NULL, NULL, NULL, NULL, 'active', 'AmBank', '123456789112'),
(67, 'Yuvaraj', 'Krishnan', 'yuva@gmail.com', '$2y$10$ni2Ggzvx/ZvKwcJuM6xEMuAtmyoFLDWz6CxLX6cNW/DflPIAms4UG', '2025-06-01', '0145683487', 'Skudai, Johor, Malaysia', 'Male', 'petsitter', 'uploads/68597f276c25e_IMG_20250624_002125_814.jpg', 1.53436160, 103.65942670, NULL, NULL, NULL, NULL, 'active', 'Hong Leong Bank', '258036901470'),
(68, 'Chan', 'Yen', 'chan@gmail.com', '$2y$10$r52dDoxRLkwMJCgu7MotuuosU8ubAKwFBW/80De1NKWZUSn2cmLTG', '2025-06-13', '0156789876', 'Senai, Johor, Malaysia', 'Female', 'petsitter', 'uploads/685980df95752_lawrence-wong-digi-cover.jpg', 1.60203100, 103.64437910, NULL, NULL, NULL, NULL, 'active', 'Bank Rakyat', '125803697580'),
(69, 'Goh', 'Chan', 'goh@gmail.com', '$2y$10$LOiz4BKBKWkXKProlFw/S.1FkVlnDBogDqrHc3ieR8Mo5PArQTIXm', '2025-06-06', '0178967652', '521, Jln Permas 10, Bandar Baru Permas Jaya, 81750 Johor Bahru, Johor Darul Ta\'zim, Malaysia', 'Male', 'petsitter', 'uploads/6859866922177_smiling-asian-man-playing-lovely-600nw-1960043986.jpg', 1.49711340, 103.81518960, NULL, NULL, NULL, NULL, 'active', 'Affin Bank', '258065408521'),
(70, 'uhmmis', 'naidu', 'uhmmis@gmail.com', '$2y$10$k2efVgXhxDCZG.egZSP5ReXNdge0yl5d0lpMonNc0EzKhooRFIKwa', '2025-08-03', '0189372688', 'Taman Rinting, 81750 Masai, Johor Darul Ta\'zim, Malaysia', 'Male', 'petsitter', '', 1.48610000, 103.86314100, NULL, NULL, NULL, NULL, 'active', 'AmBank', '6184884533');

-- --------------------------------------------------------

--
-- Table structure for table `services`
--

CREATE TABLE `services` (
  `id` int(100) NOT NULL,
  `service_name` varchar(100) NOT NULL,
  `summ` varchar(300) NOT NULL,
  `numofpets` int(100) NOT NULL,
  `accept_pet` varchar(100) NOT NULL,
  `accept_petsize` varchar(100) NOT NULL,
  `unsupervised` varchar(100) NOT NULL,
  `potty` varchar(100) NOT NULL,
  `walks` varchar(100) NOT NULL,
  `home` varchar(100) NOT NULL,
  `transport` varchar(100) NOT NULL,
  `price` int(100) NOT NULL,
  `service` varchar(100) NOT NULL,
  `picture` varchar(100) NOT NULL,
  `petsitter_id` int(100) NOT NULL,
  `picture1` varchar(255) DEFAULT NULL,
  `picture2` varchar(255) DEFAULT NULL,
  `picture3` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `services`
--

INSERT INTO `services` (`id`, `service_name`, `summ`, `numofpets`, `accept_pet`, `accept_petsize`, `unsupervised`, `potty`, `walks`, `home`, `transport`, `price`, `service`, `picture`, `petsitter_id`, `picture1`, `picture2`, `picture3`) VALUES
(93, 'Tamilcare', 'Loving and responsible pet sitter ready to care for your furry friend like my own! I provide a safe, clean, and comfortable environment where your pet can feel relaxed and happy. Whether it\'s playtime, feeding, or walks, I’m here to ensure your pet gets the love and attention they deserve', 2, 'dog', 'small', 'Free roam of the house', '1-2', '1', 'Apartment/Condo', 'Yes', 60, 'walking,feeding,playing', '', 48, 'uploads/68586c6c23056.jpg', 'uploads/68586c6c232cf.jpg', 'uploads/68586c6c233c2.jpg'),
(94, 'Aina Meow', 'Loving and responsible pet sitter ready to care for your furry friend like my own! I provide a safe, clean, and comfortable environment where your pet can feel relaxed and happy. Whether it\'s playtime, feeding, or walks, I’m here to ensure your pet gets the love and attention they deserve', 1, 'cat', 'small(1-5kg)', 'Free roam of the house', '1-2', '1', 'House', 'Yes', 80, 'walking,feeding,playing', '', 64, 'uploads/img_685973cc6f1c8_1000150314.jpg', 'uploads/68596e36cf324.jpg', 'uploads/img_685973c258bc5_1000150313.jpg'),
(95, 'Ainnnn Home', 'Purr-fect care for your purr-fect companion!', 1, 'Cat', 'Small (0-15 lbs)', 'Free roam of the house', '1-2', '2', 'Apartment/Condo', 'Yes', 60, 'Playing', '', 65, 'uploads/6859762f21c75.webp', 'uploads/6859762f21e39.jpg', 'uploads/6859762f220a8.jpg'),
(97, 'MB Home', 'Long walks, happy tails, and safe naps — every time', 2, 'dog', 'small(1-5kg),medium(5-10kg)', 'Free roam of the house', '1-2', '2', 'Apartment/Condo', 'Yes', 70, 'feeding,playing', '', 66, 'uploads/img_68597c1920aa4_1000150326.jpg', 'uploads/img_68597c1921251_1000150328.jpg', 'uploads/img_68597c1921738_1000150324.jpg'),
(98, 'Love Birds', 'Your dog’s happiness and comfort are my top priority. I provide a safe, fun, and caring environment where your fur baby can feel right at home with belly rubs, treats, and plenty of tail wags guaranteed!', 1, 'Dog', 'Small (0-15 lbs)', 'Free roam of the house', '1-2', '2', 'Apartment/Condo', 'Yes', 90, 'Playing', '', 67, 'uploads/68597f9bc0b29.jpg', 'uploads/68597f9bc0d00.jpg', 'uploads/68597f9bc0dfe.jpg'),
(99, 'Pet Care', 'Caring for cats with patience, love, and quiet vibes', 2, 'cat', 'small(1-5kg)', 'Free roam of the house', '1-2', '1', 'Apartment/Condo', 'Yes', 60, 'feeding,playing', '', 68, 'uploads/68598154e4347.jpg', 'uploads/img_6859818d15f3c_1000150336.jpg', 'uploads/img_6859818d16346_1000150335.jpg'),
(100, 'Fur Home', 'Hi! I’m a friendly and responsible dog sitter who truly loves spending time with dogs of all sizes. Whether your pup needs a walk, playtime, or just some cuddles, I’m here to keep them happy, active, and loved while you\'re away', 2, 'Dog', 'Small (0-15 lbs), Medium (16-40 lbs)', 'Free roam of the house', '1-2', '1', 'House', 'Yes', 90, 'Walking, Playing', '', 69, 'uploads/685986dc75fee.jpg', 'uploads/685986dc76182.jpg', 'uploads/685986dc762ba.jpg'),
(101, 'Umesh', 'uuu', 2, 'Dog', 'Small (0-15 lbs), Medium (16-40 lbs)', 'Free roam of the house', '1-2', '0', 'Apartment/Condo', 'Yes', 25, 'Walking', '', 70, 'uploads/68a1fdfb687b3.jpg', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `sitter_complaints`
--

CREATE TABLE `sitter_complaints` (
  `record_id` int(11) NOT NULL,
  `sitter_id` int(11) NOT NULL,
  `complaint_count` int(11) DEFAULT 1,
  `last_updated` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `sitting`
--

CREATE TABLE `sitting` (
  `sitting_id` int(100) NOT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `FromDate` date DEFAULT NULL,
  `ToDate` date DEFAULT NULL,
  `FromTime` time(6) DEFAULT NULL,
  `ToTime` time(6) DEFAULT NULL,
  `Pets` text DEFAULT NULL,
  `PetTypes` varchar(100) NOT NULL,
  `Price` varchar(50) DEFAULT NULL,
  `TotalPrice` varchar(50) DEFAULT NULL,
  `PayMethod` varchar(50) DEFAULT NULL,
  `booking_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `petOwner_ID` int(100) NOT NULL,
  `petSitter_ID` int(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sitting`
--

INSERT INTO `sitting` (`sitting_id`, `service_name`, `FromDate`, `ToDate`, `FromTime`, `ToTime`, `Pets`, `PetTypes`, `Price`, `TotalPrice`, `PayMethod`, `booking_date`, `petOwner_ID`, `petSitter_ID`) VALUES
(19, 'cryy', '2025-06-15', '2025-06-15', '13:45:00.000000', '21:45:00.000000', 'champ, jimmyyyy', '', '80', 'RM53.33', 'Wallet', '2025-05-30 23:45:25', 56, 16),
(20, 'Premium Pet Care', '2025-07-25', '2025-07-28', '16:43:00.000000', '18:43:00.000000', 'ricky, jimmyyyy, champ', '', '75', 'RM693.75', 'Visa', '2025-05-31 02:43:44', 56, 13),
(21, 'bkhoho', '2025-06-01', '2025-06-14', '23:31:00.000000', '23:31:00.000000', 'tamilpet', '', '569', 'RM7397.00', 'FPX', '2025-05-31 09:31:44', 70, 18),
(22, 'ojko Care', '2025-07-12', '2025-07-13', '02:58:00.000000', '02:58:00.000000', 'ricky', '', '75', 'RM75.00', 'Wallet', '2025-06-01 12:58:39', 56, 11),
(23, 'ojko Care', '2025-07-14', '2025-07-15', '02:59:00.000000', '02:59:00.000000', 'ricky, champ', '', '75', 'RM150.00', 'Wallet', '2025-06-01 12:59:11', 56, 11),
(24, 'ojko Care', '2025-07-25', '2025-07-27', '03:02:00.000000', '03:02:00.000000', 'jimmyyyy', '', '75', 'RM150.00', 'Wallet', '2025-06-01 13:02:33', 56, 11),
(25, 'Premium Pet Care', '2025-08-16', '2025-08-17', '03:12:00.000000', '03:12:00.000000', 'jimmyyyy', '', '75', 'RM75.00', 'Visa', '2025-06-01 13:12:09', 56, 13),
(26, 'Premium Pet Care', '2025-06-28', '2025-06-29', '03:14:00.000000', '03:14:00.000000', 'ri', '', '75', 'RM75.00', 'Visa', '2025-06-01 13:14:26', 69, 13),
(27, 'Premium Pet Care', '2025-07-03', '2025-07-04', '03:14:00.000000', '03:14:00.000000', 'ri', '', '75', 'RM75.00', 'Visa', '2025-06-01 13:14:42', 69, 13),
(28, 'BellaCare', '2025-07-13', '2025-07-14', '03:28:00.000000', '03:28:00.000000', 'ri', '', '40', 'RM40.00', 'FPX', '2025-06-01 13:28:18', 69, 15),
(29, 'BellaCare', '2025-07-17', '2025-07-18', '03:28:00.000000', '03:28:00.000000', 'ri', '', '40', 'RM40.00', 'FPX', '2025-06-01 13:28:38', 69, 15),
(30, 'BellaCare', '2025-07-26', '2025-07-27', '03:28:00.000000', '03:28:00.000000', 'ri', '', '40', 'RM40.00', 'FPX', '2025-06-01 13:28:52', 69, 15),
(31, 'Su Pet Care', '2025-07-05', '2025-07-26', '04:09:00.000000', '04:09:00.000000', 'ricky', '', '50', 'RM1050.00', 'FPX', '2025-06-01 14:09:22', 56, 19),
(32, 'Su Pet Care', '2025-07-29', '2025-07-31', '04:09:00.000000', '04:09:00.000000', 'ricky', '', '50', 'RM100.00', 'FPX', '2025-06-01 14:09:45', 56, 19),
(33, 'Su Pet Care', '2025-06-14', '2025-06-15', '04:09:00.000000', '04:09:00.000000', 'ricky', '', '50', 'RM50.00', 'FPX', '2025-06-01 14:10:00', 56, 19),
(34, 'bkhoho', '2025-08-09', '2025-08-10', '04:33:00.000000', '04:33:00.000000', 'ricky', '', '569', 'RM569.00', 'FPX', '2025-06-01 14:33:54', 56, 18),
(35, 'bkhoho', '2025-08-11', '2025-08-12', '04:34:00.000000', '04:34:00.000000', 'jimmyyyy', '', '569', 'RM569.00', 'FPX', '2025-06-01 14:34:11', 56, 18),
(36, 'bkhoho', '2025-07-20', '2025-07-27', '04:36:00.000000', '04:36:00.000000', 'jimmyyyy', '', '569', 'RM3983.00', 'FPX', '2025-06-01 14:36:40', 56, 18),
(37, 'thivya nai care', '2025-08-30', '2025-08-31', '21:58:00.000000', '21:58:00.000000', 'ricky', '', '10', 'RM10.00', 'Visa', '2025-06-03 07:58:21', 56, 21),
(38, 'thivya nai care', '2025-09-01', '2025-09-02', '21:58:00.000000', '21:58:00.000000', 'jimmyyyy', '', '10', 'RM10.00', 'Visa', '2025-06-03 07:58:42', 56, 21),
(39, 'thivya nai care', '2025-06-04', '2025-09-02', '21:58:00.000000', '21:58:00.000000', 'jimmyyyy', '', '10', 'RM900.00', 'Visa', '2025-06-03 07:58:55', 56, 21),
(40, 'th', '2025-07-12', '2025-07-13', '22:37:00.000000', '22:37:00.000000', 'ricky', '', '20', 'RM20.00', 'Visa', '2025-06-03 08:37:06', 56, 22),
(41, 'th', '2025-07-15', '2025-07-17', '22:37:00.000000', '22:37:00.000000', 'ricky', '', '20', 'RM40.00', 'Visa', '2025-06-03 08:39:34', 56, 22),
(42, 'th', '2025-07-19', '2025-07-20', '22:37:00.000000', '22:37:00.000000', 'ricky', '', '20', 'RM20.00', 'Visa', '2025-06-03 08:39:48', 56, 22),
(43, 'th', '2025-06-30', '2025-07-01', '22:48:00.000000', '22:48:00.000000', 'jimmyyyy, ricky, champ', '', '20', 'RM60.00', 'Visa', '2025-06-03 08:48:29', 56, 22),
(44, 'Test Service', '2025-08-24', '2025-08-26', '00:20:00.000000', '04:20:00.000000', 'ricky', '', '30', 'RM65.00', 'Visa', '2025-06-06 10:20:41', 56, 14),
(45, 'vino', '2025-07-12', '2025-07-13', '01:08:00.000000', '01:08:00.000000', 'ricky', '', '50', 'RM50.00', 'Visa', '2025-06-06 11:09:05', 56, 24),
(46, 'vin', '2025-07-25', '2025-07-26', '01:20:00.000000', '06:20:00.000000', 'ricky, champ', '', '90', 'RM217.50', 'Visa', '2025-06-06 11:20:50', 56, 25),
(47, 'vino', '2025-06-30', '2025-07-01', '01:42:00.000000', '04:42:00.000000', 'jimmyyyy, champ', '', '59', 'RM132.75', 'Visa', '2025-06-06 11:42:42', 56, 28),
(48, 'tam', '2025-07-19', '2025-07-20', '02:10:00.000000', '05:10:00.000000', 'ricky', '', '800', 'RM900.00', 'Visa', '2025-06-06 12:10:58', 56, 34),
(49, 't', '2025-07-26', '2025-07-27', '02:15:00.000000', '02:15:00.000000', 'jimmyyyy', '', '1000', 'RM1000.00', 'Visa', '2025-06-06 12:15:57', 56, 35),
(50, 'kk', '2025-07-27', '2025-09-28', '02:31:00.000000', '10:50:00.000000', 'ricky', '', '2', 'RM126.69', 'Visa', '2025-06-06 12:31:22', 56, 38),
(51, 'kk', '2025-07-26', '2025-10-24', '02:32:00.000000', '21:32:00.000000', 'champ', '', '2', 'RM181.58', 'Visa', '2025-06-06 12:32:33', 56, 38),
(52, 'vino', '2025-06-29', '2025-07-27', '05:38:00.000000', '02:38:00.000000', 'jimmyyyy', '', '100', 'RM2787.50', 'Visa', '2025-06-06 12:38:22', 56, 39),
(53, 'vino', '2025-07-30', '2025-08-22', '05:38:00.000000', '02:38:00.000000', 'ricky', '', '100', 'RM2287.50', 'FPX', '2025-06-06 12:39:07', 56, 39),
(54, 'vino', '2025-06-07', '2025-06-07', '02:40:00.000000', '10:41:00.000000', 'jimmyyyy', '', '100', 'RM33.40', 'Wallet', '2025-06-06 12:41:11', 56, 39),
(55, 'vino', '2025-06-08', '2025-06-09', '21:37:00.000000', '23:38:00.000000', 'ricky, champ', '', '48', 'RM104.07', 'Visa', '2025-06-07 07:38:28', 56, 40),
(56, 't', '2025-06-07', '2025-06-07', '01:44:00.000000', '21:44:00.000000', 'jimmyyyy', '', '87', 'RM72.50', 'Visa', '2025-06-07 07:45:01', 56, 41),
(57, 't', '2025-06-08', '2025-06-10', '01:44:00.000000', '21:44:00.000000', 'jimmyyyy', '', '87', 'RM246.50', 'Visa', '2025-06-07 07:45:17', 56, 41),
(58, 't', '2025-06-07', '2025-06-08', '01:52:00.000000', '21:52:00.000000', 'ricky', '', '40', 'RM73.33', 'Visa', '2025-06-07 07:52:21', 56, 42),
(59, 't', '2025-06-07', '2025-06-08', '01:52:00.000000', '21:52:00.000000', 'ricky', '', '40', 'RM73.33', 'Visa', '2025-06-07 07:54:31', 56, 42),
(60, 't', '2025-06-07', '2025-06-08', '01:52:00.000000', '21:52:00.000000', 'ricky', '', '40', 'RM73.33', 'Visa', '2025-06-07 07:55:28', 56, 42),
(61, 't', '2025-06-07', '2025-06-07', '01:08:00.000000', '22:09:00.000000', 'jimmyyyy, ricky', '', '40', 'RM70.06', 'Wallet', '2025-06-07 08:09:16', 56, 43),
(62, 't', '2025-06-07', '2025-06-07', '22:10:00.000000', '23:10:00.000000', 'ricky', '', '40', 'RM1.67', 'Wallet', '2025-06-07 08:10:29', 56, 43),
(63, 't', '2025-06-08', '2025-06-10', '22:10:00.000000', '23:10:00.000000', 'ricky', '', '40', 'RM81.67', 'Wallet', '2025-06-07 08:11:04', 56, 43),
(64, 't', '2025-06-07', '2025-06-07', '22:20:00.000000', '23:20:00.000000', 'ricky', '', '42', 'RM1.75', 'Wallet', '2025-06-07 08:21:07', 56, 44),
(65, 't', '2025-06-07', '2025-06-07', '21:21:00.000000', '23:21:00.000000', 'ricky', '', '42', 'RM3.50', 'Wallet', '2025-06-07 08:22:02', 56, 44),
(66, 't', '2025-06-08', '2025-06-09', '21:21:00.000000', '22:50:00.000000', 'ricky', '', '42', 'RM44.60', 'Wallet', '2025-06-07 08:22:20', 56, 44),
(67, 'ty', '2025-06-07', '2025-06-07', '20:43:00.000000', '22:43:00.000000', 'ricky', '', '24', 'RM2.00', 'Wallet', '2025-06-07 08:43:59', 56, 45),
(68, 'ty', '2025-06-08', '2025-06-09', '22:44:00.000000', '23:44:00.000000', 'champ', '', '24', 'RM25.00', 'Wallet', '2025-06-07 08:44:28', 56, 45),
(69, 'ty', '2025-06-07', '2025-06-07', '22:46:00.000000', '23:46:00.000000', 'ricky', '', '24', 'RM1.00', 'Wallet', '2025-06-07 08:46:22', 56, 45),
(70, 'ty', '2025-06-15', '2025-06-15', '20:48:00.000000', '23:48:00.000000', 'jimmyyyy', '', '24', 'RM3.00', 'Wallet', '2025-06-07 08:48:20', 56, 45),
(71, 'giokukkookoooooo', '2025-06-15', '2025-06-16', '00:06:00.000000', '09:06:00.000000', 'ricky', '', '25', 'RM34.38', 'Visa', '2025-06-14 10:06:26', 56, 11),
(72, 'giokukkookoooooo', '2025-06-15', '2025-06-16', '00:06:00.000000', '09:06:00.000000', 'ricky', '', '25', 'RM34.38', 'Wallet', '2025-06-14 10:06:37', 56, 11),
(73, 'Vino Pet Care', '2025-06-15', '2025-06-16', '03:18:00.000000', '20:18:00.000000', 'ricky', '', '68', 'RM116.17', 'Wallet', '2025-06-14 13:18:15', 56, 46),
(74, 'tamilcare', '2025-06-15', '2025-06-16', '14:36:00.000000', '17:36:00.000000', 'ricky, champ', '', '50', 'RM112.50', 'Wallet', '2025-06-15 00:38:43', 56, 48),
(75, 'giokukkookoooooo', '2025-06-27', '2025-06-28', '05:34:00.000000', '18:34:00.000000', 'jimmyyyy', '', '25', 'RM38.54', 'Wallet', '2025-06-15 04:34:22', 56, 11),
(76, 'tamilcare', '2025-06-25', '2025-06-26', '04:41:00.000000', '04:41:00.000000', 'ricky', '', '50', 'RM50.00', 'FPX', '2025-06-15 11:41:48', 56, 48),
(77, 'tamilcare', '2025-06-24', '2025-06-24', '04:42:00.000000', '05:42:00.000000', 'champ', '', '50', 'RM2.08', 'FPX', '2025-06-15 11:42:49', 56, 48),
(78, 'tamilcare', '2025-06-19', '2025-06-20', '17:50:00.000000', '18:50:00.000000', 'ricky', '', '50', 'RM52.08', 'FPX', '2025-06-16 10:51:06', 56, 48),
(79, 'tamilcare', '2025-06-20', '2025-06-20', '15:51:00.000000', '17:51:00.000000', 'ricky', '', '50', 'RM4.17', 'Visa', '2025-06-16 10:51:44', 56, 48);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`booking_id`),
  ADD KEY `petOwner_ID` (`petOwner_ID`),
  ADD KEY `pet_id` (`pet_id`),
  ADD KEY `petSitter_ID` (`petSitter_ID`),
  ADD KEY `service_id` (`service_id`);

--
-- Indexes for table `complaints`
--
ALTER TABLE `complaints`
  ADD PRIMARY KEY (`complaint_id`),
  ADD KEY `booking_id` (`booking_id`),
  ADD KEY `petOwner_ID` (`petOwner_ID`),
  ADD KEY `petSitter_ID` (`petSitter_ID`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `complaint_id` (`complaint_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `pet`
--
ALTER TABLE `pet`
  ADD PRIMARY KEY (`id`),
  ADD KEY `petOwner_ID` (`petOwner_ID`);

--
-- Indexes for table `petadmin`
--
ALTER TABLE `petadmin`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `petowner`
--
ALTER TABLE `petowner`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `Email` (`Email`);

--
-- Indexes for table `petsitter`
--
ALTER TABLE `petsitter`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `Email` (`Email`);

--
-- Indexes for table `services`
--
ALTER TABLE `services`
  ADD PRIMARY KEY (`id`),
  ADD KEY `petsitter_id` (`petsitter_id`);

--
-- Indexes for table `sitter_complaints`
--
ALTER TABLE `sitter_complaints`
  ADD PRIMARY KEY (`record_id`),
  ADD KEY `sitter_id` (`sitter_id`);

--
-- Indexes for table `sitting`
--
ALTER TABLE `sitting`
  ADD PRIMARY KEY (`sitting_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `booking`
--
ALTER TABLE `booking`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=199;

--
-- AUTO_INCREMENT for table `complaints`
--
ALTER TABLE `complaints`
  MODIFY `complaint_id` int(100) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=144;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=159;

--
-- AUTO_INCREMENT for table `pet`
--
ALTER TABLE `pet`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=69;

--
-- AUTO_INCREMENT for table `petadmin`
--
ALTER TABLE `petadmin`
  MODIFY `id` int(100) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `petowner`
--
ALTER TABLE `petowner`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=94;

--
-- AUTO_INCREMENT for table `petsitter`
--
ALTER TABLE `petsitter`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=71;

--
-- AUTO_INCREMENT for table `services`
--
ALTER TABLE `services`
  MODIFY `id` int(100) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=102;

--
-- AUTO_INCREMENT for table `sitter_complaints`
--
ALTER TABLE `sitter_complaints`
  MODIFY `record_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `sitting`
--
ALTER TABLE `sitting`
  MODIFY `sitting_id` int(100) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=80;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `booking`
--
ALTER TABLE `booking`
  ADD CONSTRAINT `booking_ibfk_3` FOREIGN KEY (`petOwner_ID`) REFERENCES `petowner` (`id`),
  ADD CONSTRAINT `booking_ibfk_4` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  ADD CONSTRAINT `booking_ibfk_5` FOREIGN KEY (`petSitter_ID`) REFERENCES `petsitter` (`id`),
  ADD CONSTRAINT `booking_ibfk_6` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`);

--
-- Constraints for table `complaints`
--
ALTER TABLE `complaints`
  ADD CONSTRAINT `complaints_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
  ADD CONSTRAINT `complaints_ibfk_3` FOREIGN KEY (`petOwner_ID`) REFERENCES `booking` (`petOwner_ID`),
  ADD CONSTRAINT `complaints_ibfk_4` FOREIGN KEY (`petSitter_ID`) REFERENCES `booking` (`petSitter_ID`);

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`complaint_id`) REFERENCES `complaints` (`complaint_id`),
  ADD CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `complaints` (`petSitter_ID`);

--
-- Constraints for table `pet`
--
ALTER TABLE `pet`
  ADD CONSTRAINT `pet_ibfk_1` FOREIGN KEY (`petOwner_ID`) REFERENCES `petowner` (`id`);

--
-- Constraints for table `services`
--
ALTER TABLE `services`
  ADD CONSTRAINT `services_ibfk_1` FOREIGN KEY (`petsitter_id`) REFERENCES `petsitter` (`id`);

--
-- Constraints for table `sitter_complaints`
--
ALTER TABLE `sitter_complaints`
  ADD CONSTRAINT `sitter_complaints_ibfk_1` FOREIGN KEY (`sitter_id`) REFERENCES `petsitter` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
