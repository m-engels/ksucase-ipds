function ieee37_temp=PowerSystemDataOPF()
% clc
% clear all
% close all


%%
Z=lineimpedance;  % Line impedance
ieee37=xlsread('C:\Users\malekpour\Desktop\Optimal Power Flow\System_Data_3phase_OPF1.xlsx');
ieee37=sortrows(ieee37,4);
ieee37_temp=ieee37;
%%
% load PV_Irradiance
PV_Irradiance1=xlsread('C:\Users\malekpour\Desktop\Optimal Power Flow\Generation_Full_Sunny.xlsx');
PV_Irradiance1=[zeros(18000,1); PV_Irradiance1; zeros(14400,1)];
PG=5*PV_Irradiance1/1000;PG_max=max(PG);
PF_load=tan(acos(0.9));
Penetration=0.50;
%%
% for i=1:86400
%     if A(i,32)>15
%         A(i,32)=A(i,32)-(A(i,32)-15)/2;
%     end
% end
load Spot_Load % Commercial_Load
Commercial_Load=3*Commercial_Load;
load modified_Load
Load_PQ_400=[A A A A];
% load Large_System_Load_Data % Load_PQ_400
% Load_PQ_400=Load_PQ_400; 
%%
Home.PhaseA.Node=find(ieee37(:,7)==1);
Home.PhaseB.Node=find(ieee37(:,9)==1);
Home.PhaseC.Node=find(ieee37(:,11)==1);

Commercial.PhaseA.Node=find(ieee37(:,7)==2);
Commercial.PhaseB.Node=find(ieee37(:,9)==2);
Commercial.PhaseC.Node=find(ieee37(:,11)==2);

Home.PhaseA.Index=randperm(size(Load_PQ_400,2),length(Home.PhaseA.Node));
Home.PhaseA.Index=[145,147,95,111,163,9,78,6,156,126,87,155,124,83,82,47,31,161,128,49,70,89,1,94,108,21,38,91,148,69,50,34,141,110,157,56,153,63,20,116,98,152,42,121,23,159,90,33,136,129,165,45,142,123,112,13,96,117,120,39,65,46,29,18,2,104,59,52,12,36,137,144,140,54,160,151,4,102,135,7,73,127,154,64,97,114,146,25,88,26,40,138,75,15,158,86,164,22,11,122,16,139,35,103,81,17,133,53,74,132,93,72,150,28,162,113,105,61,67,37,79,8,80,77,14,43,32,24,109,57,76,125,99,27,118,115,107,101,134,10,3,62,85,66;];
Home.PhaseB.Index=randperm(size(Load_PQ_400,2),length(Home.PhaseB.Node));
Home.PhaseB.Index=[36,6,9,73,106,124,72,52,59,149,68,26,152,105,15,48,87,99,60,62,47,145,162,50,160,37,84,137,111,70,118,131,89,71,91,96,101,136,134,16,154,122,98,138,141,78,133,42,158,24,44,113,139,119,21,108,114,157,79,165,151,130,1,103,104,29,146,35,94,115,34,125,33,20,41,93,23,22,147,100,8,57,159,95,81,53,5,155,30,166,17,123,4,140,110,127,18,144,74,143,168,56,11,135,88,80,12,117,32,163,13,10,58,75,39,83,77,126,129,164,45,64,150,92,19,142,63,121,82,109,167,38,102,97,65,90,107,51,25,27,3,55,49,76;];
Home.PhaseC.Index=randperm(size(Load_PQ_400,2),length(Home.PhaseC.Node));
Home.PhaseC.Index=[168,14,42,66,73,110,45,113,156,86,146,67,132,21,115,64,28,37,165,105,117,160,43,164,81,162,99,69,16,49,34,29,9,20,41,97,112,145,55,77,131,94,10,25,87,158,101,111,56,161,89,100,24,93,46,60,39,133,18,58,80,8,118,128,155,154,109,143,148,163,65,123,108,149,116,129,95,61,137,82,31,22,75,51,152,52,47,90,141,6,138,74,53,36,98,124,159,127,135,27,84,104,126,35,119,140,150,32,151,17,85,23,57,153,30,107,96,102,68,76,3,106,44,144,63,13,19,91,147,4,15,142,167,1,120,166,114,38,33,139,62,12,11,7,125,2,26,70,78,88,136,50,5,103,48,134,72,59,79,121;];

Home.PhaseA.PLmax=max(Load_PQ_400(:,Home.PhaseA.Index));
Home.PhaseB.PLmax=max(Load_PQ_400(:,Home.PhaseB.Index));
Home.PhaseC.PLmax=max(Load_PQ_400(:,Home.PhaseC.Index));
%%
Home.PhaseA.NodePV=sort(Home.PhaseA.Node(randperm(length(Home.PhaseA.Node),round(Penetration*length(Home.PhaseA.Node)))))';
Home.PhaseA.NodePV=[43,45,51,54,55,56,58,59,60,61,172,173,174,207,227,232,234,239,244,245,246,247,254,256,258,339,340,343,347,348,349,352,353,414,416,417,421,423,424,439,482,483,487,488,492,493,494,500,502,504,505,511,512,513,514,518,519,524,526,527,528,529,538,541,542,543,544,546,548,549,553,556;];
Home.PhaseB.NodePV=sort(Home.PhaseB.Node(randperm(length(Home.PhaseB.Node),round(Penetration*length(Home.PhaseB.Node)))))';
Home.PhaseB.NodePV=[65,70,71,72,105,107,108,110,111,115,117,118,120,121,125,127,128,131,132,139,140,143,145,172,175,206,208,210,212,215,217,222,223,254,256,257,258,259,301,302,303,304,309,311,312,316,317,319,395,398,401,402,403,445,448,450,455,514,522,524,526,527,529,538,541,548,549,552,553,554,556,559;];
Home.PhaseC.NodePV=sort(Home.PhaseC.Node(randperm(length(Home.PhaseC.Node),round(Penetration*length(Home.PhaseC.Node)))))';
Home.PhaseC.NodePV=[79,81,89,90,92,94,95,97,106,108,153,154,157,161,162,163,166,168,181,184,186,187,192,194,251,258,259,263,264,269,278,279,283,284,285,288,292,293,294,295,323,324,325,328,330,331,359,361,362,365,366,374,375,378,380,383,384,389,391,468,469,472,474,475,511,513,516,518,523,524,526,528,536,537,543,546,549,552,554,559;];
% 
% 
% Home.PhaseA.NodePV=[51,55,173,228,239,245,337,338,343,348,421,422,426,427,438,482,483,494,498,499,505,513,516,520,531,533;];
% c = setdiff(Home.PhaseA.Node, Home.PhaseA.NodePV);
% Home.PhaseA.NodePV=sort([Home.PhaseA.NodePV Home.PhaseA.Node(randperm(length(c),round(0.1*length(Home.PhaseA.Node))))']);
% Home.PhaseA.NodePV=[51,55,58,173,228,229,239,245,254,337,338,339,343,345,347,348,354,411,421,421,422,426,427,438,438,482,483,483,487,490,494,498,499,505,513,516,520,531,533;];
% 
% Home.PhaseB.NodePV=[106,108,125,127,133,145,206,210,215,217,223,254,302,312,313,319,400,403,455,460,463,513,518,525,533,540;];
% c = setdiff(Home.PhaseB.Node, Home.PhaseB.NodePV);
% Home.PhaseB.NodePV=sort([Home.PhaseB.NodePV Home.PhaseB.Node(randperm(length(c),round(0.1*length(Home.PhaseB.Node))))']);
% Home.PhaseB.NodePV=[71,72,73,106,108,112,125,125,127,128,133,142,144,145,206,210,213,215,217,217,223,254,302,312,313,316,319,396,400,403,455,460,461,463,513,518,525,533,540;];
% 
% Home.PhaseC.NodePV=[87,97,108,156,157,158,163,183,184,191,251,271,293,294,330,331,361,364,366,378,389,390,467,469,472,514,518,525,529,539;];
% c = setdiff(Home.PhaseC.Node, Home.PhaseC.NodePV);
% Home.PhaseC.NodePV=sort([Home.PhaseC.NodePV Home.PhaseC.Node(randperm(length(c),round(0.1*length(Home.PhaseC.Node))))']);
% Home.PhaseC.NodePV=[87,97,105,108,151,156,157,158,163,169,183,184,191,194,251,252,265,271,280,285,288,289,292,293,294,294,323,326,330,331,361,364,366,378,386,389,390,467,469,472,514,518,525,529,539;];

%%
% weights=[0.05 0.27 0.27 0.27 0.09 0.05];
weights=[0.09 0.28 0.28 0.28 0.05 0.02];
PV_To_Peak_Load_ratio=[0.25 0.5 0.75 1 1.5 2];
for i=1:length(Home.PhaseA.NodePV)
    choice = roulette_wheel(weights);
%     Home.PhaseA.PGmax(i)=PV_To_Peak_Load_ratio(choice)*Home.PhaseA.PLmax(find(Home.PhaseA.Node==Home.PhaseA.NodePV(i)));
    Home.PhaseA.PGmax(i)=PV_To_Peak_Load_ratio(choice)*Home.PhaseA.PLmax(i);
end
% Home.PhaseA.PGmax=round([13.745625	12.72871875	16.05508333	2.24645	8.130262498	8.546454167	3.8473	6.349375001	13.733	1.655508334	23.0194	16.22351667	4.278675	9.17105	8.55735	3.462708334	12.45449375	12.52416667	6.204783333	5.569466666	6.303775001	13.85083333	8.3105	15.416675	27.08545	25.4574375	8.094233333	4.1771	12.964925	4.4929	5.0432	6.593733333	12.8037	7.610050001	16.60599167	3.442621	4.4929	12.72871875	9.13569375	4.458416667	4.590161334	15.2201	6.622033334	8.130262498	7.6306	10.8848	4.232916667	5.4424	15.416675	5.75485	8.344066666	5.75485	4.449633333	8.344066666	6.303775001	13.82036667	11.23653333	5.4424	7.6946	8.344066666	5.72295	4.01855	8.111758333	27.324425	6.204783333	13.70354063	12.9906	23.1250125	11.23653333	5.77095	8.027541667	18.21628333]);
Home.PhaseA.PGmax=round([18.3275	8.4858125	16.05508333	3.369675	2.032565624	17.09290833	3.8473	4.232916667	6.8665	4.966525001	5.75485	4.055879167	12.836025	18.3421	4.278675	10.388125	16.60599167	9.393125	9.307175	5.569466666	4.202516667	10.388125	5.540333334	15.416675	18.05696667	8.4858125	24.2827	2.784733333	12.964925	4.4929	7.5648	9.8906	25.6074	10.14673333	33.21198333	4.590161334	8.9858	8.4858125	9.13569375	13.37525	3.442621	5.073366667	3.311016667	6.097696873	7.6306	5.4424	4.232916667	5.4424	15.416675	5.75485	8.344066666	11.5097	2.224816667	4.172033333	6.303775001	6.910183333	11.23653333	8.1636	7.6946	8.344066666	3.8153	4.01855	24.335275	18.21628333	12.40956667	18.2713875	4.3302	7.7083375	5.618266667	7.6946	8.027541667	13.66221]);

for i=1:length(Home.PhaseB.NodePV)
    choice = roulette_wheel(weights);
%     Home.PhaseB.PGmax(i)=PV_To_Peak_Load_ratio(choice)*Home.PhaseB.PLmax(find(Home.PhaseB.Node==Home.PhaseB.NodePV(i)));
    Home.PhaseB.PGmax(i)=PV_To_Peak_Load_ratio(choice)*Home.PhaseB.PLmax(i);
end
% Home.PhaseB.PGmax=round([3.8473	12.69875	12.81968125	12.45449375	17.28656667	8.55735	13.733	15.416675	25.9812	3.8153	5.073366667	15.2201	10.14673333	12.72871875	8.5358	4.232916667	5.75485	17.0716	36.43256667	13.70354063	10.388125	27.49125	3.8473	5.0432	9.8906	6.097696873	4.966525001	24.082625	4.4929	4.202516667	6.593733333	20.77625	27.70166667	16.22351667	2.784733333	5.618266667	17.3208	15.416675	7.5648	8.899266666	16.81006667	16.18846667	3.442621	2.809133334	34.1432	5.77095	8.354199999	4.966525001	13.37525	18.05696667	9.307175	24.335275	10.365275	12.52416667	8.4858125	9.028483333	6.8665	8.302995833	4.065131249	6.25805	12.626875	12.05565	8.3105	13.745625	18.2713875	24.335275	9.13569375	3.131041667	15.416675	8.302995833	3.296866667	9.17105]);
Home.PhaseB.PGmax=round([7.6946	2.116458334	8.546454167	8.302995833	8.643283333	25.67205	13.733	7.7083375	8.6604	3.8153	10.14673333	5.073366667	5.073366667	12.72871875	4.2679	6.349375001	11.5097	17.0716	9.108141667	18.2713875	6.925416667	18.3275	3.8473	10.0864	3.296866667	4.065131249	3.311016667	16.05508333	4.4929	8.405033334	6.593733333	10.388125	10.388125	12.1676375	2.784733333	16.8548	8.6604	11.56250625	10.0864	4.449633333	6.303775001	8.094233333	4.590161334	5.618266667	12.8037	7.6946	2.784733333	4.966525001	13.37525	18.05696667	6.204783333	24.335275	10.365275	9.393125	16.971625	18.05696667	13.733	8.302995833	4.065131249	2.086016667	12.626875	4.01855	5.540333334	9.16375	13.70354063	12.1676375	27.40708125	25.04833333	11.56250625	4.151497917	13.18746667	12.228]);

for i=1:length(Home.PhaseC.NodePV)
    choice = roulette_wheel(weights);
%     Home.PhaseC.PGmax(i)=PV_To_Peak_Load_ratio(choice)*Home.PhaseC.PLmax(find(Home.PhaseC.Node==Home.PhaseC.NodePV(i)));
    Home.PhaseC.PGmax(i)=PV_To_Peak_Load_ratio(choice)*Home.PhaseC.PLmax(i);
end
% Home.PhaseC.PGmax=round([3.311016667	2.295080667	13.24406667	4.514241667	12.45449375	10.14673333	5.75485	12.1676375	13.733	12.40956667	18.2713875	12.626875	6.349375001	8.4858125	8.302995833	4.321641667	4.202516667	6.097696873	6.25805	8.4858125	5.4424	13.18746667	8.3105	8.094233333	6.25805	11.5419	12.8037	3.369675	4.449633333	2.784733333	6.593733333	8.111758333	34.18581667	9.13569375	9.17105	3.455091667	6.303775001	18.3275	10.365275	12.52416667	10.388125	11.56250625	23.1250125	12.626875	5.75485	8.916833334	8.6604	6.73935	3.442621	12.52416667	27.70166667	4.449633333	27.08545	12.81968125	6.027825	13.6622125	6.25805	8.354199999	18.21628333	8.899266666	16.18846667	7.5648	13.18746667	12.40956667	8.111758333	6.303775001	16.83583333	8.6604	25.92985	6.097696873	3.8153	6.25805	13.542725	7.6306	13.37525	5.75485	8.027541667	9.16375	16.05508333	4.278675]);
Home.PhaseC.PGmax=round([3.311016667	3.442621	3.311016667	27.08545	12.45449375	10.14673333	8.632275	8.111758333	10.29975	12.40956667	13.70354063	12.626875	6.349375001	16.971625	12.45449375	4.321641667	8.405033334	4.065131249	4.172033333	8.4858125	5.4424	3.296866667	2.770166667	12.14135	4.172033333	5.77095	12.8037	1.123225	6.67445	4.1771	3.296866667	4.055879167	4.273227083	9.13569375	12.22806667	20.73055	4.202516667	13.745625	10.365275	12.52416667	6.925416667	7.7083375	15.416675	8.417916667	5.75485	8.916833334	8.6604	2.24645	2.295080667	12.52416667	10.388125	6.67445	13.542725	8.546454167	12.05565	18.21628333	6.25805	5.569466666	13.6622125	4.449633333	8.094233333	7.5648	9.8906	3.102391667	24.335275	6.303775001	4.208958333	12.9906	8.643283333	8.130262498	5.72295	4.172033333	13.542725	7.6306	17.83366667	8.632275	4.013770833	27.49125	16.05508333	8.55735]);

%%
VA=[];VB=[];VC=[];
% for i=1:60:size(Load_PQ_400,1)
% for i=27000:65400
    i=27000+18000+8000;

    % phase A
    ieee37_temp(Home.PhaseA.Node,7)=Load_PQ_400(i,Home.PhaseA.Index)';
    ieee37_temp(Home.PhaseA.Node,8)=ieee37_temp(Home.PhaseA.Node,7)*PF_load;
    ieee37_temp(Commercial.PhaseA.Node,7)=Commercial_Load(i)*0;
    ieee37_temp(Commercial.PhaseA.Node,8)=ieee37_temp(Commercial.PhaseA.Node,7)*PF_load;    
%    ieee37_temp(Home.PhaseA.NodePV,7)=ieee37_temp(Home.PhaseA.NodePV,7)-0*Home.PhaseA.PGmax'*PG(i)/PG_max;
    ieee37_temp(Home.PhaseA.NodePV,14)=Home.PhaseA.PGmax'*PG(i)/PG_max;

    % phase B    
    ieee37_temp(Home.PhaseB.Node,9)=Load_PQ_400(i,Home.PhaseB.Index)';
    ieee37_temp(Home.PhaseB.Node,10)=ieee37_temp(Home.PhaseB.Node,9)*PF_load;
    ieee37_temp(Commercial.PhaseA.Node,9)=Commercial_Load(i)*0;
    ieee37_temp(Commercial.PhaseA.Node,10)=ieee37_temp(Commercial.PhaseA.Node,9)*PF_load;    
%    ieee37_temp(Home.PhaseB.NodePV,9)=ieee37_temp(Home.PhaseB.NodePV,9)-0*Home.PhaseB.PGmax'*PG(i)/PG_max;
    ieee37_temp(Home.PhaseB.NodePV,15)=Home.PhaseB.PGmax'*PG(i)/PG_max;

    % phase C    
    ieee37_temp(Home.PhaseC.Node,11)=Load_PQ_400(i,Home.PhaseC.Index)';
    ieee37_temp(Home.PhaseC.Node,12)=ieee37_temp(Home.PhaseC.Node,11)*PF_load; 
    ieee37_temp(Commercial.PhaseA.Node,11)=Commercial_Load(i)*0;
    ieee37_temp(Commercial.PhaseA.Node,12)=ieee37_temp(Commercial.PhaseA.Node,11)*PF_load;    
%    ieee37_temp(Home.PhaseC.NodePV,11)=ieee37_temp(Home.PhaseC.NodePV,11)-0*Home.PhaseC.PGmax'*PG(i)/PG_max;
    ieee37_temp(Home.PhaseC.NodePV,16)=Home.PhaseC.PGmax'*PG(i)/PG_max;
    
%     [Volt]=PowerFlowTest12(ieee37_temp, Z);
%     VA=[VA Volt(:,2)];VB=[VB Volt(:,3)];VC=[VC Volt(:,4)];
%     
%     if (((i-1)/3600) == floor((i-1)/3600)) || (i==size(Load_PQ_400,1))
%         i
% %         save Base_Case VA VB VC
%     end    
    
% end

% plot(VA(31,:),'blue','LineWidth',2);
% hold
% plot(VB(31,:),'green','LineWidth',2);
% plot(VC(31,:),'red','LineWidth',2);
% 
% 
% 
% XA=[];
% for i=1:size(VA,1)
%     B = (VA(i,398:1090)<0.95);
%     if any(B)
%         XA(i)=1;
%     else
%         XA(i)=0;
%     end
% end
% sum(XA)
% 
% XB=[];
% for i=1:size(VB,1)
%     B = (VB(i,398:1090)<0.95);
%     if any(B)
%         XB(i)=1;
%     else
%         XB(i)=0;
%     end
% end
% sum(XB)
% 
% XC=[];
% for i=1:size(VC,1)
%     B = (VC(i,398:1090)<0.95);
%     if any(B)
%         XC(i)=1;
%     else
%         XC(i)=0;
%     end
% end
% sum(XC)
