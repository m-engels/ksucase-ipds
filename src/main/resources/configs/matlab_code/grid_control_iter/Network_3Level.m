function Net=Network_3Level(ieee37_temp)
% clc
% clear all
% close all
% 
% ieee37=xlsread('C:\Users\malekpour\Desktop\Optimal Power Flow\System_Data_3phase_OPF1.xlsx');
% ieee37=sortrows(ieee37,4);
% ieee37_temp=ieee37;

Net.Sub=ieee37_temp(1:37,:);

Net.SubNode(1).Feeder=[];

Net.SubNode(2).Feeder=[];

Net.SubNode(3).Feeder=ieee37_temp(506:509,:);
Net.SubNode(3).FeederNode(1).Neighborhood=ieee37_temp(510:514,:);
Net.SubNode(3).FeederNode(2).Neighborhood=ieee37_temp(515:519,:);
Net.SubNode(3).FeederNode(3).Neighborhood=ieee37_temp(520:524,:);
Net.SubNode(3).FeederNode(4).Neighborhood=ieee37_temp(525:529,:);

Net.SubNode(4).Feeder=[];

Net.SubNode(5).Feeder=[];

Net.SubNode(6).Feeder=ieee37_temp(260:261,:);
Net.SubNode(6).FeederNode(1).Neighborhood=ieee37_temp(262:266,:);
Net.SubNode(6).FeederNode(2).Neighborhood=ieee37_temp(267:271,:);

Net.SubNode(7).Feeder=ieee37_temp(236:237,:);
Net.SubNode(7).FeederNode(1).Neighborhood=ieee37_temp(238:242,:);
Net.SubNode(7).FeederNode(2).Neighborhood=ieee37_temp(243:247,:);

Net.SubNode(8).Feeder=ieee37_temp(248:249,:);
Net.SubNode(8).FeederNode(1).Neighborhood=ieee37_temp(250:254,:);
Net.SubNode(8).FeederNode(2).Neighborhood=ieee37_temp(255:259,:);

Net.SubNode(9).Feeder=ieee37_temp(224:225,:);
Net.SubNode(9).FeederNode(1).Neighborhood=ieee37_temp(226:230,:);
Net.SubNode(9).FeederNode(2).Neighborhood=ieee37_temp(231:235,:); 

Net.SubNode(10).Feeder=ieee37_temp(272:275,:);
Net.SubNode(10).FeederNode(1).Neighborhood=ieee37_temp(276:280,:);
Net.SubNode(10).FeederNode(2).Neighborhood=ieee37_temp(281:285,:);
Net.SubNode(10).FeederNode(3).Neighborhood=ieee37_temp(286:290,:);
Net.SubNode(10).FeederNode(4).Neighborhood=ieee37_temp(291:295,:);

Net.SubNode(11).Feeder=[];

Net.SubNode(12).Feeder=[];

Net.SubNode(13).Feeder=ieee37_temp(320:321,:);
Net.SubNode(13).FeederNode(1).Neighborhood=ieee37_temp(322:326,:);
Net.SubNode(13).FeederNode(2).Neighborhood=ieee37_temp(327:331,:);

Net.SubNode(14).Feeder=ieee37_temp(332:335,:);
Net.SubNode(14).FeederNode(1).Neighborhood=ieee37_temp(336:340,:);
Net.SubNode(14).FeederNode(2).Neighborhood=ieee37_temp(341:345,:);
Net.SubNode(14).FeederNode(3).Neighborhood=ieee37_temp(346:350,:);
Net.SubNode(14).FeederNode(4).Neighborhood=ieee37_temp(351:355,:);

Net.SubNode(15).Feeder=ieee37_temp(356:357,:);
Net.SubNode(15).FeederNode(1).Neighborhood=ieee37_temp(358:362,:);
Net.SubNode(15).FeederNode(2).Neighborhood=ieee37_temp(363:367,:);
 
Net.SubNode(16).Feeder=[];

Net.SubNode(17).Feeder=ieee37_temp(368:371,:);
Net.SubNode(17).FeederNode(1).Neighborhood=ieee37_temp(372:376,:);
Net.SubNode(17).FeederNode(2).Neighborhood=ieee37_temp(377:381,:);
Net.SubNode(17).FeederNode(3).Neighborhood=ieee37_temp(382:386,:);
Net.SubNode(17).FeederNode(4).Neighborhood=ieee37_temp(387:391,:);

Net.SubNode(18).Feeder=ieee37_temp(392:393,:);
Net.SubNode(18).FeederNode(1).Neighborhood=ieee37_temp(394:398,:);
Net.SubNode(18).FeederNode(2).Neighborhood=ieee37_temp(399:403,:);

Net.SubNode(19).Feeder=ieee37_temp(404:409,:);
Net.SubNode(19).FeederNode(1).Neighborhood=ieee37_temp(410:414,:);
Net.SubNode(19).FeederNode(2).Neighborhood=ieee37_temp(415:419,:);
Net.SubNode(19).FeederNode(3).Neighborhood=ieee37_temp(420:424,:);
Net.SubNode(19).FeederNode(4).Neighborhood=ieee37_temp(425:429,:);
Net.SubNode(19).FeederNode(5).Neighborhood=ieee37_temp(430:434,:);
Net.SubNode(19).FeederNode(6).Neighborhood=ieee37_temp(435:439,:);

Net.SubNode(20).Feeder=ieee37_temp(476:480,:);
Net.SubNode(20).FeederNode(1).Neighborhood=ieee37_temp(481:485,:);
Net.SubNode(20).FeederNode(2).Neighborhood=ieee37_temp(486:490,:);
Net.SubNode(20).FeederNode(3).Neighborhood=ieee37_temp(491:495,:);
Net.SubNode(20).FeederNode(4).Neighborhood=ieee37_temp(496:500,:);
Net.SubNode(20).FeederNode(5).Neighborhood=ieee37_temp(501:505,:);

Net.SubNode(21).Feeder=[];

Net.SubNode(22).Feeder=ieee37_temp(440:443,:);
Net.SubNode(22).FeederNode(1).Neighborhood=ieee37_temp(444:448,:);
Net.SubNode(22).FeederNode(2).Neighborhood=ieee37_temp(449:453,:);
Net.SubNode(22).FeederNode(3).Neighborhood=ieee37_temp(454:458,:);
Net.SubNode(22).FeederNode(4).Neighborhood=ieee37_temp(459:463,:);

Net.SubNode(23).Feeder=ieee37_temp(464:465,:);
Net.SubNode(23).FeederNode(1).Neighborhood=ieee37_temp(466:470,:);
Net.SubNode(23).FeederNode(2).Neighborhood=ieee37_temp(471:475,:);

Net.SubNode(24).Feeder=ieee37_temp(296:299,:);
Net.SubNode(24).FeederNode(1).Neighborhood=ieee37_temp(300:304,:);
Net.SubNode(24).FeederNode(2).Neighborhood=ieee37_temp(305:309,:);
Net.SubNode(24).FeederNode(3).Neighborhood=ieee37_temp(310:314,:);
Net.SubNode(24).FeederNode(4).Neighborhood=ieee37_temp(315:319,:);

Net.SubNode(25).Feeder=ieee37_temp(530:534,:);
Net.SubNode(25).FeederNode(1).Neighborhood=ieee37_temp(535:539,:);
Net.SubNode(25).FeederNode(2).Neighborhood=ieee37_temp(540:544,:);
Net.SubNode(25).FeederNode(3).Neighborhood=ieee37_temp(545:549,:);
Net.SubNode(25).FeederNode(4).Neighborhood=ieee37_temp(550:554,:);
Net.SubNode(25).FeederNode(5).Neighborhood=ieee37_temp(555:559,:);
 
Net.SubNode(26).Feeder=[];

Net.SubNode(27).Feeder=ieee37_temp(176:179,:);
Net.SubNode(27).FeederNode(1).Neighborhood=ieee37_temp(180:184,:);
Net.SubNode(27).FeederNode(2).Neighborhood=ieee37_temp(185:189,:);
Net.SubNode(27).FeederNode(3).Neighborhood=ieee37_temp(190:194,:);
Net.SubNode(27).FeederNode(4).Neighborhood=ieee37_temp(195:199,:);

Net.SubNode(28).Feeder=ieee37_temp(200:203,:);
Net.SubNode(28).FeederNode(1).Neighborhood=ieee37_temp(204:208,:);
Net.SubNode(28).FeederNode(2).Neighborhood=ieee37_temp(209:213,:);
Net.SubNode(28).FeederNode(3).Neighborhood=ieee37_temp(214:218,:);
Net.SubNode(28).FeederNode(4).Neighborhood=ieee37_temp(219:223,:);

Net.SubNode(29).Feeder=ieee37_temp(146:149,:);
Net.SubNode(29).FeederNode(1).Neighborhood=ieee37_temp(150:154,:);
Net.SubNode(29).FeederNode(2).Neighborhood=ieee37_temp(155:159,:);
Net.SubNode(29).FeederNode(3).Neighborhood=ieee37_temp(160:164,:);
Net.SubNode(29).FeederNode(4).Neighborhood=ieee37_temp(165:169,:);

Net.SubNode(30).Feeder=[];

Net.SubNode(31).Feeder=ieee37_temp(170,:);
Net.SubNode(31).FeederNode(1).Neighborhood=ieee37_temp(171:175,:);

Net.SubNode(32).Feeder=ieee37_temp(38:41,:);
Net.SubNode(32).FeederNode(1).Neighborhood=ieee37_temp(42:46,:);
Net.SubNode(32).FeederNode(2).Neighborhood=ieee37_temp(47:51,:);
Net.SubNode(32).FeederNode(3).Neighborhood=ieee37_temp(52:56,:);
Net.SubNode(32).FeederNode(4).Neighborhood=ieee37_temp(57:61,:);

Net.SubNode(33).Feeder=ieee37_temp(74:77,:);
Net.SubNode(33).FeederNode(1).Neighborhood=ieee37_temp(78:82,:);
Net.SubNode(33).FeederNode(2).Neighborhood=ieee37_temp(83:87,:);
Net.SubNode(33).FeederNode(3).Neighborhood=ieee37_temp(88:92,:);
Net.SubNode(33).FeederNode(4).Neighborhood=ieee37_temp(93:97,:);
 
Net.SubNode(34).Feeder=[];
 
Net.SubNode(35).Feeder=ieee37_temp(62:63,:);
Net.SubNode(35).FeederNode(1).Neighborhood=ieee37_temp(64:68,:);
Net.SubNode(35).FeederNode(2).Neighborhood=ieee37_temp(69:73,:); 

Net.SubNode(36).Feeder=[];

Net.SubNode(37).Feeder=ieee37_temp(98:103,:);
Net.SubNode(37).FeederNode(1).Neighborhood=ieee37_temp(104:108,:);
Net.SubNode(37).FeederNode(2).Neighborhood=ieee37_temp(109:113,:);
Net.SubNode(37).FeederNode(3).Neighborhood=ieee37_temp(114:118,:);
Net.SubNode(37).FeederNode(4).Neighborhood=ieee37_temp(119:123,:);
Net.SubNode(37).FeederNode(5).Neighborhood=ieee37_temp(124:128,:);
Net.SubNode(37).FeederNode(6).Neighborhood=ieee37_temp(129:133,:);

Net.SubNode(38).Feeder=ieee37_temp(134:135,:);
Net.SubNode(38).FeederNode(1).Neighborhood=ieee37_temp(136:140,:);
Net.SubNode(38).FeederNode(2).Neighborhood=ieee37_temp(141:145,:);


